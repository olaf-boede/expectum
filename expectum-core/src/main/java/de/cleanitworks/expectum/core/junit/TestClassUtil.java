package de.cleanitworks.expectum.core.junit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestClassUtil {

  private static final Set<String> TEST_ANNOTATIONS =
      Set.of(
          "org.junit.jupiter.api.Test",
          "org.junit.jupiter.api.RepeatedTest",
          "org.junit.jupiter.params.ParameterizedTest",
          "org.junit.Test",
          "junit.framework.Test");

  /**
   * Provides the name of @Test annotated method that is currently executed.
   *
   * @param testClass the test class to find the running test method in.
   * @return the method name. Never <code>null</code>.
   */
  public static String getTestMethodName(Class<?> testClass) {
    StackTraceElement[] elements = new Throwable().getStackTrace();

    for (StackTraceElement element : elements) {
      var method = findMethod(testClass, element.getMethodName());
      if (method != null
          && Arrays.stream(method.getAnnotations())
              .anyMatch(a -> TEST_ANNOTATIONS.contains(a.annotationType().getName()))) {
        return element.getMethodName();
      }
    }

    throw new IllegalArgumentException(
        "Current stacktrace does not contain a method having one of the supported test annotations: "
            + new ArrayList<>(TEST_ANNOTATIONS));
  }

  private static Method findMethod(Class<?> testClass, String methodName) {
    // XXX: finds just the first one. If there are more than one we might return not the correct
    // method.
    return Arrays.stream(testClass.getDeclaredMethods())
        .filter(m -> m.getName().equals(methodName))
        .findFirst()
        .orElse(null);
  }
}