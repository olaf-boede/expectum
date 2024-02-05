package de.cleanitworks.expectum.core.resource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResourceTestUtilTest {

    @Test
    void jsonData_rootNodeString() {
        assertThat(JsonResourceTestUtil.jsonData(getClass(), "/rootNode"))
            .startsWith("{\"simpleNode\":");
    }

    @Test
    void jsonData_simpleNode() {
        assertThat(JsonResourceTestUtil.jsonData(getClass(), "/rootNode/simpleNode"))
            .isEqualTo("simpleNode Content");
    }

    @Test
    void jsonData_arrayNode() {
        assertThat(JsonResourceTestUtil.jsonData(getClass(), "/rootNode/arrayNode"))
            .isEqualTo("[\"hello\",\"world\"]");
    }

    @Test
    void hjsonData_rootNodeString() {
        assertThat(JsonResourceTestUtil.hjsonData(getClass(), "/rootNode"))
                .startsWith("{\"simpleNode\":");
    }

    @Test
    void hjsonData_simpleNode() {
        assertThat(JsonResourceTestUtil.jsonData(getClass(), "/rootNode/simpleNode"))
                .isEqualTo("simpleNode Content");
    }

    @Test
    void hjsonData_arrayNode() {
        assertThat(JsonResourceTestUtil.jsonData(getClass(), "/rootNode/arrayNode"))
                .isEqualTo("[\"hello\",\"world\"]");
    }

}