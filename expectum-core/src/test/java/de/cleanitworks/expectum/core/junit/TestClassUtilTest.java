package de.cleanitworks.expectum.core.junit;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class TestClassUtilTest {

  @Test
  void getTestMethodName_forJupiterTest() {
    assertThat(TestClassUtil.getTestMethodName(getClass()))
            .isEqualTo("getTestMethodName_forJupiterTest");
  }

  @ParameterizedTest
  @CsvSource("paramValue")
  @SuppressWarnings("unused")
  void getTestMethodName_forJupiterParameterizedTest(String unusedParam) {
    assertThat(TestClassUtil.getTestMethodName(getClass()))
            .isEqualTo("getTestMethodName_forJupiterParameterizedTest");
  }

  @RepeatedTest(2)
  void getTestMethodName_forJupiterRepeatedTest() {
    assertThat(TestClassUtil.getTestMethodName(getClass()))
            .isEqualTo("getTestMethodName_forJupiterRepeatedTest");
  }

  @org.junit.Test
  public void getTestMethodName_forJunitVintageTest() {
    assertThat(TestClassUtil.getTestMethodName(getClass()))
            .isEqualTo("getTestMethodName_forJunitVintageTest");
  }
}