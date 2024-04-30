package de.cleanitworks.expectum.core.resource;

/**
 * Convenience base class for tests using json resource files to deliver test fixtures.
 */
public abstract class JsonResourceTest implements JsonResourceTestMixin {

  private final JsonResourceTestDelegate jsonDelegate = new JsonResourceTestDelegate(this::getClass);

  public JsonResourceTestDelegate getJsonDelegate() {
    return jsonDelegate;
  }

}