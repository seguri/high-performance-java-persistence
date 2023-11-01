package com.vladmihalcea.hpjp.hibernate.identifier.uuid;

import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Properties;
import java.util.UUID;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import org.junit.Test;

public class PostgreSQLUUIDIdentifierTest extends AbstractPostgreSQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.put("hibernate.jdbc.batch_size", "5");
    properties.put("hibernate.order_inserts", "true");
    properties.put("hibernate.order_updates", "true");
  }

  @Override
  protected void afterInit() {
    executeStatement("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
  }

  @Test
  public void testPersist() {
    doInJPA(
        entityManager -> {
          Post post = new Post();
          post.setTitle("High-Performance Java Persistence");

          entityManager.persist(post);
        });
  }

  @Test
  public void testBatch() {
    doInJPA(
        entityManager -> {
          for (int i = 0; i < 3; i++) {
            Post post = new Post();
            post.setTitle(String.format("High-Performance Java Persistence, Part %d", i + 1));

            entityManager.persist(post);
          }
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "pg-uuid")
    @GenericGenerator(
        name = "pg-uuid",
        strategy = "uuid2",
        parameters =
            @Parameter(
                name = "uuid_gen_strategy_class",
                value =
                    "com.vladmihalcea.hpjp.hibernate.identifier.uuid.PostgreSQLUUIDGenerationStrategy"))
    private UUID id;

    private String title;

    public UUID getId() {
      return id;
    }

    public void setId(UUID id) {
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
