package com.vladmihalcea.hpjp.hibernate.identifier.uuid;

import static org.junit.Assert.*;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.junit.Test;

public class AssignedUUIDIdentifierTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Test
  public void testAssignedIdentifierGenerator() {
    doInJPA(
        entityManager -> {
          entityManager.persist(new Post().setTitle("High-Performance Java Persistence"));

          assertEquals(
              "High-Performance Java Persistence",
              entityManager
                  .createQuery(
                      """
                    select p
                    from Post p
                    """,
                      Post.class)
                  .getSingleResult()
                  .getTitle());

          byte[] uuid =
              (byte[]) entityManager.createNativeQuery("select id from Post").getSingleResult();

          assertNotNull(uuid);

          entityManager.merge(new Post().setTitle("High-Performance Java Persistence"));
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id = UUID.randomUUID();

    private String title;

    public UUID getId() {
      return id;
    }

    public Post setId(UUID id) {
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
  }
}
