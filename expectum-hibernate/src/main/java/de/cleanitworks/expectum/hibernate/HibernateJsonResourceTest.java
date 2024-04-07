package de.cleanitworks.expectum.hibernate;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import de.cleanitworks.expectum.core.resource.JsonResourceTest;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.function.Consumer;

public class HibernateJsonResourceTest extends JsonResourceTest {

    protected Session session;

    @BeforeEach
    void hibernateSerializer() {
        var om = getJsonDelegate().createObjectMapper();
        om.registerModule(new Hibernate6Module()
                .configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true));
        // XXX: useful general setup?
        // om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @BeforeEach
    void rootSession() {
        session = HibernateUtil.getSessionFactory().openSession();
    }

    @AfterEach
    void closeSession() {
        if (session != null) {
            session.close();
        }
    }

    protected void doInTxn(Consumer<Session> runnable) {
        try(var writeSession = HibernateUtil.getSessionFactory().openSession()) {
            writeSession.beginTransaction();
            runnable.accept(writeSession);
            writeSession.flush();
        }
    }
}