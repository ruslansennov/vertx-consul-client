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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Holds result of key/value pairs query
 *
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@DataObject(generateConverter = true)
public class KeyValueList {

  private long index;
  private List<KeyValue> list;
  private JsonObject tree;

  /**
   * Default constructor
   */
  public KeyValueList() {}

  /**
   * Copy constructor
   *
   * @param other the one to copy
   */
  public KeyValueList(KeyValueList other) {
    this.index = other.index;
    this.list = other.list;
  }

  /**
   * Constructor from JSON
   *
   * @param json the JSON
   */
  public KeyValueList(JsonObject json) {
    KeyValueListConverter.fromJson(json, this);
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    KeyValueListConverter.toJson(this, jsonObject);
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
  public KeyValueList setIndex(long index) {
    this.index = index;
    return this;
  }

  /**
   * Get list of key/value pairs
   *
   * @return list of key/value pairs
   */
  public List<KeyValue> getList() {
    return list;
  }

  /**
   * Get list of key/value pairs as tree
   *
   * @return tree
   */
  public JsonObject getTree() {
    return getTree("/");
  }

  /**
   * Get list of key/value pairs as tree
   *
   * @param delimiter the delimiter
   * @return tree
   */
  public JsonObject getTree(String delimiter) {
    if (tree == null) {
      tree = new JsonObject();
      if (list != null) {
        for (KeyValue keyValue : list) {
          if (keyValue.getKey().endsWith(delimiter)) {
            continue;
          }
          JsonObject json = tree;
          String[] arr = keyValue.getKey().split(delimiter);
          for (int i = 0; i < arr.length; i++) {
            String key = arr[i];
            if (i == arr.length - 1) {
              json.put(key, keyValue.getValue());
            } else {
              JsonObject next = json.getJsonObject(key);
              if (next == null) {
                next = new JsonObject();
                json.put(key, next);
              }
              json = next;
            }
          }
        }
      }
    }
    return tree;
  }

  /**
   * Set list of key/value pairs
   *
   * @param list list of key/value pairs
   * @return reference to this, for fluency
   */
  public KeyValueList setList(List<KeyValue> list) {
    this.list = list;
    this.tree = null;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KeyValueList list1 = (KeyValueList) o;

    if (index != list1.index) return false;
    return list != null ? sorted().equals(list1.sorted()) : list1.list == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (index ^ (index >>> 32));
    result = 31 * result + (list != null ? sorted().hashCode() : 0);
    return result;
  }

  private List<KeyValue> sorted() {
    List<KeyValue> sorted = null;
    if (list != null) {
      sorted = new ArrayList<>(list);
      sorted.sort(Comparator.comparing(KeyValue::getKey));
    }
    return sorted;
  }
}
