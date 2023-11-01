package com.vladmihalcea.hpjp.hibernate.transaction;

import static org.junit.Assert.assertTrue;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Id;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class SessionContainsTransactionTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class};
  }

  @Test
  public void testSessionContains() {
    EntityManager entityManager = null;
    EntityTransaction txn = null;
    try {
      entityManager = entityManagerFactory().createEntityManager();

      txn = entityManager.getTransaction();
      txn.begin();

      Post person = new Post(1L, "High-Performance Java Persistence");
      entityManager.persist(person);

      txn.commit();

      txn = entityManager.getTransaction();
      txn.begin();

      assertTrue(entityManager.contains(person));

      txn.commit();
    } catch (RuntimeException e) {
      if (txn != null && txn.isActive()) {
        txn.rollback();
      }
      throw e;
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Entity(name = "Post")
  public static class Post {

    @Id private Long id;

    private String name;

    public Post() {}

    public Post(long id, String name) {
      this.id = id;
      this.name = name;
    }

    public Long getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }
}
