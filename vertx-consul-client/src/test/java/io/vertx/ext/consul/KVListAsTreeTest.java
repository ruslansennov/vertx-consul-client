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

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class KVListAsTreeTest {

  @Test
  public void testEmptyList() {
    assertTrue(new KeyValueList().asTree().isEmpty());
  }

  @Test
  public void t1() {
    assertEquals(list("", "v").asTree(), new JsonObject().put("", "v"));
    assertEquals(list("k", "v").asTree(), new JsonObject().put("k", "v"));
    assertEquals(list("k1/", "v").asTree(), new JsonObject().put("k1", new JsonObject().put("", "v")));
    assertEquals(list("k1/k2", "v").asTree(), new JsonObject().put("k1", new JsonObject().put("k2", "v")));
    assertEquals(list("k1/k2/", "v").asTree(), new JsonObject().put("k1", new JsonObject().put("k2", new JsonObject().put("", "v"))));
  }

  @Test
  public void t2() {
    assertEquals(list("k1", "v1", "k2", "v2").asTree(), new JsonObject().put("k1", "v1").put("k2", "v2"));
    assertEquals(list("p/k1", "v1", "p/k2", "v2").asTree(), new JsonObject().put("p", new JsonObject().put("k1", "v1").put("k2", "v2")));
    assertEquals(list("p/", "v0", "p/k1", "v1", "p/k2", "v2").asTree(), new JsonObject().put("p", new JsonObject().put("", "v0").put("k1", "v1").put("k2", "v2")));
    assertEquals(list("p", "v", "p/", "v0", "p/k1", "v1", "p/k2", "v2").asTree(), new JsonObject().put("p", new JsonObject().put("", "v0").put("k1", "v1").put("k2", "v2")));
  }

  private static KeyValueList list(String... kvList) {
    List<KeyValue> list = new ArrayList<>();
    for (int i = 0; i < kvList.length; i += 2) {
      list.add(new KeyValue().setKey(kvList[i]).setValue(kvList[i+1]));
    }
    return new KeyValueList().setList(list);
  }
}
