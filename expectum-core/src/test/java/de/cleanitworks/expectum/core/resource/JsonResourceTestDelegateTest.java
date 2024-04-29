package de.cleanitworks.expectum.core.resource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResourceTestDelegateTest {
    JsonResourceTestDelegate delegate = new JsonResourceTestDelegate(() -> JsonResourceTestDelegateTest.class);

    @Test
    void json_rootNodeString() {
        assertThat(delegate.json(getClass(), "/rootNode"))
            .startsWith("{\"simpleNode\":");
    }

    @Test
    void json_simpleNode() {
        assertThat(delegate.json(getClass(), "/rootNode/simpleNode"))
            .isEqualTo("simpleNode Content");
    }

    @Test
    void json_arrayNode() {
        assertThat(delegate.json(getClass(), "/rootNode/arrayNode"))
            .isEqualTo("[\"hello\",\"world\"]");
    }

    @Test
    void hjson_rootNodeString() {
        assertThat(delegate.hjson(getClass(), "/rootNode"))
                .startsWith("{\"simpleNode\":");
    }

    @Test
    void hjson_simpleNode() {
        assertThat(delegate.hjson(getClass(), "/rootNode/simpleNode"))
                .isEqualTo("simpleNode Content");
    }

    @Test
    void hjson_arrayNode() {
        assertThat(delegate.hjson(getClass(), "/rootNode/arrayNode"))
                .isEqualTo("[\"hello\",\"world\"]");
    }

}