package com.vladmihalcea.hpjp.hibernate.query;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.List;
import org.hibernate.Session;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class EscapeLikeTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Person.class,
    };
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          Person vlad = new Person();
          vlad.id = 1L;
          vlad.firstName = "sp_Vlad";
          vlad.lastName = "Mihalcea";

          Person dan = new Person();
          dan.id = 2L;
          dan.firstName = "Dan";
          dan.lastName = "Mihalcea";

          entityManager.persist(vlad);
          entityManager.persist(dan);
        });

    doInJPA(
        entityManager -> {
          Session session = entityManager.unwrap(Session.class);
          List<Person> persons =
              session
                  .createQuery(
                      "select p " + "from Person p " + "where p.firstName like 'sp|_%' escape '|'")
                  .list();
          assertEquals(1, persons.size());
        });
  }

  @Entity(name = "Person")
  public static class Person {

    @Id private Long id;

    private String firstName;

    private String lastName;
  }
}
