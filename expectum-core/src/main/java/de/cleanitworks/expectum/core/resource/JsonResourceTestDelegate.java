package de.cleanitworks.expectum.core.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import de.cleanitworks.expectum.core.junit.TestClassUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonIgnoreProperties.Value.forIgnoredProperties;
import static java.util.stream.Collectors.toList;

/**
 * Supports junit tests using json and hjson resource files.
 *
 * It is designed to work as a delegate behind simple test classes or test base classes.
 * This way it might be usable in various scenarios.
 */
@RequiredArgsConstructor
public class JsonResourceTestDelegate {

  /**
   * By default each concrete test class uses a corresponding json test data file having a similar
   * name (class-name.json) within the same test package.
   * The test class is used to evaluate that file name.
   */
  @NonNull
  private final Supplier<Class<?>> testClassSupplier;

  /**
   * The object mapper defines the way, java objects get serialized to json.
   */
  @Getter
  @Setter
  private ObjectMapper objectMapper = JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  /**
   * Serializes the given object to json.
   *
   * This method can be used to compare the given object state against expected values within a json file.
   * E.g.
   * <pre>assertThat(toJson(myObj)).isEqualTo(json("myExpectedStateNode");</pre>
   *
   * @param obj the object to serialize.
   * @return a json string generated as defined by the #objectMapper.
   */
  String toJson(Object obj) {
    try {
      return TextNodeQuoteWorkaround.unquote(getObjectMapper().writeValueAsString(obj));
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize json: " + obj, e);
    }
  }

  /**
   * Reads the json string, referenced by the given jsonPtr, and converts it to a java bean.
   *
   * <p>Precondition: The target class should be de-serializable for jackson.
   *
   * @param jsonPtr refers to a content node within the json file. E.g.: /myTest/someBeanData or someBeanData
   * @param targetClass the bean class type to provide.
   * @return a new bean instance having property values as provided by the json node.
   */
  public <T> T fromJson(String jsonPtr, Class<T> targetClass) {
    try {
      var jsonString = json(jsonPtr);
      return objectMapper.readValue(jsonString, targetClass);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to deserialize json string.", e);
    }
  }

  /**
   * Provides test data from a file having a name matching the class name. E.g. for a test
   * class XTest a corresponding file XTest.json within the test class package will be used.
   *
   * <p>If the given nodePtr starts with a slash ('/') it will used as an absolute json pointer
   * (See: {@link com.fasterxml.jackson.core.JsonPointer}).
   *
   * <p>If there is no leading slash, a prefix having the name as the calling test method will be
   * added automatically. +
   *
   * <p>Example: If a test needs to read <code>jsonData("/myTestMethodName/someData")</code> it might
   * alternatively use <code>jsonData("someData")</code> to achieve the same result.
   *
   * @param nodePtr refers to a content node within the json file. E.g.: /myTest/request
   * @return the content string of the referenced node (unformatted json).
   */
  protected String json(String nodePtr) {
    return json(testClassSupplier.get(), nodePtr);
  }

  protected String hjson(String nodePtr) {
    return hjson(testClassSupplier.get(), nodePtr);
  }

  protected String hjson(Class<?> ctxtClass, String nodePtr) {
    var absolutePtr = nodePtrToAbsolutePtr(ctxtClass, nodePtr);
    return JsonResourceTestUtil.hjsonData(ctxtClass, absolutePtr);
  }

  /**
   * Similar to {@link #json(String)}, but with the ability to pass a specific class which is
   * used to get the related json file for.
   *
   * @param ctxtClass The class to get the corresponding .json file for. E.g. for class com.foo.Bar
   *     a corresponding file /com/foo/Bar.json will be used.
   * @param nodePtr refers to a content node within the json file. E.g.: /myTest/request
   * @return the content string of the referenced node (unformatted json).
   */
  protected String json(Class<?> ctxtClass, String nodePtr) {
    var absolutePtr = nodePtrToAbsolutePtr(ctxtClass, nodePtr);
    return JsonResourceTestUtil.jsonData(ctxtClass, absolutePtr);
  }

  private String nodePtrToAbsolutePtr(Class<?> ctxtClass, String nodePtr) {
    var isAbsolutePtr = Objects.requireNonNull(nodePtr, "nodePtr should not be null.")
            .startsWith("/");
    return isAbsolutePtr
            ? nodePtr
            : "/" + TestClassUtil.getTestMethodName(ctxtClass) + "/" + nodePtr;
  }

  /**
   * Configures {@link #objectMapper} to ignore the given properties for the given class on json serialization.
   *
   * @param cls the class to configure the property restriction for.
   * @param propNames the properties to ignore.
   */
  public void jsonHide(Class<?> cls, String ... propNames) {
    objectMapper.configOverride(cls).setIgnorals(forIgnoredProperties(propNames));
  }

  static final List<String> GETTER_PREFIXES = List.of("get", "is");

  /**
   * Configures {@link #objectMapper} to serialize only the given properties for the given class.
   *
   * @param cls the class to configure the property restriction for.
   * @param propNames the properties to write on json serialization.
   */
  protected void jsonShow(Class<?> cls, String... propNames) {
    var propsToShow = Set.of(propNames);
    var allProps = Stream.of(cls.getMethods())
            .filter(m -> m.getDeclaringClass() != Object.class)
            .map(JsonResourceTestDelegate::getterToPropertyName)
            .filter(Objects::nonNull)
            .collect(toList());
    var propsToHide = allProps.stream()
            .filter(n -> !propsToShow.contains(n))
            .collect(toList());

    if (allProps.size() != propsToShow.size() + propsToHide.size()) {
      throw new IllegalArgumentException(
              "Properties not found in the given class. Available properties are: " +
                      allProps.stream().sorted().collect(toList()));
    }

    jsonHide(cls, propsToHide.toArray(new String[]{}));
  }

  /**
   * Provides the name of the property provided by a given method if it's a kind of getter
   * (no arguments, return something, not static, starts with a standard getter prefix).
   *
   * @param method the method to check.
   * @return the corresponding property name or <code>null</code> if the method does not match the getter criteria.
   */
  static String getterToPropertyName(Method method) {
    if ((method.getModifiers() & Modifier.STATIC) != 0 ||
            method.getParameterCount() > 0 ||
            method.getReturnType() == Void.class) {
      return null;
    }

    var methodName = method.getName();
    for (var getterPfx : GETTER_PREFIXES) {
      if (methodName.startsWith(getterPfx)) {
        return StringUtils.uncapitalize(StringUtils.substringAfter(methodName, getterPfx));
      }
    }
    return null;
  }

}