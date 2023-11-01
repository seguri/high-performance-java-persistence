package com.vladmihalcea.hpjp.hibernate.identifier.batch;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import org.junit.Test;

public class AutoIdentifierWithSequenceGeneratorTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Post.class,
    };
  }

  @Test
  public void test() {
    int batchSize = 10;
    doInJPA(
        entityManager -> {
          for (int i = 0; i < batchSize; i++) {
            entityManager.persist(new Post());
          }
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_sequence")
    @SequenceGenerator(name = "custom_sequence", initialValue = 10)
    private Long id;
  }
}
