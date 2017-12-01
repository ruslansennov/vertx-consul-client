package io.vertx.ext.consul.concurrent;

import io.vertx.core.*;
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.KeyValueOptions;
import io.vertx.ext.consul.SessionBehavior;
import io.vertx.ext.consul.SessionOptions;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
class LockFactory {

  private static final long DELAY_STEP = 10;
  private static final long SESSION_TTL = 100;
  private static final VertxException TIMEOUT_EXCEPTION = new VertxException("Timed out waiting to get lock");

  private final Vertx vertx;
  private final ConsulClient client;
  private final String key;
  private final LockSession session;
  private final Queue<LockWaiter> waiters = new LinkedList<>();

  private LockWaiter current;

  LockFactory(Vertx vertx, ConsulClient client, String key) {
    this.vertx = vertx;
    this.client = client;
    this.key = key;
    this.session = new LockSession(vertx, client);
  }

  void acquire(long timeout, Handler<AsyncResult<Lock>> resultHandler) {
    synchronized (this) {
      LockWaiter waiter = new LockWaiter(vertx, timeout, resultHandler);
      if (current == null) {
        current = waiter;
        current.acquire();
        acquire();
      } else {
        waiters.add(waiter);
      }
    }
  }

  private void acquire() {
    session.get(sid -> {
      if (sid.succeeded()) {
        LockHunter hunter = new LockHunter(vertx, client, key);
        hunter.acquire(sid.result(), current.wait, ac -> {
          if (ac.succeeded()) {
            current.resultHandler.handle(Future.succeededFuture(() -> release(session)));
          } else {
            release(session);
          }
        });
      } else {
        release(session);
      }
    });
  }

  private void release(LockSession session) {
    client.deleteValue(key, kd -> {
      current = pollWaiters();
      if (current != null) {
        acquire();
      }
    });
  }

  private LockWaiter pollWaiters() {
    while (true) {
      LockWaiter waiter = waiters.poll();
      if (waiter == null) {
        return null;
      } else if (waiter.acquire()) {
        return waiter;
      }
    }
  }

  private static class LockWaiter {

    private final Object lock = new Object();
    private final Vertx vertx;

    final long wait;
    final Handler<AsyncResult<Lock>> resultHandler;

    private boolean timedOut;
    private boolean acquired;

    LockWaiter(Vertx vertx, long wait, Handler<AsyncResult<Lock>> resultHandler) {
      this.vertx = vertx;
      this.wait = wait;
      this.resultHandler = resultHandler;
      if (wait != Long.MAX_VALUE) {
        vertx.setTimer(wait, tid -> timedOut());
      }
    }

    private void timedOut() {
      synchronized (lock) {
        if (!acquired) {
          timedOut = true;
          vertx.runOnContext(v -> resultHandler.handle(Future.failedFuture(TIMEOUT_EXCEPTION)));
        }
      }
    }

    boolean acquire() {
      synchronized (lock) {
        if (!timedOut) {
          acquired = true;
        }
      }
      return acquired;
    }
  }

  private static class LockHunter {

    private final Vertx vertx;
    private final ConsulClient client;
    private final String key;

    private boolean timedOut;

    LockHunter(Vertx vertx, ConsulClient client, String key) {
      this.vertx = vertx;
      this.client = client;
      this.key = key;
    }

    void acquire(String sessionId, long wait, Handler<AsyncResult<Void>> resultHandler) {
      KeyValueOptions opt = new KeyValueOptions().setAcquireSession(sessionId);
      vertx.setTimer(wait, d -> timedOut = true);
      client.putValueWithOptions(key, "*", opt, bh -> {
        if (bh.succeeded()) {
          if (bh.result()) {
            resultHandler.handle(Future.succeededFuture());
          } else {
            if (timedOut) {
              resultHandler.handle(Future.failedFuture(TIMEOUT_EXCEPTION));;
            } else {
              vertx.setTimer(DELAY_STEP, l -> acquire(sessionId, wait, resultHandler));
            }
          }
        } else {
          resultHandler.handle(Future.failedFuture(bh.cause()));
        }
      });
    }
  }

  private static class LockSession {

    private final Vertx vertx;
    private final ConsulClient client;

    private long periodic;
    private String sessionId;

    LockSession(Vertx vertx, ConsulClient client) {
      this.vertx = vertx;
      this.client = client;
    }

    void get(Handler<AsyncResult<String>> idHandler) {
      if (sessionId != null) {
        idHandler.handle(Future.succeededFuture(sessionId));
        return;
      }
      SessionOptions opt = new SessionOptions()
        .setTtl(SESSION_TTL)
        .setBehavior(SessionBehavior.DELETE);
      client.createSessionWithOptions(opt, idh -> {
        if (idh.succeeded()) {
          sessionId = idh.result();
          periodic = vertx.setPeriodic(2 * TimeUnit.SECONDS.toMillis(SESSION_TTL) / 3, p ->
            client.renewSession(sessionId, renewed -> {
              if (renewed.failed()) {
                vertx.cancelTimer(periodic);
              }
            }));
          idHandler.handle(Future.succeededFuture(sessionId));
        } else {
          idHandler.handle(Future.failedFuture(idh.cause()));
        }
      });
    }
  }
}
