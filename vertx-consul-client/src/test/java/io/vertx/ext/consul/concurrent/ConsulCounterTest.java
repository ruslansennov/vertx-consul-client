package io.vertx.ext.consul.concurrent;

import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.consul.ConsulTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConsulCounterTest extends ConsulTestBase {

  private ConsulCounter cnt;

  @Before
  public void init() {
    cnt = new ConsulCounter(vertx, ctx.writeClient(), randomFooBarAlpha());
  }

  @Test
  public void incrementAndGet(TestContext tc) {
    cnt.incrementAndGet(tc.asyncAssertSuccess(l1 -> {
      tc.assertEquals(l1, 1L);
    }));
  }

  @Test
  public void concurrent(TestContext tc) {
    int n = 10000;
    Async async = tc.async(n);
    long tm = System.currentTimeMillis();
    ConcurrentHashSet<Long> set = new ConcurrentHashSet<>();
    for (int i = 0; i < n; i++) {
      cnt.incrementAndGet(c -> {
        if (c.succeeded()) {
          set.add(c.result());
          System.out.println(c.result());
        }
        async.countDown();
      });
    }
    async.await();
    tc.assertEquals(n, set.size());
    System.out.println(1.0 * (System.currentTimeMillis() - tm) / n);
  }
}
