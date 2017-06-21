package io.vertx.kotlin.ext.consul

import io.vertx.ext.consul.KeyValueTree

/**
 * A function providing a DSL for building [io.vertx.ext.consul.KeyValueTree] objects.
 *
 * Holds result of key/value pairs query as 
 *
 * @param index  Set Consul index
 * @param tree  Set tree of key/value pairs
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.consul.KeyValueTree original] using Vert.x codegen.
 */
fun KeyValueTree(
  index: Long? = null,
  tree: io.vertx.core.json.JsonObject? = null): KeyValueTree = io.vertx.ext.consul.KeyValueTree().apply {

  if (index != null) {
    this.setIndex(index)
  }
  if (tree != null) {
    this.setTree(tree)
  }
}

