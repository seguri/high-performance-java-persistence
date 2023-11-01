package com.vladmihalcea.hpjp.hibernate.flushing;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import org.hibernate.FlushMode;
import org.hibernate.cfg.AvailableSettings;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class HibernateAlwaysFlushConfigurationPropertyTest extends JPAAutoFlushTest {

  @Override
  protected boolean nativeHibernateSessionFactoryBootstrap() {
    return true;
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.setProperty(AvailableSettings.FLUSH_MODE, FlushMode.ALWAYS.name());
  }

  @Test
  public void testFlushAutoNativeSQL() {
    doInJPA(
        entityManager -> {
          assertEquals(
              0,
              ((Number)
                      entityManager
                          .createNativeQuery(
                              """
                        SELECT COUNT(*)
                        FROM post
                        """)
                          .getSingleResult())
                  .intValue());

          entityManager.persist(new Post().setTitle("High-Performance Java Persistence"));

          int postCount =
              ((Number)
                      entityManager
                          .createNativeQuery(
                              """
                SELECT COUNT(*)
                FROM post
                """)
                          .getSingleResult())
                  .intValue();

          assertEquals(1, postCount);
        });
  }
}
