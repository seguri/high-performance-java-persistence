package com.vladmihalcea.hpjp.hibernate.concurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.vladmihalcea.hpjp.util.AbstractTest;
import com.vladmihalcea.hpjp.util.providers.Database;
import jakarta.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.cfg.AvailableSettings;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class LockModePessimisticReadWriteTest extends AbstractTest {

  public static final int WAIT_MILLIS = 500;

  private interface LockRequestCallable {
    void lock(Session session, Post post);
  }

  private final CountDownLatch endLatch = new CountDownLatch(1);

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class, PostComment.class};
  }

  @Override
  protected Database database() {
    return Database.ORACLE;
  }

  @Override
  public void afterInit() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setId(1L);
          post.setTitle("High-Performance Java Persistence");
          entityManager.persist(post);
        });
  }

  private void testPessimisticLocking(
      LockRequestCallable primaryLockRequestCallable,
      LockRequestCallable secondaryLockRequestCallable) {
    doInJPA(
        entityManager -> {
          try {
            Session session = entityManager.unwrap(Session.class);
            Post post = entityManager.find(Post.class, 1L);
            primaryLockRequestCallable.lock(session, post);
            executeAsync(
                () -> {
                  doInJPA(
                      _entityManager -> {
                        Session _session = _entityManager.unwrap(Session.class);
                        Post _post = _entityManager.find(Post.class, 1L);
                        secondaryLockRequestCallable.lock(_session, _post);
                      });
                },
                endLatch::countDown);
            sleep(WAIT_MILLIS);
          } catch (StaleObjectStateException e) {
            LOGGER.info("Optimistic locking failure: ", e);
          }
        });
    awaitOnLatch(endLatch);
  }

  @Test
  public void testPessimisticRead() {
    LOGGER.info("Test PESSIMISTIC_READ");
    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L, LockModeType.PESSIMISTIC_READ);
        });
  }

  @Test
  public void testPessimisticWrite() {
    LOGGER.info("Test PESSIMISTIC_WRITE");
    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        });
  }

  @Test
  public void testPessimisticWriteAfterFetch() {
    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L);
          entityManager.lock(post, LockModeType.PESSIMISTIC_WRITE);
        });
  }

  @Test
  public void testPessimisticWriteAfterFetchWithDetachedForJPA() {
    Post post =
        doInJPA(
            entityManager -> {
              return entityManager.find(Post.class, 1L);
            });
    try {
      doInJPA(
          entityManager -> {
            entityManager.lock(post, LockModeType.PESSIMISTIC_WRITE);
          });
    } catch (IllegalArgumentException e) {
      assertEquals("entity not in the persistence context", e.getMessage());
    }
  }

  @Test
  public void testPessimisticWriteAfterFetchWithDetachedForHibernate() {
    Post post =
        doInJPA(
            entityManager -> {
              return entityManager.find(Post.class, 1L);
            });
    doInJPA(
        entityManager -> {
          LOGGER.info("Lock and reattach");
          entityManager
              .unwrap(Session.class)
              .buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE))
              .lock(post);
          post.setTitle("High-Performance Hibernate");
        });
  }

  @Test
  public void testPessimisticReadDoesNotBlockPessimisticRead() throws InterruptedException {
    LOGGER.info("Test PESSIMISTIC_READ doesn't block PESSIMISTIC_READ");
    testPessimisticLocking(
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(post);
          LOGGER.info("PESSIMISTIC_READ acquired");
        },
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(post);
          LOGGER.info("PESSIMISTIC_READ acquired");
        });
  }

  @Test
  public void testPessimisticReadBlocksUpdate() throws InterruptedException {
    LOGGER.info("Test PESSIMISTIC_READ blocks UPDATE");
    testPessimisticLocking(
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(post);
          LOGGER.info("PESSIMISTIC_READ acquired");
        },
        (session, post) -> {
          post.setTitle("High-Performance Java Persistence 2nd edition");
          session.flush();
          LOGGER.info("Implicit lock acquired");
        });
  }

  @Test
  public void testPessimisticReadWithPessimisticWriteNoWait() throws InterruptedException {
    LOGGER.info("Test PESSIMISTIC_READ blocks PESSIMISTIC_WRITE, NO WAIT fails fast");
    testPessimisticLocking(
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(post);
          LOGGER.info("PESSIMISTIC_READ acquired");
        },
        (session, post) -> {
          session
              .buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE))
              .setTimeOut(Session.LockRequest.PESSIMISTIC_NO_WAIT)
              .lock(post);
          LOGGER.info("PESSIMISTIC_WRITE acquired");
        });
  }

  @Test
  public void testPessimisticWriteBlocksPessimisticRead() throws InterruptedException {
    LOGGER.info("Test PESSIMISTIC_WRITE blocks PESSIMISTIC_READ");
    testPessimisticLocking(
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(post);
          LOGGER.info("PESSIMISTIC_WRITE acquired");
        },
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(post);
          LOGGER.info("PESSIMISTIC_READ acquired");
        });
  }

  @Test
  public void testPessimisticWriteBlocksPessimisticWrite() throws InterruptedException {
    LOGGER.info("Test PESSIMISTIC_WRITE blocks PESSIMISTIC_WRITE");
    testPessimisticLocking(
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(post);
          LOGGER.info("PESSIMISTIC_WRITE acquired");
        },
        (session, post) -> {
          session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(post);
          LOGGER.info("PESSIMISTIC_WRITE acquired");
        });
  }

  @Test
  public void testPessimisticNoWait() {
    LOGGER.info("Test PESSIMISTIC_READ blocks PESSIMISTIC_WRITE, NO WAIT fails fast");

    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L, LockModeType.PESSIMISTIC_WRITE);

          executeSync(
              () ->
                  doInJPA(
                      _entityManager -> {
                        try {
                          Post _post =
                              _entityManager.find(
                                  Post.class,
                                  1L,
                                  LockModeType.PESSIMISTIC_WRITE,
                                  Collections.singletonMap(
                                      AvailableSettings.JAKARTA_LOCK_TIMEOUT, LockOptions.NO_WAIT));
                          fail("Should throw PessimisticEntityLockException");
                        } catch (LockTimeoutException expected) {
                          // This is expected since the first transaction already acquired this lock
                        }
                      }));
        });
  }

  @Test
  public void testPessimisticNoWaitJPA() throws InterruptedException {
    LOGGER.info("Test PESSIMISTIC_READ blocks PESSIMISTIC_WRITE, NO WAIT fails fast");
    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L);
          entityManager.lock(
              post,
              LockModeType.PESSIMISTIC_WRITE,
              Collections.singletonMap("jakarta.persistence.lock.timeout", 0));
        });
  }

  @Test
  public void testPessimisticTimeout() throws InterruptedException {
    doInJPA(
        entityManager -> {
          Post post = entityManager.getReference(Post.class, 1L);

          entityManager
              .unwrap(Session.class)
              .buildLockRequest(
                  new LockOptions(LockMode.PESSIMISTIC_WRITE)
                      .setTimeOut((int) TimeUnit.SECONDS.toMillis(3)))
              .lock(post);
        });
  }

  @Test
  public void testPessimisticTimeoutJPA() throws InterruptedException {
    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L);
          entityManager.lock(
              post,
              LockModeType.PESSIMISTIC_WRITE,
              Collections.singletonMap(
                  "jakarta.persistence.lock.timeout", TimeUnit.SECONDS.toMillis(3)));
        });
  }

  @Test
  public void testPessimisticWriteQuery() throws InterruptedException {
    doInJPA(
        entityManager -> {
          List<PostComment> comments =
              entityManager
                  .createQuery(
                      "select pc " + "from PostComment pc " + "join fetch pc.post p ",
                      PostComment.class)
                  .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                  .setHint("jakarta.persistence.lock.timeout", 0)
                  .getResultList();
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id private Long id;

    private String title;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }
  }

  @Entity(name = "PostComment")
  @Table(name = "post_comment")
  public static class PostComment {

    @Id private Long id;

    private String review;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getReview() {
      return review;
    }

    public void setReview(String review) {
      this.review = review;
    }

    public Post getPost() {
      return post;
    }

    public void setPost(Post post) {
      this.post = post;
    }
  }
}
