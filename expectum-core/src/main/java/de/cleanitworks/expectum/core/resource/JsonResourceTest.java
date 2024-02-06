package de.cleanitworks.expectum.core.resource;

import lombok.Getter;

/**
 * Convenience base class for tests using json resource files to deliver test fixtures.
 */
public abstract class JsonResourceTest {

  @Getter
  private final JsonResourceTestDelegate jsonDelegate = new JsonResourceTestDelegate(this::getClass);

  protected String toJson(Object obj) {
    return jsonDelegate.toJson(obj);
  }

  /**
   * Provides test data from a file having the a similar name to the class name. E.g. for a test
   * class XTest a corresponding file XTest.json will be read (within the same package path).
   *
   * <p>If the given nodePtr starts with a slash ('/') it will used as an absolute json pointer
   * (See: {@link com.fasterxml.jackson.core.JsonPointer}).
   *
   * <p>If there is no leading slash, a prefix having the name of the calling test method will be
   * added automatically. +
   *
   * <p>Example: If a test uses <code>jsonData("/myTestMethodName/someData")</code> it might
   * alternatively use <code>jsonData("someData")</code>.
   *
   * @param nodePtr refers to a content node within the json file. E.g.: /myTest/request
   * @return the content string of the referenced node (unformatted json).
   */
  protected String json(String nodePtr) {
    return jsonDelegate.json(nodePtr);
  }

  /**
   * Similar to
   * @param nodePtr
   * @return
   */
  protected String hjson(String nodePtr) {
    return jsonDelegate.hjson(nodePtr);
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
    return jsonDelegate.json(ctxtClass, nodePtr);
  }

  protected void jsonHide(Class<?> cls, String ... propNames) {
    jsonDelegate.jsonHide(cls, propNames);
  }

  protected void jsonShow(Class<?> cls, String... propNames) {
    jsonDelegate.jsonShow(cls, propNames);
  }
}