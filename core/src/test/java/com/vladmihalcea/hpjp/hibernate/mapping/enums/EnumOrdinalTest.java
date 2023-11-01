package com.vladmihalcea.hpjp.hibernate.mapping.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.vladmihalcea.hpjp.util.AbstractTest;
import com.vladmihalcea.hpjp.util.providers.Database;
import jakarta.persistence.*;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class EnumOrdinalTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Override
  protected Database database() {
    return Database.POSTGRESQL;
  }

  @Override
  protected void afterInit() {
    executeStatement("ALTER TABLE post DROP COLUMN status");
    executeStatement("ALTER TABLE post ADD COLUMN status NUMERIC(2)");
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          entityManager.persist(
              new Post()
                  .setTitle("Check out my website")
                  .setStatus(PostStatus.REQUIRES_MODERATOR_INTERVENTION));
        });

    try {
      doInJPA(
          entityManager -> {
            int postId = 50;

            int rowCount =
                entityManager
                    .createNativeQuery(
                        """
                    INSERT INTO post (status, title, id)
                    VALUES (:status, :title, :id)
                    """)
                    .setParameter("status", 99)
                    .setParameter("title", "Illegal Enum value")
                    .setParameter("id", postId)
                    .executeUpdate();

            assertEquals(1, rowCount);

            Post post = entityManager.find(Post.class, postId);

            fail("Should not map the Enum value of 100!");
          });
    } catch (ArrayIndexOutOfBoundsException e) {
      assertEquals("Index 99 out of bounds for length 4", e.getMessage());
    }
  }

  public enum PostStatus {
    PENDING,
    APPROVED,
    SPAM,
    REQUIRES_MODERATOR_INTERVENTION
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id @GeneratedValue private Integer id;

    private String title;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "NUMERIC(2)")
    private PostStatus status;

    public Integer getId() {
      return id;
    }

    public Post setId(Integer id) {
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

    public PostStatus getStatus() {
      return status;
    }

    public Post setStatus(PostStatus status) {
      this.status = status;
      return this;
    }
  }
}
