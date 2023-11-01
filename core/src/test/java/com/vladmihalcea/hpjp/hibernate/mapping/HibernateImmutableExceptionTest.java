package com.vladmihalcea.hpjp.hibernate.mapping;

import static org.junit.Assert.*;

import com.vladmihalcea.hpjp.util.AbstractTest;
import com.vladmihalcea.hpjp.util.ReflectionUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import java.util.Date;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.annotations.Immutable;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class HibernateImmutableExceptionTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Event.class};
  }

  @Override
  protected void afterInit() {
    doInJPA(
        entityManager -> {
          Event event = new Event(1L, "Temperature", "25");

          entityManager.persist(event);
        });
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.put("hibernate.query.immutable_entity_update_query_handling_mode", "exception");
  }

  @Test
  public void testFlushChanges() {
    doInJPA(
        entityManager -> {
          Event event = entityManager.find(Event.class, 1L);

          assertEquals("25", event.getEventValue());

          ReflectionUtils.setFieldValue(event, "eventValue", "10");
          assertEquals("10", event.getEventValue());
        });

    doInJPA(
        entityManager -> {
          Event event = entityManager.find(Event.class, 1L);

          assertEquals("25", event.getEventValue());
        });
  }

  @Test
  public void testJPQL() {
    try {
      doInJPA(
          entityManager -> {
            entityManager
                .createQuery("update Event " + "set eventValue = :eventValue " + "where id = :id")
                .setParameter("eventValue", "10")
                .setParameter("id", 1L)
                .executeUpdate();
          });

      fail("Should have thrown exception");
    } catch (HibernateException e) {
      assertEquals(
          "The query: [update Event set eventValue = :eventValue where id = :id] "
              + "attempts to update an immutable entity: [Event]",
          e.getMessage());
    }
  }

  @Test
  public void testCriteriaAPI() {
    try {
      doInJPA(
          entityManager -> {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaUpdate<Event> update = builder.createCriteriaUpdate(Event.class);

            Root<Event> root = update.from(Event.class);

            update.set(root.get("eventValue"), "100").where(builder.equal(root.get("id"), 1L));

            entityManager.createQuery(update).executeUpdate();
          });

      fail("Should have thrown exception");
    } catch (HibernateException e) {
      assertEquals(
          "The query: [<criteria>] attempts to update an immutable entity: [Event]",
          e.getMessage());
    }
  }

  @Entity(name = "Event")
  @Immutable
  public static class Event {

    @Id private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn = new Date();

    @Column(name = "event_key")
    private String eventKey;

    @Column(name = "event_value")
    private String eventValue;

    public Event(Long id, String eventKey, String eventValue) {
      this.id = id;
      this.eventKey = eventKey;
      this.eventValue = eventValue;
    }

    private Event() {}

    public Long getId() {
      return id;
    }

    public Date getCreatedOn() {
      return createdOn;
    }

    public String getEventKey() {
      return eventKey;
    }

    public String getEventValue() {
      return eventValue;
    }
  }
}
