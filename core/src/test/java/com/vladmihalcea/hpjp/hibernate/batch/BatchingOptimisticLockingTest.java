package com.vladmihalcea.hpjp.hibernate.batch;

import static org.junit.Assert.assertTrue;

import com.vladmihalcea.hpjp.util.AbstractTest;
import com.vladmihalcea.hpjp.util.exception.ExceptionUtil;
import com.vladmihalcea.hpjp.util.providers.Database;
import jakarta.persistence.*;
import java.util.List;
import java.util.Properties;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class BatchingOptimisticLockingTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Override
  protected Database database() {
    return Database.POSTGRESQL;
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.put("hibernate.jdbc.batch_size", "5");
    properties.put("hibernate.order_inserts", "true");
    properties.put("hibernate.order_updates", "true");
  }

  @Test
  public void testOptimsticLOcking() {
    doInJPA(
        entityManager -> {
          for (int i = 1; i <= 3; i++) {
            entityManager.persist(new Post().setTitle(String.format("Post no. %d", i)));
          }
        });

    try {
      doInJPA(
          entityManager -> {
            List<Post> posts =
                entityManager
                    .createQuery(
                        """
                    select p
                    from Post p
                    """,
                        Post.class)
                    .getResultList();

            posts.forEach(post -> post.setTitle(post.getTitle() + " - 2nd edition"));

            executeSync(
                () ->
                    doInJPA(
                        _entityManager -> {
                          Post post =
                              _entityManager
                                  .createQuery(
                                      """
                            select p
                            from Post p
                            order by p.id
                            """,
                                      Post.class)
                                  .setMaxResults(1)
                                  .getSingleResult();

                          post.setTitle(post.getTitle() + " - corrected");
                        }));
          });
    } catch (Exception e) {
      assertTrue(
          ExceptionUtil.rootCause(e)
              .getMessage()
              .startsWith(
                  "Batch update returned unexpected row count from update [0]; "
                      + "actual row count: 0; "
                      + "expected: 1; "
                      + "statement executed:"));
    }
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;

    @Version private short version;

    public Long getId() {
      return id;
    }

    public Post setId(Long id) {
      this.id = id;
      return this;
    }

    public String getTitle() {
      return title;
    }

    public Post setTitle(String title) {
      this.title = title;
      return this;
    }

    public short getVersion() {
      return version;
    }
  }
}
