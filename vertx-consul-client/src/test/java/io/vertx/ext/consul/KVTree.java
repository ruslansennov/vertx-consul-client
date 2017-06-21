package io.vertx.ext.consul;

import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class KVTree extends VertxTestBase {

  @Test
  public void testUninitialized() {
    KeyValueList list = new KeyValueList();
    assertTrue(list.getTree().isEmpty());
  }

  @Test
  public void testEmpty() {
    KeyValueList list = new KeyValueList().setList(Collections.emptyList());
    assertTrue(list.getTree().isEmpty());
  }

  @Test
  public void testSingle() {
  }

  private void testList(List<String> kvs) {
    KeyValueList list = new KeyValueList().setList(kvs.stream().map(e -> {
      String[] kv = e.split(" => ");
      return new KeyValue().setKey(kv[0]).setValue(kv[1]);
    }).collect(Collectors.toList()));
    JsonObject tree = list.getTree();
    assertEquals(tree.getString("key"), "value");
  }
}
