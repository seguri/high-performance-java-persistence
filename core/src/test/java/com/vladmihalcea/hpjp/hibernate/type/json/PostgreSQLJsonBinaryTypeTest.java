package com.vladmihalcea.hpjp.hibernate.type.json;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.hibernate.type.json.model.BaseEntity;
import com.vladmihalcea.hpjp.hibernate.type.json.model.Location;
import com.vladmihalcea.hpjp.hibernate.type.json.model.Ticket;
import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import org.hibernate.annotations.Type;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLJsonBinaryTypeTest extends AbstractPostgreSQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Event.class, Participant.class};
  }

  @Override
  protected void afterInit() {
    doInJPA(
        entityManager -> {
          Event nullEvent = new Event();
          nullEvent.setId(0L);
          entityManager.persist(nullEvent);

          Location location = new Location();
          location.setCountry("Romania");
          location.setCity("Cluj-Napoca");

          Event event = new Event();
          event.setId(1L);
          event.setLocation(location);
          entityManager.persist(event);

          Ticket ticket = new Ticket();
          ticket.setPrice(12.34d);
          ticket.setRegistrationCode("ABC123");

          Participant participant = new Participant();
          participant.setId(1L);
          participant.setTicket(ticket);
          participant.setEvent(event);

          entityManager.persist(participant);
        });
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          Event event = entityManager.find(Event.class, 1L);
          assertEquals("Cluj-Napoca", event.getLocation().getCity());

          Participant participant = entityManager.find(Participant.class, 1L);
          assertEquals("ABC123", participant.getTicket().getRegistrationCode());

          List<String> participants =
              entityManager
                  .createNativeQuery(
                      "select jsonb_pretty(p.ticket) "
                          + "from participant p "
                          + "where p.ticket ->> 'price' > :price")
                  .setParameter("price", "10")
                  .getResultList();

          event.getLocation().setCity("Constanța");
          assertEquals(0, event.getVersion().intValue());
          entityManager.flush();
          assertEquals(1, event.getVersion().intValue());

          assertEquals(1, participants.size());
        });

    doInJPA(
        entityManager -> {
          Event event = entityManager.find(Event.class, 1L);
          event.getLocation().setCity(null);
          assertEquals(1, event.getVersion().intValue());
          entityManager.flush();
          assertEquals(2, event.getVersion().intValue());
        });

    doInJPA(
        entityManager -> {
          Event event = entityManager.find(Event.class, 1L);
          event.setLocation(null);
          assertEquals(2, event.getVersion().intValue());
          entityManager.flush();
          assertEquals(3, event.getVersion().intValue());
        });
  }

  @Entity(name = "Event")
  @Table(name = "event")
  public static class Event extends BaseEntity {

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Location location;

    public Location getLocation() {
      return location;
    }

    public void setLocation(Location location) {
      this.location = location;
    }
  }

  @Entity(name = "Participant")
  @Table(name = "participant")
  public static class Participant extends BaseEntity {

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Ticket ticket;

    @ManyToOne private Event event;

    public Ticket getTicket() {
      return ticket;
    }

    public void setTicket(Ticket ticket) {
      this.ticket = ticket;
    }

    public Event getEvent() {
      return event;
    }

    public void setEvent(Event event) {
      this.event = event;
    }
  }
}
