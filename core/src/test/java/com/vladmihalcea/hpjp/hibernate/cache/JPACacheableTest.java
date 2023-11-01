package com.vladmihalcea.hpjp.hibernate.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import java.util.Properties;
import org.hibernate.stat.Statistics;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class JPACacheableTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Override
  protected Properties properties() {
    Properties properties = super.properties();
    properties.put("hibernate.cache.region.factory_class", "jcache");
    properties.put("hibernate.generate_statistics", Boolean.TRUE.toString());
    return properties;
  }

  public void afterInit() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setId(1L);
          post.setTitle("High-Performance Java Persistence");
          entityManager.persist(post);
        });
  }

  @Test
  public void testEntityLoad() {

    Statistics statistics = sessionFactory().getStatistics();
    statistics.clear();
    assertEquals(0, statistics.getPrepareStatementCount());

    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L);
          assertNotNull(post);
        });
    assertEquals(1, statistics.getPrepareStatementCount());

    doInJPA(
        entityManager -> {
          LOGGER.info("Load from cache");
          Post post = entityManager.find(Post.class, 1L);
        });
    assertEquals(2, statistics.getPrepareStatementCount());
  }

  @Entity(name = "Post")
  @Cacheable
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
}
