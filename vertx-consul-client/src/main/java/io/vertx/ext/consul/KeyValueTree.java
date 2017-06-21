/*
 * Copyright (c) 2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.consul;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Holds result of key/value pairs query as {@link JsonObject}
 *
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@DataObject(generateConverter = true)
public class KeyValueTree {

  private long index;
  private JsonObject tree;

  /**
   * Default constructor
   */
  public KeyValueTree() {}

  /**
   * Copy constructor
   *
   * @param other the one to copy
   */
  public KeyValueTree(KeyValueTree other) {
    this.index = other.index;
    this.tree = other.tree;
  }

  /**
   * Constructor from JSON
   *
   * @param json the JSON
   */
  public KeyValueTree(JsonObject json) {
    KeyValueTreeConverter.fromJson(json, this);
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    KeyValueTreeConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  /**
   * Get Consul index
   *
   * @return the consul index
   */
  public long getIndex() {
    return index;
  }

  /**
   * Set Consul index
   *
   * @param index the consul index
   * @return reference to this, for fluency
   */
  public KeyValueTree setIndex(long index) {
    this.index = index;
    return this;
  }

  /**
   * Get tree of key/value pairs
   *
   * @return tree of key/value pairs
   */
  public JsonObject getTree() {
    return tree;
  }

  /**
   * Set tree of key/value pairs
   *
   * @param tree tree of key/value pairs
   * @return reference to this, for fluency
   */
  public KeyValueTree setTree(JsonObject tree) {
    this.tree = tree;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KeyValueTree tree1 = (KeyValueTree) o;

    if (index != tree1.index) return false;
    return tree != null ? this.equals(tree1) : tree1.tree == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (index ^ (index >>> 32));
    result = 31 * result + (tree != null ? hashCode() : 0);
    return result;
  }
}
