package io.vertx.ext.consul.connect;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectOptionsTest {

  @Test
  public void parse() throws Exception {
    JsonObject src = new JsonObject(Utils.readResource("connect_example.json"));
    SidecarServiceOptions scOpts = new ConnectOptions(src).getSidecarService();
    assertEquals(scOpts.getPort(), 33333);
    ProxyOptions proxyOptions = scOpts.getProxy();
    assertEquals(proxyOptions.getUpstreams().size(), 1);
    UpstreamOptions upstream = proxyOptions.getUpstreams().get(0);
    assertEquals(upstream.getDestinationName(), "dev-mesh-database-service");
    assertEquals(upstream.getLocalBindPort(), 19102);
    JsonObject config = proxyOptions.getConfig();
    assertEquals(config.getString("envoy_local_cluster_json"), "envoy_local_cluster_json1");
    assertEquals(config.getString("envoy_public_listener_json"), "envoy_local_cluster_json2");
    assertEquals(config.getString("envoy_prometheus_bind_addr"), "0.0.0.0:19500");
    assertEquals(config.getString("envoy_extra_static_clusters_json"), "envoy_local_cluster_json3");
    ExposeOptions expOptions = proxyOptions.getExpose();
    assertEquals(expOptions.getPaths().size(), 1);
    ExposePathOptions path = expOptions.getPaths().get(0);
    assertEquals(path.getPath(), "/metrics");
    assertEquals(path.getProtocol(), "http");
    assertEquals(path.getLocalPathPort(), (Integer) 53000);
    assertEquals(path.getListenerPort(), (Integer) 19600);
  }

  @Test
  public void convert() throws Exception {
    JsonObject src = new JsonObject(Utils.readResource("connect_example.json"));
    ConnectOptions options = new ConnectOptions(src);
    JsonObject act = new JsonObject(options.toJson().encode());
    assertEquals(src, act);
  }
}
