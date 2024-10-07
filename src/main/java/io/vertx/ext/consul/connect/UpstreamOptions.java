package io.vertx.ext.consul.connect;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class UpstreamOptions {
  private static final String DEST_NAME = "DestinationName";
  private static final String LOCAL_PORT = "LocalBindPort";

  private String destinationName;
  private String dc;
  private int localBindPort;

  /**
   * Default constructor
   */
  public UpstreamOptions() {
  }

  /**
   * Constructor from JSON
   *
   * @param options the JSON
   */
  public UpstreamOptions(JsonObject options) {
    this.destinationName = options.getString(DEST_NAME);
    this.localBindPort = options.getInteger(LOCAL_PORT);
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(DEST_NAME, destinationName);
    jsonObject.put(LOCAL_PORT, localBindPort);
    return jsonObject;
  }

  public String getDestinationName() {
    return destinationName;
  }

  public UpstreamOptions setDestinationName(String destinationName) {
    this.destinationName = destinationName;
    return this;
  }

  public String getDc() {
    return dc;
  }

  public UpstreamOptions setDc(String dc) {
    this.dc = dc;
    return this;
  }

  public int getLocalBindPort() {
    return localBindPort;
  }

  public UpstreamOptions setLocalBindPort(int localBindPort) {
    this.localBindPort = localBindPort;
    return this;
  }
}
