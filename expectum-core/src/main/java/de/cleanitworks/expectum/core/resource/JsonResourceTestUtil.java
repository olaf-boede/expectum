package de.cleanitworks.expectum.core.resource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.hjson.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static lombok.AccessLevel.PRIVATE;

/** Helper methods for writing file data based tests. */
@NoArgsConstructor(access = PRIVATE)
public class JsonResourceTestUtil {

  /** A mapper allowing non-standard C-style inline comments in json. */
  private static final ObjectMapper MAPPER = new ObjectMapper()
          .enable(JsonParser.Feature.ALLOW_COMMENTS);

  /**
   * @param testClass a class to find a .json file having the same name and resource path for.
   * @param nodePtr a json pointer. E.g.: "/myTest/request"
   * @return the content of the requested node as a json string
   */
  public static String jsonData(Class<?> testClass, String nodePtr) {
    return getNodeAsString(testClass, testClass.getSimpleName() + ".json", nodePtr);
  }

  public static String hjsonData(Class<?> testClass, String nodePtr) {
    var fileName = testClass.getSimpleName() + ".hjson";
    try (var reader = new InputStreamReader(getResourceFile(testClass, fileName))) {
      var jsonString = JsonValue.readHjson(reader).toString();
      var rootNode = (ObjectNode) MAPPER.readTree(jsonString);
      return getNodeAsString(rootNode, fileName, nodePtr);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read " + fileName, e);
    }
  }

  private static String getNodeAsString(Class<?> ctxtClass, String jsonFileName, String nodePtr) {
    var root = getRootNode(ctxtClass, jsonFileName);
    return getNodeAsString(root, jsonFileName, nodePtr);
  }

  private static String getNodeAsString(ObjectNode root, String fileName, String nodePtr) {
    var subNode = root.at(nodePtr);
    if (subNode.isMissingNode()) {
      throw new IllegalArgumentException(
              "Node '" + nodePtr + "' not found in file: " + fileName);
    }

    try {
      var nodeString = MAPPER.writeValueAsString(subNode);
      return TextNodeQuoteWorkaround.unquote(nodeString);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
              "Unable to read value of '" + nodePtr + "' from file " + fileName, e);
    }
  }

  private static ObjectNode getRootNode(Class<?> ctxtClass, String jsonFileName) {
    try (var inputStream = getResourceFile(ctxtClass, jsonFileName)) {
      return (ObjectNode) MAPPER.readTree(inputStream);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Unable to read json content from file '" + jsonFileName + "'.", e);
    }
  }

  private static InputStream getResourceFile(Class<?> ctxtClass, String fileName) {
    var separator = "/"; // String.valueOf(File.separatorChar);
    // XXX: Use java core only

    var pkgPath = RegExUtils.replaceAll(ctxtClass.getPackage().getName(), "\\.+", separator);
    var filePath = separator + pkgPath + separator + fileName;
    var url = ctxtClass.getResource(filePath);
    if (url == null) {
      throw new IllegalArgumentException("Resource file not found: " + filePath);
    }
    return JsonResourceTestUtil.class.getResourceAsStream(filePath);
  }
}