package com.vladmihalcea.hpjp.hibernate.equality;

import com.vladmihalcea.hpjp.hibernate.identifier.Identifiable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class UUIDEqualityTest extends AbstractEqualityCheckTest<UUIDEqualityTest.Post> {

  @Override
  protected Class<?>[] entities() {
    return new Class[] {Post.class};
  }

  @Test
  public void testEquality() {
    Post post = new Post();
    post.setTitle("High-PerformanceJava Persistence");

    assertEqualityConsistency(Post.class, post);
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post implements Identifiable<UUID> {

    @Id private UUID id = UUID.randomUUID();

    private String title;

    public Post() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Post)) return false;
      Post post = (Post) o;
      return Objects.equals(id, post.getId());
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }

    @Override
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
