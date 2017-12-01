package io.vertx.ext.consul.concurrent;

import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulTestBase;
import io.vertx.ext.consul.dc.ConsulAgent;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@RunWith(VertxUnitRunner.class)
public class LockFactoryTest extends ConsulTestBase {

  @Test
  public void concurrentClients(TestContext tc) {
    vertx.<ConsulAgent>executeBlocking(a -> a.complete(ctx.attachAgent("concurrent-node")), tc.asyncAssertSuccess(agent2 -> {
      ConsulClient client2 = ConsulClient.create(vertx, ctx.writeClientOptions().setPort(agent2.getHttpPort()));
      String key = randomFooBarAlpha();
      LockFactory cal1 = new LockFactory(vertx, ctx.writeClient(), key);
      LockFactory cal2 = new LockFactory(vertx, client2, key);
      cal1.acquire(1000, tc.asyncAssertSuccess(lock1 -> {
        cal2.acquire(10000, tc.asyncAssertSuccess(lock2 -> {
          vertx.executeBlocking(d -> {
            client2.close();
            ctx.detachAgent(agent2);
            d.complete();
          }, tc.asyncAssertSuccess());
        }));
        vertx.setTimer(1000, t -> {
          lock1.release();
        });
      }));
    }));
  }

}
