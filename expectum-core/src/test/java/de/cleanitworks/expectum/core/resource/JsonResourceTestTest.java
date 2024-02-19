package de.cleanitworks.expectum.core.resource;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JsonResourceTestTest extends JsonResourceTest {
    @Test
    void json_simpleNode() {
        assertThat(json("simpleNode"))
                .isEqualTo("abc");
    }

    @Test
    void testBean() {
        assertThat(toJson(TestBean.create())).isEqualTo(json("expected"));
    }

    @Test
    void jsonHide() {
        jsonHide(TestBean.class, "localDate", "itemList", "primitiveBoolean");

        assertThat(toJson(TestBean.create())).isEqualTo(json("expected"));
    }

    @Test
    void jsonShow() {
        jsonShow(TestBean.class, "localDate", "string");

        assertThat(toJson(TestBean.create())).isEqualTo(json("expected"));
    }

    @Test
    void jsonShow_withUnknownPropertyName_fails() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> jsonShow(TestBean.class, "unknown"))
                .withMessage("Properties not found in the given class. Available properties are: " +
                        "[itemList, localDate, primitiveBoolean, string]");
    }

    @Test
    void localDateTime_timestampFormat() {
        this.getJsonDelegate().setObjectMapper(JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build());

        assertThat(toJson(LocalDateTime.parse("2023-01-23T15:50"))).isEqualTo("[2023,1,23,15,50]");
    }

    @Test
    void localDateTime_isoFormat() {
        assertThat(toJson(LocalDateTime.parse("2023-01-23T15:50"))).isEqualTo("2023-01-23T15:50:00");
    }

    @Test
    void instant_timestampFormat() {
        this.getJsonDelegate().setObjectMapper(JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build());
        assertThat(toJson(Instant.parse("2007-12-03T10:15:30.00Z"))).isEqualTo("1196676930.000000000");
    }

    @Test
    void instant_isoFormat() {
        assertThat(toJson(Instant.parse("2007-12-03T10:15:30.00Z"))).isEqualTo("2007-12-03T10:15:30Z");
    }

    @Test
    void fromJson() {
        var bean = fromJson("beandata", TestBean.class);

        assertThat(bean).isNotNull();
        assertThat(toJson(bean)).isEqualTo(json("beandata"));
    }

}

@Value @Builder @Jacksonized
class TestBean {
    String string;
    LocalDate localDate;
    boolean primitiveBoolean;
    List<TestItem> itemList;

    static TestBean create() {
        return TestBean.builder()
                .string("string value")
                .localDate(LocalDate.parse("2020-07-01"))
                .itemList(List.of(TestItem.builder().name("item name").build()))
                .build();
    }

    public static String getStaticIsNotSerializedToJson() {
        return "static content";
    }
}

@Value @Builder @Jacksonized
class TestItem {
    String name;
}