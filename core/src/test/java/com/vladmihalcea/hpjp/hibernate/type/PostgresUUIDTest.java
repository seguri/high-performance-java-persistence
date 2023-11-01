package com.vladmihalcea.hpjp.hibernate.type;

import static org.junit.Assert.assertNotNull;

import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class PostgresUUIDTest extends AbstractPostgreSQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Event.class};
  }

  @Override
  public void init() {
    executeStatement("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
    super.init();
  }

  @Override
  public void destroy() {
    doInJPA(
        entityManager -> {
          entityManager.createNativeQuery("DROP EXTENSION \"uuid-ossp\" CASCADE").executeUpdate();
        });
    super.destroy();
  }

  @Test
  public void test() {
    Event _event =
        doInJPA(
            entityManager -> {
              Event event = new Event();
              entityManager.persist(event);
              return event;
            });

    assertNotNull(_event.uuid);
  }

  @Entity(name = "Event")
  @Table(name = "event")
  public static class Event {

    @Id @GeneratedValue private Long id;

    @Generated(GenerationTime.INSERT)
    @Column(columnDefinition = "UUID NOT NULL DEFAULT uuid_generate_v4()", insertable = false)
    private UUID uuid;
  }
}
