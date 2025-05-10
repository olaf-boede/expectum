package de.cleanitworks.expectum.hibernate5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import de.cleanitworks.expectum.core.resource.JsonResourceTest;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.function.Consumer;

public abstract class Hibernate5JsonResourceTest extends JsonResourceTest {

    protected Session session;

    @BeforeEach
    void hibernateSerializer() {
        ObjectMapper om = getJsonDelegate().createObjectMapper();
        om.registerModule(new Hibernate5Module()
                .configure(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true));
        // XXX: useful general setup?
        // om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @BeforeEach
    void rootSession() {
        session = de.cleanitworks.expectum.hibernate5.HibernateUtil.getSessionFactory().openSession();
    }

    @AfterEach
    void closeSession() {
        if (session != null) {
            session.close();
        }
    }

    protected void doInTxn(Consumer<Session> runnable) {
        try(Session writeSession = HibernateUtil.getSessionFactory().openSession()) {
            writeSession.beginTransaction();
            runnable.accept(writeSession);
            writeSession.flush();
        }
    }
}