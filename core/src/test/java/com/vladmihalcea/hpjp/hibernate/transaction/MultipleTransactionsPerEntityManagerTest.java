package com.vladmihalcea.hpjp.hibernate.transaction;

import com.vladmihalcea.hpjp.util.AbstractTest;
import com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class MultipleTransactionsPerEntityManagerTest extends AbstractTest {
  private BlogEntityProvider entityProvider = new BlogEntityProvider();

  @Override
  protected Class<?>[] entities() {
    return entityProvider.entities();
  }

  @Test
  public void test() {
    EntityManager entityManager = null;
    EntityTransaction txn = null;
    try {
      entityManager = entityManagerFactory().createEntityManager();
      txn = entityManager.getTransaction();
      txn.begin();
      entityManager.persist(new BlogEntityProvider.Post(1L));
      txn.commit();
      txn.begin();
      entityManager.persist(new BlogEntityProvider.Post(2L));
      txn.commit();
    } catch (RuntimeException e) {
      if (txn != null && txn.isActive()) txn.rollback();
      throw e;
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }
}
