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
package io.vertx.ext.consul.utils;

import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;
import com.pszymczyk.consul.LogLevel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collector;

import static io.vertx.ext.consul.utils.ConsulTestUtils.*;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class ConsulAgent {

  private final ConsulDatacenter datacenter;
  private final ConsulProcess process;
  private final String host;

  ConsulAgent(ConsulDatacenter dc, ConsulAgentOptions options) {
    JsonObject cfg = new JsonObject()
      .put("server", true)
      .put("advertise_addr", options.getAddress())
      .put("client_addr", options.getAddress())
      .put("leave_on_terminate", true)
      .put("key_file", options.getKeyFile())
      .put("cert_file", options.getCertFile())
      .put("ca_file", options.getCaFile())
      .put("ports", new JsonObject().put("https", getFreePort()))
      .put("addresses", new JsonObject().put("https", "0.0.0.0"))
      .put("datacenter", dc.getName())
      .put("node_name", "node-" + randomHex(16))
      .put("node_id", randomNodeId())
      .put("acl_default_policy", "deny")
      .put("acl_master_token", dc.getMasterToken())
      .put("acl_datacenter", dc.getName());
    List<ConsulAgent> existingAgents = dc.getAgents();
    if (!existingAgents.isEmpty()) {
      cfg.put("start_join", existingAgents.stream()
        .map(agent -> "127.0.0.1:" + agent.getSerfLanPort())
        .collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::addAll)));
    }
    host = options.getAddress();
    datacenter = dc;
    process = ConsulStarterBuilder.consulStarter()
      .withLogLevel(LogLevel.ERR)
      .withConsulVersion(options.getConsulVersion())
      .withCustomConfig(cfg.encode())
      .build()
      .start();
    datacenter.addAgent(this);
  }

  public int getSerfLanPort() {
    return process.getSerfLanPort();
  }

  public int getHttpPort() {
    return process.getHttpPort();
  }

  public String getAddress() {
    return host;
  }

  public void stop() {
    datacenter.removeAgent(this);
    process.close();
  }
}
