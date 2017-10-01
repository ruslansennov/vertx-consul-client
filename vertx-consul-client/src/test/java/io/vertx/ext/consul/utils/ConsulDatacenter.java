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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class ConsulDatacenter {

  private final ObjectMapper mapper = new ObjectMapper();
  private final Object lock = new Object();
  private final String name;
  private final String masterToken;
  private final List<ConsulAgent> agents;

  public ConsulDatacenter() {
    this(new ConsulDatacenterOptions());
  }

  public ConsulDatacenter(ConsulDatacenterOptions options) {
    name = options.getName();
    masterToken = options.getMasterToken();
    agents = new ArrayList<>();
  }

  public ConsulAgent attachAgent() {
    return attachAgent(new ConsulAgentOptions());
  }

  public ConsulAgent attachAgent(ConsulAgentOptions options) {
    return new ConsulAgent(this, options);
  }

  public String createAclToken(String rules) throws IOException {
    if (agents.isEmpty()) {
      throw new RuntimeException("No agents in datacenter");
    }
    HttpClient client = new DefaultHttpClient();
    HttpPut put = new HttpPut("http://" + agents.get(0).getAddress() + ":" + agents.get(0).getHttpPort() + "/v1/acl/create");
    put.addHeader("X-Consul-Token", masterToken);
    Map<String, String> tokenRequest = new HashMap<>();
    tokenRequest.put("Type", "client");
    tokenRequest.put("Rules", rules);
    put.setEntity(new StringEntity(mapper.writeValueAsString(tokenRequest)));
    HttpResponse response = client.execute(put);
    if (response.getStatusLine().getStatusCode() != 200) {
      throw new RuntimeException("Bad response");
    }
    Map<String, String> tokenResponse = mapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Map<String, String>>(){});
    return tokenResponse.get("ID");
  }

  void addAgent(ConsulAgent agent) {
    synchronized (lock) {
      agents.add(agent);
    }
  }

  void removeAgent(ConsulAgent agent) {
    synchronized (lock) {
      agents.remove(agent);
    }
  }

  public String getName() {
    return name;
  }

  public String getMasterToken() {
    return masterToken;
  }

  public List<ConsulAgent> getAgents() {
    return agents;
  }

//  private String post(String url, String )
}
