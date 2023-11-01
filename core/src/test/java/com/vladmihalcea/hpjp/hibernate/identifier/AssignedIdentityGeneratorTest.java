package com.vladmihalcea.hpjp.hibernate.identifier;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.junit.Test;

public class AssignedIdentityGeneratorTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Post.class,
    };
  }

  @Test
  public void test() {
    LOGGER.debug("test");
    executeStatement(
        """
            ALTER TABLE post
            ALTER COLUMN id bigint
            GENERATED BY DEFAULT AS IDENTITY (START WITH 1)
            """);

    doInJPA(
        entityManager -> {
          entityManager.persist(new Post());
          entityManager.merge(new Post().setId(-1L));
          entityManager.persist(new Post());
          entityManager.merge(new Post().setId(21L));
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post implements Identifiable<Long> {

    @Id
    @GenericGenerator(
        name = "assigned-identity",
        strategy = "com.vladmihalcea.hpjp.hibernate.identifier.AssignedIdentityGenerator")
    @GeneratedValue(generator = "assigned-identity", strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
      return id;
    }

    public Post setId(Long id) {
      this.id = id;
      return this;
    }
  }
}
