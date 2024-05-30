package de.cleanitworks.expectum.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Java8Util {

    public static <T> List<T> listOf(T... items) {
        return Collections.unmodifiableList(
                Arrays.stream(items).collect(Collectors.toList()));
    }

    public static <T> Set<T> setOf(T... items) {
        return Collections.unmodifiableSet(
                Arrays.stream(items).collect(Collectors.toSet()));
    }
}