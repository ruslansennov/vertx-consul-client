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

import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class ConsulDatacenterTest {

  @Test
  public void ke() throws IOException {
    ConsulDatacenter dc = new ConsulDatacenter();
    ConsulAgent agent = dc.attachAgent(new ConsulAgentOptions().setAddress("192.168.1.66"));
    String token = dc.createAclToken("key \"\" {\n" +
      "  policy = \"read\"\n" +
      "}\n" +
      "key \"foo/\" {\n" +
      "  policy = \"read\"\n" +
      "}\n" +
      "event \"\" {\n" +
      "  policy = \"read\"\n" +
      "}\n" +
      "service \"\" {\n" +
      "  policy = \"read\"\n" +
      "}\n" +
      "query \"\" {\n" +
      "  policy = \"read\"\n" +
      "}\n");
    System.out.println(token);
    agent.stop();
  }
}
