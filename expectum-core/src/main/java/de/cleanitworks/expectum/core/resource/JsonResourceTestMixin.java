package de.cleanitworks.expectum.core.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JsonResourceTestMixin {

   JsonResourceTestDelegate getJsonDelegate();

   default String toJson(Object obj) {
      return getJsonDelegate().toJson(obj);
   }

   default <T> T fromJson(String jsonPtr, Class<T> targetClass) {
      return getJsonDelegate().fromJson(jsonPtr, targetClass);
   }

   /**
    * Provides test data from a file having the a similar name to the class name. E.g. for a test
    * class XTest a corresponding file XTest.json will be read (within the same package path).
    *
    * <p>If the given nodePtr starts with a slash ('/') it will used as an absolute json pointer
    * (See: {@link com.fasterxml.jackson.core.JsonPointer}).
    *
    * <p>If there is no leading slash, a prefix having the name of the calling test method will be
    * added automatically.
    *
    * <p>Example: If a test uses <code>jsonData("/myTestMethodName/someData")</code> it might
    * alternatively use <code>jsonData("someData")</code>.
    *
    * @param nodePtr refers to a content node within the json file. E.g.: /myTest/request
    * @return the content string of the referenced node (unformatted json).
    */
   default String json(String nodePtr) {
      return getJsonDelegate().json(nodePtr);
   }

   /**
    * Similar to
    * @param nodePtr
    * @return
    */
   default String hjson(String nodePtr) {
      return getJsonDelegate().hjson(nodePtr);
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
   default String json(Class<?> ctxtClass, String nodePtr) {
      return getJsonDelegate().json(ctxtClass, nodePtr);
   }

   default void jsonHide(Class<?> cls, String ... propNames) {
      getJsonDelegate().jsonHide(cls, propNames);
   }

   default void jsonShow(Class<?> cls, String... propNames) {
      getJsonDelegate().jsonShow(cls, propNames);
   }

   default void assertJsonNode(Object bean, String nodeInTestMethodJson) {
      getJsonDelegate().assertJsonNode(bean, nodeInTestMethodJson);
   }

   default ObjectMapper getObjectMapper() {
      return getJsonDelegate().getObjectMapper();
   }

}