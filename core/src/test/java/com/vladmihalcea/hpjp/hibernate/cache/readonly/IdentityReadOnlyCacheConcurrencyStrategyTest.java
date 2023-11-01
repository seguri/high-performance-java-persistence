package com.vladmihalcea.hpjp.hibernate.cache.readonly;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import java.util.Properties;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class IdentityReadOnlyCacheConcurrencyStrategyTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Post.class,
    };
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
    properties.put("hibernate.cache.region.factory_class", "jcache");
  }

  public void afterInit() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setTitle("High-Performance Java Persistence");
          entityManager.persist(post);
        });
    printCacheRegionStatistics(Post.class.getName());
    LOGGER.info("Post entity inserted");
  }

  @Test
  public void testPostEntityLoad() {

    LOGGER.info("Entities are not loaded from cache for identity as it's read-through");

    doInJPA(
        entityManager -> {
          Post post = entityManager.find(Post.class, 1L);
          printEntityCacheRegionStatistics(Post.class);
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  public static class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
