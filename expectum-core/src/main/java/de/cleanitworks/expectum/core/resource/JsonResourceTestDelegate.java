package de.cleanitworks.expectum.core.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MutableConfigOverride;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.cleanitworks.expectum.core.junit.TestClassUtil;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonIgnoreProperties.Value.forIgnoredProperties;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Supports junit tests using json and hjson resource files.
 *
 * It is designed to work as a delegate behind simple test classes or test base classes.
 * This way it might be usable in various scenarios.
 */
public class JsonResourceTestDelegate {

  static final List<String> GETTER_PREFIXES = List.of("get", "is");

  /**
   * By default, each concrete test class uses a corresponding json test data file having a similar
   * name (class-name.json) within the same test package.
   * The test class is used to evaluate that file name.
   */
  private final Supplier<Class<?>> testClassSupplier;


  /**
   * The object mapper defines the way, java objects get serialized to json.
   */
  private ObjectMapper objectMapper = createObjectMapper();

  /**
   * By default, each concrete test class uses a corresponding json test data file having a similar
   * name (class-name.json) within the same test package.
   * The test class is used to evaluate that file name.
   *
   * @param testClassSupplier provides the test class used for getting corresponding test data.
   */
  public JsonResourceTestDelegate(Supplier<Class<?>> testClassSupplier) {
    this.testClassSupplier = requireNonNull(testClassSupplier);
  }

  public ObjectMapper createObjectMapper() {
    this.objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    return this.objectMapper;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public ObjectMapper setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = requireNonNull(objectMapper);
    return this.objectMapper;
  }

  /**
   * Serializes the given object to a json string.
   *
   * This method may be used to compare the given object state against expected json string.
   * E.g.
   * <pre>assertThat(toJson(myObj)).isEqualTo(json("myExpectedStateNode");</pre>
   *
   * @param obj the object to serialize.
   * @return a json string generated as defined by the #objectMapper.
   */
  public String toJson(Object obj) {
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
      String jsonString = json(jsonPtr);
      return objectMapper.readValue(jsonString, targetClass);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to deserialize json string.", e);
    }
  }

  /**
   * Provides test data from a file having a name matching the class name. E.g. for a test
   * class XTest a corresponding file XTest.json within the test class package will be used.
   *
   * <p>If the given nodePtr starts with a slash ('/') it will be used as an absolute json pointer
   * (See: {@link com.fasterxml.jackson.core.JsonPointer}).
   *
   * <p>If there is no leading slash, a prefix having the name as the calling test method will be
   * added automatically.
   *
   * <p>Example: If a test needs to read <code>jsonData("/myTestMethodName/someData")</code> it might
   * alternatively use <code>jsonData("someData")</code> to achieve the same result.
   *
   * @param nodePtr refers to a content node within the json file. E.g.: /myTest/request
   * @return the content string of the referenced node (unformatted json).
   */
  public String json(String nodePtr) {
    return json(testClassSupplier.get(), nodePtr);
  }

  public String hjson(String nodePtr) {
    return hjson(testClassSupplier.get(), nodePtr);
  }

  public String hjson(Class<?> ctxtClass, String nodePtr) {
    String absolutePtr = nodePtrToAbsolutePtr(ctxtClass, nodePtr);
    return hjsonData(ctxtClass, absolutePtr);
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
  public String json(Class<?> ctxtClass, String nodePtr) {
    return getNodeAsString(
          ctxtClass,
          ctxtClass.getSimpleName() + ".json",
          nodePtrToAbsolutePtr(ctxtClass, nodePtr));
  }

   /**
   * Compares the json serialization result of the given bean to expected json content referenced by the given relative
   * json pointer.
   * <p/>
   * Performs first a simple string compare operation.
   * If that fails an additional JSONAssert compare operation is used to provide more details about the difference.
   *
   * @param bean the bean to verify.
   * @param nodeInTestMethodJson the json sub node containing the expected bean data.
   */
  public void assertJsonNode(Object bean, String nodeInTestMethodJson) {
    String beanJson = toJson(bean);
    String expectedJson = json(nodeInTestMethodJson);
    try {
      assertThat(beanJson).isEqualTo(expectedJson);
    } catch (AssertionError stringCompareError) {
      try {
        JSONAssert.assertEquals(expectedJson, beanJson, JSONCompareMode.STRICT);
      } catch (JSONException je) {
      } catch (AssertionError jsonAssertError) {
        System.out.println("JSONAssert finding: " + jsonAssertError.getMessage());
      }
      throw stringCompareError;
    }
  }

  /**
   * Configures {@link #objectMapper} to ignore the given properties for the given class on json serialization.
   *
   * @param cls the class to configure the property restriction for.
   * @param propNames the properties to ignore.
   */
  public void jsonHide(Class<?> cls, String ... propNames) {
    MutableConfigOverride clsOverride = objectMapper.configOverride(cls);
    clsOverride.setIgnorals(forIgnoredProperties(propNames));
  }

  /**
   * Configures {@link #objectMapper} to serialize only the given properties for the given class.
   *
   * @param cls the class to configure the property restriction for.
   * @param propNames the properties to write on json serialization.
   */
  public void jsonShow(Class<?> cls, String... propNames) {
    Set<String> propsToShow = Set.of(propNames);
    List<String> allProps = Stream.of(cls.getMethods())
            .filter(m -> m.getDeclaringClass() != Object.class)
            .map(JsonResourceTestDelegate::getterToPropertyName)
            .filter(Objects::nonNull)
            .collect(toList());
    List<String> propsToHide = allProps.stream()
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

    String methodName = method.getName();
    for (String getterPfx : GETTER_PREFIXES) {
      if (methodName.startsWith(getterPfx)) {
        return StringUtils.uncapitalize(StringUtils.substringAfter(methodName, getterPfx));
      }
    }
    return null;
  }

  private String nodePtrToAbsolutePtr(Class<?> ctxtClass, String nodePtr) {
    boolean isAbsolutePtr = requireNonNull(nodePtr, "nodePtr should not be null.")
          .startsWith("/");
    return isAbsolutePtr
          ? nodePtr
          : "/" + TestClassUtil.getTestMethodName(ctxtClass) + "/" + nodePtr;
  }

  private String hjsonData(Class<?> testClass, String nodePtr) {
    String fileName = testClass.getSimpleName() + ".hjson";
    try (Reader reader = new InputStreamReader(getResourceFile(testClass, fileName))) {
      String jsonString = JsonValue.readHjson(reader).toString();
      ObjectNode rootNode = (ObjectNode) resFileObjectMapper().readTree(jsonString);
      return getNodeAsString(rootNode, fileName, nodePtr);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read " + fileName, e);
    }
  }

  private String getNodeAsString(Class<?> ctxtClass, String jsonFileName, String nodePtr) {
    ObjectNode root = getRootNode(ctxtClass, jsonFileName);
    return getNodeAsString(root, jsonFileName, nodePtr);
  }

  private String getNodeAsString(ObjectNode root, String fileName, String nodePtr) {
    JsonNode subNode = root.at(nodePtr);
    if (subNode.isMissingNode()) {
      throw new IllegalArgumentException(
            "Node '" + nodePtr + "' not found in file: " + fileName);
    }

    try {
      String nodeString = resFileObjectMapper().writeValueAsString(subNode);
      return TextNodeQuoteWorkaround.unquote(nodeString);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
            "Unable to read value of '" + nodePtr + "' from file " + fileName, e);
    }
  }

  private ObjectNode getRootNode(Class<?> ctxtClass, String jsonFileName) {
    try (InputStream inputStream = getResourceFile(ctxtClass, jsonFileName)) {
      return (ObjectNode) resFileObjectMapper().readTree(inputStream);
    } catch (IOException e) {
      throw new IllegalStateException(
            "Unable to read json content from file '" + jsonFileName + "'.", e);
    }
  }

  /**
   * Separate getter. Should be used for all code parts dealing with expectation data.
   *
   * @return the OM used for deserializing/serializing expectation data located in json resource files.
   */
  protected ObjectMapper resFileObjectMapper() {
    return objectMapper;
  }

  private InputStream getResourceFile(Class<?> ctxtClass, String fileName) {
    String separator = "/";
    // XXX: Use java core only
    String pkgPath = RegExUtils.replaceAll(ctxtClass.getPackage().getName(), "\\.+", separator);
    String filePath = separator + pkgPath + separator + fileName;
    URL url = ctxtClass.getResource(filePath);
    if (url == null) {
      throw new IllegalArgumentException("Resource file not found: " + filePath);
    }
    return JsonResourceTestDelegate.class.getResourceAsStream(filePath);
  }

}