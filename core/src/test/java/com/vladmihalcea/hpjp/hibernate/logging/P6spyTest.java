package com.vladmihalcea.hpjp.hibernate.logging;

import com.vladmihalcea.hpjp.util.AbstractTest;
import com.vladmihalcea.hpjp.util.DataSourceProxyType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Properties;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class P6spyTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.put("hibernate.jdbc.batch_size", "5");
  }

  @Override
  protected DataSourceProxyType dataSourceProxyType() {
    return DataSourceProxyType.P6SPY;
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setId(1L);
          post.setTitle("Post it!");

          entityManager.persist(post);
        });
  }

  @Test
  public void testBatch() {
    doInJPA(
        entityManager -> {
          for (long i = 0; i < 3; i++) {
            Post post = new Post();
            post.setId(i);
            post.setTitle(String.format("Post no. %d", i));
            entityManager.persist(post);
          }
        });
  }

  @Test
  public void testOutageDetection() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setId(1L);
          post.setTitle("Post it!");

          entityManager.persist(post);
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id private Long id;

    private String title;

    @Version private short version;

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
}
