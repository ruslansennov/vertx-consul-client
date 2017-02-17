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
package io.vertx.ext.consul.impl;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static io.vertx.ext.consul.impl.Utils.urlEncode;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class ConsulClientImpl implements ConsulClient {

  private static final String TOKEN_HEADER = "X-Consul-Token";
  private static final String INDEX_HEADER = "X-Consul-Index";

  private static final List<Integer> DEFAULT_VALID_CODES = Collections.singletonList(200);
  private static final List<Integer> TXN_VALID_CODES = Arrays.asList(200, 409);

  private final HttpClient httpClient;
  private final String aclToken;
  private final String dc;
  private final long timeoutMs;

  public ConsulClientImpl(Vertx vertx, ConsulClientOptions options) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(options);
    httpClient = vertx.createHttpClient(options);
    aclToken = options.getAclToken();
    dc = options.getDc();
    timeoutMs = options.getTimeoutMs();
  }

  @Override
  public ConsulClient agentInfo(Handler<AsyncResult<JsonObject>> resultHandler) {
    requestObject(HttpMethod.GET, "/v1/agent/self", null, resultHandler, (obj, headers) -> obj).end();
    return this;
  }

  @Override
  public ConsulClient coordinateNodes(Handler<AsyncResult<CoordinateList>> resultHandler) {
    return coordinateNodesWithOptions(null, resultHandler);
  }

  @Override
  public ConsulClient coordinateNodesWithOptions(BlockingQueryOptions options, Handler<AsyncResult<CoordinateList>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/coordinate/nodes", new Query().put(options), resultHandler, (arr, headers) -> {
      List<Coordinate> list = arr.stream().map(obj -> CoordinateParser.parse((JsonObject) obj)).collect(Collectors.toList());
      return new CoordinateList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient coordinateDatacenters(Handler<AsyncResult<List<DcCoordinates>>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/coordinate/datacenters", null, resultHandler, (arr, headers) ->
      arr.stream().map(obj -> CoordinateParser.parseDc((JsonObject) obj)).collect(Collectors.toList())
    ).end();
    return this;
  }

  @Override
  public ConsulClient getValue(String key, Handler<AsyncResult<KeyValue>> resultHandler) {
    return getValueWithOptions(key, null, resultHandler);
  }

  @Override
  public ConsulClient getValueWithOptions(String key, BlockingQueryOptions options, Handler<AsyncResult<KeyValue>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/kv/" + urlEncode(key), new Query().put(options), resultHandler, (arr, headers) ->
      KVParser.parse(arr.getJsonObject(0))).end();
    return this;
  }

  @Override
  public ConsulClient deleteValue(String key, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.DELETE, "/v1/kv/" + urlEncode(key), null, resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient getValues(String keyPrefix, Handler<AsyncResult<KeyValueList>> resultHandler) {
    return getValuesWithOptions(keyPrefix, null, resultHandler);
  }

  @Override
  public ConsulClient getValuesWithOptions(String keyPrefix, BlockingQueryOptions options, Handler<AsyncResult<KeyValueList>> resultHandler) {
    Query query = Query.of("recurse", true).put(options);
    requestArray(HttpMethod.GET, "/v1/kv/" + urlEncode(keyPrefix), query, resultHandler, (arr, headers) -> {
      List<KeyValue> list = arr.stream().map(obj -> KVParser.parse((JsonObject) obj)).collect(Collectors.toList());
      return new KeyValueList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient deleteValues(String keyPrefix, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.DELETE, "/v1/kv/" + urlEncode(keyPrefix), Query.of("recurse", true), resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient putValue(String key, String value, Handler<AsyncResult<Boolean>> resultHandler) {
    return putValueWithOptions(key, value, null, resultHandler);
  }

  @Override
  public ConsulClient putValueWithOptions(String key, String value, KeyValueOptions options, Handler<AsyncResult<Boolean>> resultHandler) {
    Query query = new Query();
    if (options != null) {
      query.put("flags", Long.toUnsignedString(options.getFlags()))
        .put("acquire", options.getAcquireSession())
        .put("release", options.getReleaseSession());
      long cas = options.getCasIndex();
      if (cas >= 0) {
        query.put("cas", cas);
      }
    }
    requestString(HttpMethod.PUT, "/v1/kv/" + urlEncode(key), query, resultHandler,
      (bool, headers) -> Boolean.valueOf(bool)).end(value);
    return this;
  }

  @Override
  public ConsulClient transaction(TxnRequest request, Handler<AsyncResult<TxnResponse>> resultHandler) {
    request(TXN_VALID_CODES, HttpMethod.PUT, "/v1/txn", null, resultHandler, (buff, headers) -> TxnResponseParser.parse(buff.toJsonObject()))
      .end(request.toJson().getJsonArray("operations").encode());
    return this;
  }

  @Override
  public ConsulClient createAclToken(AclToken token, Handler<AsyncResult<String>> idHandler) {
    requestObject(HttpMethod.PUT, "/v1/acl/create", null, idHandler, (obj, headers) ->
      obj.getString("ID")).end(token.toJson().encode());
    return this;
  }

  @Override
  public ConsulClient updateAclToken(AclToken token, Handler<AsyncResult<String>> idHandler) {
    requestObject(HttpMethod.PUT, "/v1/acl/update", null, idHandler, (obj, headers) ->
      obj.getString("ID")).end(token.toJson().encode());
    return this;
  }

  @Override
  public ConsulClient cloneAclToken(String id, Handler<AsyncResult<String>> idHandler) {
    requestObject(HttpMethod.PUT, "/v1/acl/clone/" + urlEncode(id), null, idHandler, (obj, headers) ->
      obj.getString("ID")).end();
    return this;
  }

  @Override
  public ConsulClient listAclTokens(Handler<AsyncResult<List<AclToken>>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/acl/list", null, resultHandler, (arr, headers) ->
      arr.stream()
        .map(obj -> new AclToken((JsonObject) obj))
        .collect(Collectors.toList()))
      .end();
    return this;
  }

  @Override
  public ConsulClient infoAclToken(String id, Handler<AsyncResult<AclToken>> tokenHandler) {
    requestArray(HttpMethod.GET, "/v1/acl/info/" + urlEncode(id), null, tokenHandler, (arr, headers) -> {
      JsonObject jsonObject = arr.getJsonObject(0);
      return new AclToken(jsonObject);
    }).end();
    return this;
  }

  @Override
  public ConsulClient destroyAclToken(String id, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.PUT, "/v1/acl/destroy/" + urlEncode(id), null, resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient fireEvent(String name, Handler<AsyncResult<Event>> resultHandler) {
    fireEventWithOptions(name, null, resultHandler);
    return this;
  }

  @Override
  public ConsulClient fireEventWithOptions(String name, EventOptions options, Handler<AsyncResult<Event>> resultHandler) {
    Query query = new Query();
    if (options != null) {
      query.put("node", options.getNode()).put("service", options.getService()).put("tag", options.getTag());
    }
    requestObject(HttpMethod.PUT, "/v1/event/fire/" + urlEncode(name), query, resultHandler, (jsonObject, headers) -> EventParser.parse(jsonObject))
      .end(options == null || options.getPayload() == null ? "" : options.getPayload());
    return this;
  }

  @Override
  public ConsulClient listEvents(Handler<AsyncResult<EventList>> resultHandler) {
    listEventsWithOptions(null, resultHandler);
    return this;
  }

  @Override
  public ConsulClient listEventsWithOptions(BlockingQueryOptions options, Handler<AsyncResult<EventList>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/event/list", Query.of(options), resultHandler, (jsonArray, headers) -> {
      List<Event> list = jsonArray.stream().map(obj -> EventParser.parse(((JsonObject) obj))).collect(Collectors.toList());
      return new EventList().setList(list).setIndex(Long.parseUnsignedLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient registerService(ServiceOptions serviceOptions, Handler<AsyncResult<Void>> resultHandler) {
    JsonObject jsonOpts = new JsonObject()
      .put("ID", serviceOptions.getId())
      .put("Name", serviceOptions.getName())
      .put("Tags", serviceOptions.getTags())
      .put("Address", serviceOptions.getAddress())
      .put("Port", serviceOptions.getPort());
    if (serviceOptions.getCheckOptions() != null){
      jsonOpts.put("Check", checkOpts(serviceOptions.getCheckOptions(), false));
    }
    requestVoid(HttpMethod.PUT, "/v1/agent/service/register", null, resultHandler).end(jsonOpts.encode());
    return this;
  }

  @Override
  public ConsulClient maintenanceService(MaintenanceOptions opts, Handler<AsyncResult<Void>> resultHandler) {
    Query query = Query.of("enable", opts.isEnable()).put("reason", opts.getReason());
    requestVoid(HttpMethod.PUT, "/v1/agent/service/maintenance/" + urlEncode(opts.getId()), query, resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient deregisterService(String id, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.GET, "/v1/agent/service/deregister/" + urlEncode(id), null, resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient catalogServiceNodes(String service, Handler<AsyncResult<ServiceList>> resultHandler) {
    return catalogServiceNodesWithOptions(service, null, resultHandler);
  }

  @Override
  public ConsulClient catalogServiceNodesWithOptions(String service, ServiceQueryOptions options, Handler<AsyncResult<ServiceList>> resultHandler) {
    Query query = options == null ? null : Query.of("tag", options.getTag()).put("near", options.getNear()).put(options.getBlockingOptions());
    requestArray(HttpMethod.GET, "/v1/catalog/service/" + urlEncode(service), query, resultHandler, (arr, headers) -> {
      List<Service> list = arr.stream().map(obj -> new Service((JsonObject) obj)).collect(Collectors.toList());
      return new ServiceList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient catalogDatacenters(Handler<AsyncResult<List<String>>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/catalog/datacenters", null, resultHandler, (arr, headers) -> arr.getList()).end();
    return this;
  }

  @Override
  public ConsulClient catalogNodes(Handler<AsyncResult<NodeList>> resultHandler) {
    return catalogNodesWithOptions(null, resultHandler);
  }

  @Override
  public ConsulClient catalogNodesWithOptions(NodeQueryOptions options, Handler<AsyncResult<NodeList>> resultHandler) {
    Query query = options == null ? null : Query.of("near", options.getNear()).put(options.getBlockingOptions());
    requestArray(HttpMethod.GET, "/v1/catalog/nodes", query, resultHandler, (arr, headers) -> {
      List<Node> list = arr.stream().map(obj -> NodeParser.parse((JsonObject) obj)).collect(Collectors.toList());
      return new NodeList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient healthServiceNodes(String service, boolean passing, Handler<AsyncResult<ServiceEntryList>> resultHandler) {
    return healthServiceNodesWithOptions(service, passing, null, resultHandler);
  }

  @Override
  public ConsulClient healthServiceNodesWithOptions(String service, boolean passing, BlockingQueryOptions options, Handler<AsyncResult<ServiceEntryList>> resultHandler) {
    Query query = Query.of(options).put("passing", passing ? 1 : null);
    requestArray(HttpMethod.GET, "/v1/health/service/" + urlEncode(service), query, resultHandler, (arr, headers) -> {
      List<ServiceEntry> list = arr.stream().map(obj -> ServiceEntryParser.parse((JsonObject) obj)).collect(Collectors.toList());
      return new ServiceEntryList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient catalogServices(Handler<AsyncResult<ServiceList>> resultHandler) {
    return catalogServicesWithOptions(null, resultHandler);
  }

  @Override
  public ConsulClient catalogServicesWithOptions(BlockingQueryOptions options, Handler<AsyncResult<ServiceList>> resultHandler) {
    requestObject(HttpMethod.GET, "/v1/catalog/services", Query.of(options), resultHandler, (json, headers) -> {
      List<Service> list = json.stream().map(ServiceParser::parseCatalogInfo).collect(Collectors.toList());
      return new ServiceList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient localChecks(Handler<AsyncResult<List<Check>>> resultHandler) {
    requestObject(HttpMethod.GET, "/v1/agent/checks", null, resultHandler, (json, headers) -> json.stream()
      .map(obj -> CheckParser.parse((JsonObject) obj.getValue()))
      .collect(Collectors.toList())).end();
    return this;
  }

  @Override
  public ConsulClient localServices(Handler<AsyncResult<List<Service>>> resultHandler) {
    requestObject(HttpMethod.GET, "/v1/agent/services", null, resultHandler, (json, headers) -> json.stream()
      .map(obj -> ServiceParser.parseAgentInfo((JsonObject) obj.getValue()))
      .collect(Collectors.toList())).end();
    return this;
  }

  @Override
  public ConsulClient catalogNodeServices(String node, Handler<AsyncResult<ServiceList>> resultHandler) {
    return catalogNodeServicesWithOptions(node, null, resultHandler);
  }

  @Override
  public ConsulClient catalogNodeServicesWithOptions(String node, BlockingQueryOptions options, Handler<AsyncResult<ServiceList>> resultHandler) {
    requestObject(HttpMethod.GET, "/v1/catalog/node/" + urlEncode(node), Query.of(options), resultHandler, (json, headers) -> {
      JsonObject nodeInfo = json.getJsonObject("Node");
      String nodeName = nodeInfo.getString("Node");
      String nodeAddress = nodeInfo.getString("Address");
      List<Service> list = json.getJsonObject("Services").stream()
        .map(obj -> ServiceParser.parseNodeInfo(nodeName, nodeAddress, (JsonObject) obj.getValue()))
        .collect(Collectors.toList());
      return new ServiceList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient registerCheck(CheckOptions checkOptions, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.PUT, "/v1/agent/check/register", null, resultHandler).end(checkOpts(checkOptions, true).encode());
    return this;
  }

  private static JsonObject checkOpts(CheckOptions checkOptions, boolean extended) {
    JsonObject json = new JsonObject()
      .put("ID", checkOptions.getId())
      .put("Name", checkOptions.getName())
      .put("Notes", checkOptions.getNotes())
      .put("Script", checkOptions.getScript())
      .put("HTTP", checkOptions.getHttp())
      .put("Interval", checkOptions.getInterval())
      .put("TTL", checkOptions.getTtl())
      .put("TCP", checkOptions.getTcp());
    if (checkOptions.getDeregisterAfter() != null) {
      json.put("DeregisterCriticalServiceAfter", checkOptions.getDeregisterAfter());
    }
    if (checkOptions.getStatus() != null) {
      json.put("Status", checkOptions.getStatus().key);
    }
    if (extended && checkOptions.getServiceId() != null) {
      json.put("ServiceID", checkOptions.getServiceId());
    }
    return json;
  }

  @Override
  public ConsulClient deregisterCheck(String checkId, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.GET, "/v1/agent/check/deregister/" + urlEncode(checkId), null, resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient passCheck(String checkId, Handler<AsyncResult<Void>> resultHandler) {
    return passCheckWithNote(checkId, null, resultHandler);
  }

  @Override
  public ConsulClient passCheckWithNote(String checkId, String note, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.GET, "/v1/agent/check/pass/" + urlEncode(checkId), Query.of("note", note), resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient warnCheck(String checkId, Handler<AsyncResult<Void>> resultHandler) {
    return warnCheckWithNote(checkId, null, resultHandler);
  }

  @Override
  public ConsulClient warnCheckWithNote(String checkId, String note, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.GET, "/v1/agent/check/warn/" + urlEncode(checkId), Query.of("note", note), resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient failCheck(String checkId, Handler<AsyncResult<Void>> resultHandler) {
    return failCheckWithNote(checkId, null, resultHandler);
  }

  @Override
  public ConsulClient failCheckWithNote(String checkId, String note, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.GET, "/v1/agent/check/fail/" + urlEncode(checkId), Query.of("note", note), resultHandler).end();
    return this;
  }

  @Override
  public ConsulClient updateCheck(String checkId, CheckStatus status, Handler<AsyncResult<Void>> resultHandler) {
    return updateCheckWithNote(checkId, status, null, resultHandler);
  }

  @Override
  public ConsulClient updateCheckWithNote(String checkId, CheckStatus status, String note, Handler<AsyncResult<Void>> resultHandler) {
    JsonObject put = new JsonObject().put("Status", status.key);
    if (note != null) {
      put.put("Output", note);
    }
    requestVoid(HttpMethod.PUT, "/v1/agent/check/update/" + urlEncode(checkId), null, resultHandler)
      .end(put.encode());
    return this;
  }

  @Override
  public ConsulClient leaderStatus(Handler<AsyncResult<String>> resultHandler) {
    requestString(HttpMethod.GET, "/v1/status/leader", null, resultHandler, (leader, headers) ->
      leader.substring(1, leader.length() - 2))
      .end();
    return this;
  }

  @Override
  public ConsulClient peersStatus(Handler<AsyncResult<List<String>>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/status/peers", null, resultHandler, (arr, headers) -> arr.stream()
      .map(obj -> (String) obj)
      .collect(Collectors.toList())).end();
    return this;
  }

  @Override
  public ConsulClient createSession(Handler<AsyncResult<String>> idHandler) {
    createSessionWithOptions(null, idHandler);
    return this;
  }

  @Override
  public ConsulClient createSessionWithOptions(SessionOptions options, Handler<AsyncResult<String>> idHandler) {
    HttpClientRequest req = requestObject(HttpMethod.PUT, "/v1/session/create", null, idHandler, (obj, headers) -> obj.getString("ID"));
    if (options != null) {
      req.end(options.toJson().encode());
    } else {
      req.end();
    }
    return this;
  }

  @Override
  public ConsulClient infoSession(String id, Handler<AsyncResult<Session>> resultHandler) {
    return infoSessionWithOptions(id, null, resultHandler);
  }

  @Override
  public ConsulClient infoSessionWithOptions(String id, BlockingQueryOptions options, Handler<AsyncResult<Session>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/session/info/" + urlEncode(id), Query.of(options), resultHandler, (sessions, headers) -> {
      if (sessions.size() == 0) {
        throw new RuntimeException("Unknown session ID: " + id);
      } else {
        return SessionParser.parse(sessions.getJsonObject(0), Long.parseLong(headers.get(INDEX_HEADER)));
      }
    }).end();
    return this;
  }

  @Override
  public ConsulClient renewSession(String id, Handler<AsyncResult<Session>> resultHandler) {
    requestArray(HttpMethod.PUT, "/v1/session/renew/" + urlEncode(id), null, resultHandler, (arr, headers) ->
      SessionParser.parse(arr.getJsonObject(0))).end();
    return this;
  }

  @Override
  public ConsulClient listSessions(Handler<AsyncResult<SessionList>> resultHandler) {
    return listSessionsWithOptions(null, resultHandler);
  }

  @Override
  public ConsulClient listSessionsWithOptions(BlockingQueryOptions options, Handler<AsyncResult<SessionList>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/session/list", Query.of(options), resultHandler, (arr, headers) -> {
      List<Session> list = arr.stream().map(obj -> SessionParser.parse((JsonObject) obj)).collect(Collectors.toList());
      return new SessionList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient listNodeSessions(String nodeId, Handler<AsyncResult<SessionList>> resultHandler) {
    return listNodeSessionsWithOptions(nodeId, null, resultHandler);
  }

  @Override
  public ConsulClient listNodeSessionsWithOptions(String nodeId, BlockingQueryOptions options, Handler<AsyncResult<SessionList>> resultHandler) {
    requestArray(HttpMethod.GET, "/v1/session/node/" + urlEncode(nodeId), Query.of(options), resultHandler, (arr, headers) -> {
      List<Session> list = arr.stream().map(obj -> SessionParser.parse((JsonObject) obj)).collect(Collectors.toList());
      return new SessionList().setList(list).setIndex(Long.parseLong(headers.get(INDEX_HEADER)));
    }).end();
    return this;
  }

  @Override
  public ConsulClient destroySession(String id, Handler<AsyncResult<Void>> resultHandler) {
    requestVoid(HttpMethod.PUT, "/v1/session/destroy/" + urlEncode(id), null, resultHandler).end();
    return this;
  }

  @Override
  public void close() {
    httpClient.close();
  }

  private <T> HttpClientRequest requestArray(HttpMethod method, String path, Query query,
                                             Handler<AsyncResult<T>> resultHandler,
                                             BiFunction<JsonArray, MultiMap, T> mapper) {
    return request(DEFAULT_VALID_CODES, method, path, query, resultHandler, (buffer, headers) -> mapper.apply(buffer.toJsonArray(), headers));
  }

  private <T> HttpClientRequest requestObject(HttpMethod method, String path, Query query,
                                              Handler<AsyncResult<T>> resultHandler,
                                              BiFunction<JsonObject, MultiMap, T> mapper) {
    return request(DEFAULT_VALID_CODES, method, path, query, resultHandler, (buffer, headers) -> mapper.apply(buffer.toJsonObject(), headers));
  }

  private <T> HttpClientRequest requestString(HttpMethod method, String path, Query query,
                                              Handler<AsyncResult<T>> resultHandler,
                                              BiFunction<String, MultiMap, T> mapper) {
    return request(DEFAULT_VALID_CODES, method, path, query, resultHandler, (buffer, headers) -> mapper.apply(buffer.toString().trim(), headers));
  }

  private <T> HttpClientRequest requestVoid(HttpMethod method, String path, Query query,
                                            Handler<AsyncResult<T>> resultHandler) {
    return request(DEFAULT_VALID_CODES, method, path, query, resultHandler, (buffer, headers) -> null);
  }

  private <T> HttpClientRequest request(List<Integer> validCodes, HttpMethod method, String path, Query query,
                                        Handler<AsyncResult<T>> resultHandler,
                                        BiFunction<Buffer, MultiMap, T> mapper) {
    if (query == null) {
      query = new Query();
    }
    if (dc != null) {
      query.put("dc", dc);
    }
    HttpClientRequest rq = httpClient.request(method, path + query, h -> {
      if (validCodes.contains(h.statusCode())) {
        h.bodyHandler(bh -> {
          try {
            resultHandler.handle(Future.succeededFuture(mapper.apply(bh, h.headers())));
          } catch (Throwable throwable) {
            resultHandler.handle(Future.failedFuture(throwable));
          }
        }).exceptionHandler(e -> resultHandler.handle(Future.failedFuture(e)));
      } else {
        resultHandler.handle(Future.failedFuture(h.statusMessage()));
      }
    }).exceptionHandler(e -> resultHandler.handle(Future.failedFuture(e)));
    if (aclToken != null) {
      rq.putHeader(TOKEN_HEADER, aclToken);
    }
    if (timeoutMs > 0) {
      rq.setTimeout(timeoutMs);
    }
    return rq;
  }

}
