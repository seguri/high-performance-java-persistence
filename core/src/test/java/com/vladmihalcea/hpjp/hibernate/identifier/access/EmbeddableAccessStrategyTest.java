package com.vladmihalcea.hpjp.hibernate.identifier.access;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class EmbeddableAccessStrategyTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Patch.class, Change.class,
    };
  }

  @Test
  public void testSequenceIdentifierGenerator() {
    doInJPA(
        entityManager -> {
          Patch patch = new Patch();
          Change ch1 = new Change();
          ch1.setDiff("123");
          ch1.setPath("/a");
          patch.change = ch1;
          entityManager.persist(patch);
        });
    doInJPA(
        entityManager -> {
          Patch path = entityManager.find(Patch.class, 1L);
          assertEquals("123", path.change.getDiff());
        });
  }

  /**
   * Patch - Patch
   *
   * @author Vlad Mihalcea
   */
  @Entity(name = "Patch")
  public static class Patch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded private Change change;
  }

  @Embeddable
  @Access(AccessType.PROPERTY)
  public static class Change {

    private String path;

    private String diff;

    public Change() {}

    @Column(name = "path", nullable = false)
    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    @Column(name = "diff", nullable = false)
    public String getDiff() {
      return diff;
    }

    public void setDiff(String diff) {
      this.diff = diff;
    }
  }
}
