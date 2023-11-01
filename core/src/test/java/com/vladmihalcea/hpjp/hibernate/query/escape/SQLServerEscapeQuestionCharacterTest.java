package com.vladmihalcea.hpjp.hibernate.query.escape;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.AbstractSQLServerIntegrationTest;
import jakarta.persistence.*;
import java.util.List;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class SQLServerEscapeQuestionCharacterTest extends AbstractSQLServerIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Post.class,
    };
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setId(1L);
          post.setTitle("High-Performance Java Persistence");
          post.setActive(true);

          entityManager.persist(post);
        });

    doInJPA(
        entityManager -> {
          List<Post> posts =
              entityManager
                  .createQuery(
                      "select p " + "from Post p " + "where p.active = :active", Post.class)
                  .setParameter("active", true)
                  .getResultList();

          assertEquals(1, posts.size());
        });

    doInJPA(
        entityManager -> {
          List<String> posts =
              entityManager
                  .createNativeQuery(
                      "select p.title " + "from [post] p " + "where p.[active\\?] = :active")
                  .setParameter("active", true)
                  .getResultList();

          assertEquals(1, posts.size());
        });
  }

  @Entity(name = "Post")
  @Table(name = "[post]")
  public static class Post {

    @Id private Long id;

    private String title;

    @Column(name = "[active?]")
    private boolean active;

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

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }
  }
}
