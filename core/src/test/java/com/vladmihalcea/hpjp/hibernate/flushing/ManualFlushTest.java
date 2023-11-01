package com.vladmihalcea.hpjp.hibernate.flushing;

import static com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider.Post;
import static org.junit.Assert.assertTrue;

import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class ManualFlushTest extends AbstractPostgreSQLIntegrationTest {

  private static final Logger log = Logger.getLogger(ManualFlushTest.class);

  private BlogEntityProvider entityProvider = new BlogEntityProvider();

  @Override
  protected Class<?>[] entities() {
    return entityProvider.entities();
  }

  @Test
  public void testFlushSQL() {
    doInJPA(
        entityManager -> {
          entityManager.createNativeQuery("delete from Post").executeUpdate();
        });
    doInJPA(
        entityManager -> {
          log.info("testFlushSQL");
          Post post = new Post("Hibernate");
          post.setId(1L);
          entityManager.persist(post);

          Session session = entityManager.unwrap(Session.class);
          session.setHibernateFlushMode(FlushMode.MANUAL);

          assertTrue(
              ((Number) entityManager.createQuery("select count(id) from Post").getSingleResult())
                      .intValue()
                  == 0);

          assertTrue(
              ((Number) session.createNativeQuery("select count(*) from Post").uniqueResult())
                      .intValue()
                  == 0);
        });
  }
}
