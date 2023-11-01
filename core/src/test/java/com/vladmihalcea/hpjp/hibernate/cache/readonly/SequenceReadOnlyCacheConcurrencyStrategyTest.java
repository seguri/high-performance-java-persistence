package com.vladmihalcea.hpjp.hibernate.cache.readonly;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import java.util.Properties;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.Before;
import org.junit.Test;

/**
 * CacheConcurrencyStrategyTest - Test to check CacheConcurrencyStrategy.READ_ONLY
 *
 * @author Vlad Mihalcea
 */
public class SequenceReadOnlyCacheConcurrencyStrategyTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Post.class,
    };
  }

  @Override
  protected Properties properties() {
    Properties properties = super.properties();
    properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
    properties.put("hibernate.cache.region.factory_class", "jcache");
    return properties;
  }

  @Before
  public void init() {
    super.init();
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

    LOGGER.info("Entities are loaded from cache for sequence as it's write-through");

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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
