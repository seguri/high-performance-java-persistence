package com.vladmihalcea.hpjp.hibernate.query.timeout;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.vladmihalcea.hpjp.util.AbstractSQLServerIntegrationTest;
import com.vladmihalcea.hpjp.util.exception.ExceptionUtil;
import jakarta.persistence.*;
import java.util.List;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class SQLServerLockTimeoutTest extends AbstractSQLServerIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Test
  public void testLockTimeout() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setTitle("High-Performance Java Persistence");

          entityManager.persist(post);
        });

    doInJPA(
        entityManager -> {
          executeStatement(entityManager, "SET LOCK_TIMEOUT 1000");

          List<Post> posts =
              entityManager
                  .createQuery("SELECT p from Post p")
                  .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                  .getResultList();

          doInJPA(
              _entityManager -> {
                LOGGER.info("Start waiting");
                executeStatement(_entityManager, "SET LOCK_TIMEOUT 1000");

                try {
                  List<Post> posts_ =
                      _entityManager
                          .createQuery("SELECT p from Post p")
                          .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                          .getResultList();

                  fail("Should have thrown a lock acquisition timeout!");
                } catch (Exception e) {
                  LOGGER.info("Timeout triggered", e);
                  assertTrue(ExceptionUtil.isLockTimeout(e));
                }

                LOGGER.info("Done waiting");
              });
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id @GeneratedValue private Integer id;

    private String title;

    public Integer getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public Post setTitle(String title) {
      this.title = title;
      return this;
    }
  }
}
