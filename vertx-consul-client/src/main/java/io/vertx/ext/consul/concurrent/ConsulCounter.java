package io.vertx.ext.consul.concurrent;

import io.vertx.core.*;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.KeyValue;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class ConsulCounter implements Counter {

  private final ConsulClient client;
  private final String key;
  private final LockFactory factory;

  ConsulCounter(Vertx vertx, ConsulClient client, String key) {
    this.client = client;
    this.key = key;
    this.factory = new LockFactory(vertx, client, key + "/lock");
  }

  @Override
  public void get(Handler<AsyncResult<Long>> resultHandler) {
    client.getValue(key, handleKV(resultHandler));
  }

  @Override
  public void incrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
    addAndGet(1L, resultHandler);
  }

  @Override
  public void getAndIncrement(Handler<AsyncResult<Long>> resultHandler) {
    getAndAdd(1L, resultHandler);
  }

  @Override
  public void decrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
    addAndGet(-1L, resultHandler);
  }

  @Override
  public void addAndGet(long value, Handler<AsyncResult<Long>> resultHandler) {
    lockAndGetAndAdd(value, h -> resultHandler.handle(h.map(l -> l + value)));
  }

  @Override
  public void getAndAdd(long value, Handler<AsyncResult<Long>> resultHandler) {
    lockAndGetAndAdd(value, resultHandler);
  }

  private void lockAndGetAndAdd(long value, Handler<AsyncResult<Long>> resultHandler) {
    factory.acquire(Long.MAX_VALUE, lh -> {
      if (lh.succeeded()) {
        Lock lock = lh.result();
        client.getValue(key, handleKV(actual -> {
          if (actual.succeeded()) {
            client.putValue(key, Long.toString(actual.result() + value), updated -> {
              lock.release();
              if (updated.succeeded()) {
                resultHandler.handle(Future.succeededFuture(actual.result()));
              } else {
                resultHandler.handle(Future.failedFuture(updated.cause()));
              }
            });
          } else {
            lock.release();
            resultHandler.handle(Future.failedFuture(actual.cause()));
          }
        }));
      } else {
        resultHandler.handle(Future.failedFuture(lh.cause()));
      }
    });
  }

  @Override
  public void compareAndSet(long expected, long value, Handler<AsyncResult<Boolean>> resultHandler) {
    factory.acquire(Long.MAX_VALUE, lh -> {
      if (lh.succeeded()) {
        client.getValue(key, handleKV(actual -> {
          if (actual.succeeded()) {
            if (actual.result() == expected) {
              client.putValue(key, Long.toString(value), updated -> {
                lh.result().release();
                if (updated.succeeded()) {
                  resultHandler.handle(Future.succeededFuture(true));
                } else {
                  resultHandler.handle(Future.failedFuture(updated.cause()));
                }
              });
            } else {
              lh.result().release();
              resultHandler.handle(Future.succeededFuture(false));
            }
          } else {
            lh.result().release();
            resultHandler.handle(Future.failedFuture(lh.cause()));
          }
        }));
      } else {
        resultHandler.handle(Future.failedFuture(lh.cause()));
      }
    });
  }

  private static Handler<AsyncResult<KeyValue>> handleKV(Handler<AsyncResult<Long>> resultHandler) {
    return kv -> {
      if (kv.succeeded() && kv.result().isPresent()) {
        long value;
        try {
          value = Long.parseLong(kv.result().getValue());
        } catch (Exception e) {
          resultHandler.handle(Future.failedFuture(new VertxException(e)));
          return;
        }
        resultHandler.handle(Future.succeededFuture(value));
      } else {
        resultHandler.handle(Future.succeededFuture(0L));
      }
    };
  }
}
