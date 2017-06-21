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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.impl.ConsulClientImpl;

import java.util.List;

/**
 * A Vert.x service used to interact with Consul.
 *
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@VertxGen
public interface ConsulClient {

  /**
   * Create a Consul client with default options.
   *
   * @param vertx  the Vert.x instance
   * @return the client
   */
  static ConsulClient create(Vertx vertx) {
    return new ConsulClientImpl(vertx, new ConsulClientOptions());
  }

  /**
   * Create a Consul client.
   *
   * @param vertx  the Vert.x instance
   * @param options the options
   * @return the client
   */
  static ConsulClient create(Vertx vertx, ConsulClientOptions options) {
    return new ConsulClientImpl(vertx, options);
  }

  /**
   * Returns the configuration and member information of the local agent
   *
   * @param resultHandler will be provided with the configuration and member information of the local agent
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_self">/v1/agent/self</a> endpoint
   */
  @Fluent
  ConsulClient agentInfo(Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Returns the LAN network coordinates for all nodes in a given DC
   *
   * @param resultHandler will be provided with network coordinates of nodes in datacenter
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/coordinate.html#coordinate_nodes">/v1/coordinate/nodes</a> endpoint
   */
  @Fluent
  ConsulClient coordinateNodes(Handler<AsyncResult<CoordinateList>> resultHandler);

  /**
   * Returns the LAN network coordinates for all nodes in a given DC
   * This is blocking query unlike {@link ConsulClient#coordinateNodes(Handler)}
   *
   * @param options       the blocking options
   * @param resultHandler will be provided with network coordinates of nodes in datacenter
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/coordinate.html#coordinate_nodes">/v1/coordinate/nodes</a> endpoint
   */
  @Fluent
  ConsulClient coordinateNodesWithOptions(BlockingQueryOptions options, Handler<AsyncResult<CoordinateList>> resultHandler);

  /**
   * Returns the WAN network coordinates for all Consul servers, organized by DCs
   *
   * @param resultHandler will be provided with network coordinates for all Consul servers
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/coordinate.html#coordinate_datacenters">/v1/coordinate/datacenters</a> endpoint
   */
  @Fluent
  ConsulClient coordinateDatacenters(Handler<AsyncResult<List<DcCoordinates>>> resultHandler);

  /**
   * Returns key/value pair that corresponding to the specified key
   *
   * @param key           the key
   * @param resultHandler will be provided with key/value pair
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient getValue(String key, Handler<AsyncResult<KeyValue>> resultHandler);

  /**
   * Returns key/value pair that corresponding to the specified key.
   * This is blocking query unlike {@link ConsulClient#getValue(String, Handler)}
   *
   * @param key           the key
   * @param options       the blocking options
   * @param resultHandler will be provided with key/value pair
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient getValueWithOptions(String key, BlockingQueryOptions options, Handler<AsyncResult<KeyValue>> resultHandler);

  /**
   * Remove the key/value pair that corresponding to the specified key
   *
   * @param key           the key
   * @param resultHandler will be called on complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient deleteValue(String key, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Returns the list of key/value pairs that corresponding to the specified key prefix.
   *
   * @param keyPrefix     the prefix
   * @param resultHandler will be provided with list of key/value pairs
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient getValues(String keyPrefix, Handler<AsyncResult<KeyValueList>> resultHandler);

  /**
   * Returns the tree of key/value pairs that corresponding to the specified key prefix.
   *
   * @param keyPrefix the prefix
   * @param resultHandler will be provided with tree of key/value pairs
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/api/kv.html">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient getValuesTree(String keyPrefix, Handler<AsyncResult<KeyValueTree>> resultHandler);

  /**
   * Returns the tree of key/value pairs that corresponding to the specified key prefix.
   *
   * @param keyPrefix the prefix
   * @param delimiter the delimiter
   * @param resultHandler will be provided with tree of key/value pairs
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/api/kv.html">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient getValuesTreeWithDelimiter(String keyPrefix, String delimiter, Handler<AsyncResult<KeyValueTree>> resultHandler);

  /**
   * Returns the list of key/value pairs that corresponding to the specified key prefix.
   * This is blocking query unlike {@link ConsulClient#getValues(String, Handler)}
   *
   * @param keyPrefix     the prefix
   * @param options       the blocking options
   * @param resultHandler will be provided with list of key/value pairs
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient getValuesWithOptions(String keyPrefix, BlockingQueryOptions options, Handler<AsyncResult<KeyValueList>> resultHandler);

  /**
   * Removes all the key/value pair that corresponding to the specified key prefix
   *
   * @param keyPrefix     the prefix
   * @param resultHandler will be called on complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient deleteValues(String keyPrefix, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Adds specified key/value pair
   *
   * @param key           the key
   * @param value         the value
   * @param resultHandler will be provided with success of operation
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient putValue(String key, String value, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * @param key           the key
   * @param value         the value
   * @param options       options used to push pair
   * @param resultHandler will be provided with success of operation
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#single">/v1/kv/&lt;key&gt;</a> endpoint
   */
  @Fluent
  ConsulClient putValueWithOptions(String key, String value, KeyValueOptions options, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Manages multiple operations inside a single, atomic transaction.
   *
   * @param request transaction request
   * @param resultHandler will be provided with result of transaction
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/kv.html#txn">/v1/txn</a> endpoint
   */
  @Fluent
  ConsulClient transaction(TxnRequest request, Handler<AsyncResult<TxnResponse>> resultHandler);

  /**
   * Create new Acl token
   *
   * @param token     properties of the token
   * @param idHandler will be provided with ID of created token
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/acl.html#acl_create">/v1/acl/create</a> endpoint
   */
  @Fluent
  ConsulClient createAclToken(AclToken token, Handler<AsyncResult<String>> idHandler);

  /**
   * Update Acl token
   *
   * @param token     properties of the token to be updated
   * @param idHandler will be provided with ID of updated
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/acl.html#acl_update">/v1/acl/update</a> endpoint
   */
  @Fluent
  ConsulClient updateAclToken(AclToken token, Handler<AsyncResult<String>> idHandler);

  /**
   * Clone Acl token
   *
   * @param id        the ID of token to be cloned
   * @param idHandler will be provided with ID of cloned token
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/acl.html#acl_clone">/v1/acl/clone/&lt;id&gt;</a> endpoint
   */
  @Fluent
  ConsulClient cloneAclToken(String id, Handler<AsyncResult<String>> idHandler);

  /**
   * Get list of Acl token
   *
   * @param resultHandler will be provided with list of tokens
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/acl.html#acl_list">/v1/acl/list</a> endpoint
   */
  @Fluent
  ConsulClient listAclTokens(Handler<AsyncResult<List<AclToken>>> resultHandler);

  /**
   * Get info of Acl token
   *
   * @param id           the ID of token
   * @param tokenHandler will be provided with token
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/acl.html#acl_info">/v1/acl/info/&lt;id&gt;</a> endpoint
   */
  @Fluent
  ConsulClient infoAclToken(String id, Handler<AsyncResult<AclToken>> tokenHandler);

  /**
   * Destroy Acl token
   *
   * @param id            the ID of token
   * @param resultHandler will be called on complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/acl.html#acl_destroy">/v1/acl/destroy/&lt;id&gt;</a> endpoint
   */
  @Fluent
  ConsulClient destroyAclToken(String id, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Fires a new user event
   *
   * @param name          name of event
   * @param resultHandler will be provided with properties of event
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/event.html#event_fire">/v1/event/fire/&lt;name&gt;</a> endpoint
   */
  @Fluent
  ConsulClient fireEvent(String name, Handler<AsyncResult<Event>> resultHandler);

  /**
   * Fires a new user event
   *
   * @param name          name of event
   * @param options       options used to create event
   * @param resultHandler will be provided with properties of event
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/event.html#event_fire">/v1/event/fire/&lt;name&gt;</a> endpoint
   */
  @Fluent
  ConsulClient fireEventWithOptions(String name, EventOptions options, Handler<AsyncResult<Event>> resultHandler);

  /**
   * Returns the most recent events known by the agent
   *
   * @param resultHandler will be provided with list of events
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/event.html#event_list">/v1/event/list</a> endpoint
   */
  @Fluent
  ConsulClient listEvents(Handler<AsyncResult<EventList>> resultHandler);

  /**
   * Returns the most recent events known by the agent.
   * This is blocking query unlike {@link ConsulClient#listEvents(Handler)}. However, the semantics of this endpoint
   * are slightly different. Most blocking queries provide a monotonic index and block until a newer index is available.
   * This can be supported as a consequence of the total ordering of the consensus protocol. With gossip,
   * there is no ordering, and instead {@code X-Consul-Index} maps to the newest event that matches the query.
   *
   * In practice, this means the index is only useful when used against a single agent and has no meaning globally.
   * Because Consul defines the index as being opaque, clients should not be expecting a natural ordering either.
   *
   * @param resultHandler will be provided with list of events
   * @param options       the blocking options
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/event.html#event_list">/v1/event/list</a> endpoint
   */
  @Fluent
  ConsulClient listEventsWithOptions(EventListOptions options, Handler<AsyncResult<EventList>> resultHandler);

  /**
   * Adds a new service, with an optional health check, to the local agent.
   *
   * @param serviceOptions the options of new service
   * @param resultHandler  will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_service_register">/v1/agent/service/register</a> endpoint
   * @see ServiceOptions
   */
  @Fluent
  ConsulClient registerService(ServiceOptions serviceOptions, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Places a given service into "maintenance mode"
   *
   * @param maintenanceOptions the maintenance options
   * @param resultHandler      will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_service_maintenance">/v1/agent/service/maintenance/&lt;serviceId&gt;</a> endpoint
   * @see MaintenanceOptions
   */
  @Fluent
  ConsulClient maintenanceService(MaintenanceOptions maintenanceOptions, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Remove a service from the local agent. The agent will take care of deregistering the service with the Catalog.
   * If there is an associated check, that is also deregistered.
   *
   * @param id            the ID of service
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_service_deregister">/v1/agent/service/deregister/&lt;serviceId&gt;</a> endpoint
   */
  @Fluent
  ConsulClient deregisterService(String id, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Returns the nodes providing a service
   *
   * @param service       name of service
   * @param resultHandler will be provided with list of nodes providing given service
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_service">/v1/catalog/service/&lt;service&gt;</a> endpoint
   */
  @Fluent
  ConsulClient catalogServiceNodes(String service, Handler<AsyncResult<ServiceList>> resultHandler);

  /**
   * Returns the nodes providing a service
   *
   * @param service       name of service
   * @param options       options used to request services
   * @param resultHandler will be provided with list of nodes providing given service
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_service">/v1/catalog/service/&lt;service&gt;</a> endpoint
   */
  @Fluent
  ConsulClient catalogServiceNodesWithOptions(String service, ServiceQueryOptions options, Handler<AsyncResult<ServiceList>> resultHandler);

  /**
   * Return all the datacenters that are known by the Consul server
   *
   * @param resultHandler will be provided with list of datacenters
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_datacenters">/v1/catalog/datacenters</a> endpoint
   */
  @Fluent
  ConsulClient catalogDatacenters(Handler<AsyncResult<List<String>>> resultHandler);

  /**
   * Returns the nodes registered in a datacenter
   *
   * @param resultHandler will be provided with list of nodes
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_nodes">/v1/catalog/nodes</a> endpoint
   */
  @Fluent
  ConsulClient catalogNodes(Handler<AsyncResult<NodeList>> resultHandler);

  /**
   * Returns the nodes registered in a datacenter
   *
   * @param resultHandler will be provided with list of nodes
   * @param options       options used to request nodes
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_nodes">/v1/catalog/nodes</a> endpoint
   */
  @Fluent
  ConsulClient catalogNodesWithOptions(NodeQueryOptions options, Handler<AsyncResult<NodeList>> resultHandler);

  /**
   * Returns the checks associated with the service
   *
   * @param service the service name
   * @param resultHandler will be provided with list of checks
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/health.html#health_checks">/v1/health/checks/&lt;service&gt;</a> endpoint
   */
  @Fluent
  ConsulClient healthChecks(String service, Handler<AsyncResult<CheckList>> resultHandler);

  /**
   * Returns the checks associated with the service
   *
   * @param service the service name
   * @param options       options used to request checks
   * @param resultHandler will be provided with list of checks
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/health.html#health_checks">/v1/health/checks/&lt;service&gt;</a> endpoint
   */
  @Fluent
  ConsulClient healthChecksWithOptions(String service, CheckQueryOptions options, Handler<AsyncResult<CheckList>> resultHandler);

  /**
   * Returns the checks in the specified status
   *
   * @param checkStatus the check status
   * @param resultHandler will be provided with list of checks
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/api/health.html#list-checks-in-state">/v1/health/state/&lt;state&gt;</a> endpoint
   */
  @Fluent
  ConsulClient healthState(CheckStatus checkStatus, Handler<AsyncResult<CheckList>> resultHandler);

  /**
   * Returns the checks in the specified status
   *
   * @param checkStatus the check status
   * @param options       options used to request checks
   * @param resultHandler will be provided with list of checks
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/api/health.html#list-checks-in-state">/v1/health/state/&lt;state&gt;</a> endpoint
   */
  @Fluent
  ConsulClient healthStateWithOptions(CheckStatus checkStatus, CheckQueryOptions options, Handler<AsyncResult<CheckList>> resultHandler);

  /**
   * Returns the nodes providing the service. This endpoint is very similar to the {@link ConsulClient#catalogServiceNodes} endpoint;
   * however, this endpoint automatically returns the status of the associated health check as well as any system level health checks.
   *
   * @param service the service name
   * @param passing if true, filter results to only nodes with all checks in the passing state
   * @param resultHandler will be provided with list of services
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/health.html#health_service">/v1/health/service/&lt;service&gt;</a> endpoint
   */
  @Fluent
  ConsulClient healthServiceNodes(String service, boolean passing, Handler<AsyncResult<ServiceEntryList>> resultHandler);

  /**
   * Returns the nodes providing the service. This endpoint is very similar to the {@link ConsulClient#catalogServiceNodesWithOptions} endpoint;
   * however, this endpoint automatically returns the status of the associated health check as well as any system level health checks.
   *
   * @param service the service name
   * @param passing if true, filter results to only nodes with all checks in the passing state
   * @param options options used to request services
   * @param resultHandler will be provided with list of services
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/health.html#health_service">/v1/health/service/&lt;service&gt;</a> endpoint
   */
  @Fluent
  ConsulClient healthServiceNodesWithOptions(String service, boolean passing, ServiceQueryOptions options, Handler<AsyncResult<ServiceEntryList>> resultHandler);

  /**
   * Returns the services registered in a datacenter
   *
   * @param resultHandler will be provided with list of services
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_services">/v1/catalog/services</a> endpoint
   */
  @Fluent
  ConsulClient catalogServices(Handler<AsyncResult<ServiceList>> resultHandler);

  /**
   * Returns the services registered in a datacenter
   * This is blocking query unlike {@link ConsulClient#catalogServices(Handler)}
   *
   * @param resultHandler will be provided with list of services
   * @param options       the blocking options
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_services">/v1/catalog/services</a> endpoint
   */
  @Fluent
  ConsulClient catalogServicesWithOptions(BlockingQueryOptions options, Handler<AsyncResult<ServiceList>> resultHandler);

  /**
   * Returns the node's registered services
   *
   * @param node          node name
   * @param resultHandler will be provided with list of services
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_node">/v1/catalog/node/&lt;node&gt;</a> endpoint
   */
  @Fluent
  ConsulClient catalogNodeServices(String node, Handler<AsyncResult<ServiceList>> resultHandler);

  /**
   * Returns the node's registered services
   * This is blocking query unlike {@link ConsulClient#catalogNodeServices(String, Handler)}
   *
   * @param node          node name
   * @param options       the blocking options
   * @param resultHandler will be provided with list of services
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/catalog.html#catalog_node">/v1/catalog/node/&lt;node&gt;</a> endpoint
   */
  @Fluent
  ConsulClient catalogNodeServicesWithOptions(String node, BlockingQueryOptions options, Handler<AsyncResult<ServiceList>> resultHandler);

  /**
   * Returns list of services registered with the local agent.
   *
   * @param resultHandler will be provided with list of services
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_services">/v1/agent/services</a> endpoint
   */
  @Fluent
  ConsulClient localServices(Handler<AsyncResult<List<Service>>> resultHandler);

  /**
   * Return all the checks that are registered with the local agent.
   *
   * @param resultHandler will be provided with list of checks
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_checks">/v1/agent/checks</a> endpoint
   */
  @Fluent
  ConsulClient localChecks(Handler<AsyncResult<List<Check>>> resultHandler);

  /**
   * Add a new check to the local agent. The agent is responsible for managing the status of the check
   * and keeping the Catalog in sync.
   *
   * @param checkOptions  options used to register new check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_register">/v1/agent/check/register</a> endpoint
   */
  @Fluent
  ConsulClient registerCheck(CheckOptions checkOptions, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Remove a check from the local agent. The agent will take care of deregistering the check from the Catalog.
   *
   * @param checkId       the ID of check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_deregister">/v1/agent/check/deregister/&lt;checkId&gt;</a> endpoint
   */
  @Fluent
  ConsulClient deregisterCheck(String checkId, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to "passing". Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_pass">/v1/agent/check/pass/&lt;checkId&gt;</a> endpoint
   * @see CheckStatus
   */
  @Fluent
  ConsulClient passCheck(String checkId, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to "passing". Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param note          a human-readable message with the status of the check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_pass">/v1/agent/check/pass/&lt;checkId&gt;</a> endpoint
   * @see CheckStatus
   */
  @Fluent
  ConsulClient passCheckWithNote(String checkId, String note, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to "warning". Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_warn">/v1/agent/check/warn/&lt;checkId&gt;</a> endpoint
   * @see CheckStatus
   */
  @Fluent
  ConsulClient warnCheck(String checkId, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to "warning". Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param note          a human-readable message with the status of the check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_warn">/v1/agent/check/warn/&lt;checkId&gt;</a> endpoint
   * @see CheckStatus
   */
  @Fluent
  ConsulClient warnCheckWithNote(String checkId, String note, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to "critical". Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_fail">/v1/agent/check/fail/&lt;checkId&gt;</a> endpoint
   * @see CheckStatus
   */
  @Fluent
  ConsulClient failCheck(String checkId, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to "critical". Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param note          a human-readable message with the status of the check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_fail">/v1/agent/check/fail/&lt;checkId&gt;</a> endpoint
   * @see CheckStatus
   */
  @Fluent
  ConsulClient failCheckWithNote(String checkId, String note, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to given status. Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param status        new status of check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_update">/v1/agent/check/update/&lt;checkId&gt;</a> endpoint
   */
  @Fluent
  ConsulClient updateCheck(String checkId, CheckStatus status, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Set status of the check to given status. Used with a check that is of the TTL type. The TTL clock will be reset.
   *
   * @param checkId       the ID of check
   * @param status        new status of check
   * @param note          a human-readable message with the status of the check
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/agent.html#agent_check_update">/v1/agent/check/update/&lt;checkId&gt;</a> endpoint
   */
  @Fluent
  ConsulClient updateCheckWithNote(String checkId, CheckStatus status, String note, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Get the Raft leader for the datacenter in which the agent is running.
   * It returns an address in format "<code>10.1.10.12:8300</code>"
   *
   * @param resultHandler will be provided with address of cluster leader
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/status.html#status_leader">/v1/status/leader</a> endpoint
   */
  @Fluent
  ConsulClient leaderStatus(Handler<AsyncResult<String>> resultHandler);

  /**
   * Retrieves the Raft peers for the datacenter in which the the agent is running.
   * It returns a list of addresses "<code>10.1.10.12:8300</code>", "<code>10.1.10.13:8300</code>"
   *
   * @param resultHandler will be provided with list of peers
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/status.html#status_peers">/v1/status/peers</a> endpoint
   */
  @Fluent
  ConsulClient peersStatus(Handler<AsyncResult<List<String>>> resultHandler);

  /**
   * Initialize a new session
   *
   * @param idHandler will be provided with ID of new session
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_create">/v1/session/create</a> endpoint
   */
  @Fluent
  ConsulClient createSession(Handler<AsyncResult<String>> idHandler);

  /**
   * Initialize a new session
   *
   * @param options   options used to create session
   * @param idHandler will be provided with ID of new session
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_create">/v1/session/create</a> endpoint
   */
  @Fluent
  ConsulClient createSessionWithOptions(SessionOptions options, Handler<AsyncResult<String>> idHandler);

  /**
   * Returns the requested session information
   *
   * @param id            the ID of requested session
   * @param resultHandler will be provided with info of requested session
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_info">/v1/session/info/&lt;session&gt;</a> endpoint
   */
  @Fluent
  ConsulClient infoSession(String id, Handler<AsyncResult<Session>> resultHandler);

  /**
   * Returns the requested session information
   * This is blocking query unlike {@link ConsulClient#infoSession(String, Handler)}
   *
   * @param id            the ID of requested session
   * @param options       the blocking options
   * @param resultHandler will be provided with info of requested session
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_info">/v1/session/info/&lt;session&gt;</a> endpoint
   */
  @Fluent
  ConsulClient infoSessionWithOptions(String id, BlockingQueryOptions options, Handler<AsyncResult<Session>> resultHandler);

  /**
   * Renews the given session. This is used with sessions that have a TTL, and it extends the expiration by the TTL
   *
   * @param id            the ID of session that should be renewed
   * @param resultHandler will be provided with info of renewed session
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_renew">/v1/session/renew/&lt;session&gt;</a> endpoint
   */
  @Fluent
  ConsulClient renewSession(String id, Handler<AsyncResult<Session>> resultHandler);

  /**
   * Returns the active sessions
   *
   * @param resultHandler will be provided with list of sessions
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_list">/v1/session/list</a> endpoint
   */
  @Fluent
  ConsulClient listSessions(Handler<AsyncResult<SessionList>> resultHandler);

  /**
   * Returns the active sessions
   * This is blocking query unlike {@link ConsulClient#listSessions(Handler)}
   *
   * @param options       the blocking options
   * @param resultHandler will be provided with list of sessions
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_list">/v1/session/list</a> endpoint
   */
  @Fluent
  ConsulClient listSessionsWithOptions(BlockingQueryOptions options, Handler<AsyncResult<SessionList>> resultHandler);

  /**
   * Returns the active sessions for a given node
   *
   * @param nodeId        the ID of node
   * @param resultHandler will be provided with list of sessions
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_node">/v1/session/node/&lt;node&gt;</a> endpoint
   */
  @Fluent
  ConsulClient listNodeSessions(String nodeId, Handler<AsyncResult<SessionList>> resultHandler);

  /**
   * Returns the active sessions for a given node
   * This is blocking query unlike {@link ConsulClient#listNodeSessions(String, Handler)}
   *
   * @param nodeId        the ID of node
   * @param options       the blocking options
   * @param resultHandler will be provided with list of sessions
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_node">/v1/session/node/&lt;node&gt;</a> endpoint
   */
  @Fluent
  ConsulClient listNodeSessionsWithOptions(String nodeId, BlockingQueryOptions options, Handler<AsyncResult<SessionList>> resultHandler);

  /**
   * Destroys the given session
   *
   * @param id            the ID of session
   * @param resultHandler will be called when complete
   * @return reference to this, for fluency
   * @see <a href="https://www.consul.io/docs/agent/http/session.html#session_destroy">/v1/session/destroy/&lt;session&gt;</a> endpoint
   */
  @Fluent
  ConsulClient destroySession(String id, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Close the client and release its resources
   */
  void close();
}
