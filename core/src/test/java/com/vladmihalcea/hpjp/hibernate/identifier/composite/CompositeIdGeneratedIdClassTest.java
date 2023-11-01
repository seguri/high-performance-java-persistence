package com.vladmihalcea.hpjp.hibernate.identifier.composite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import org.junit.Test;

public class CompositeIdGeneratedIdClassTest extends AbstractPostgreSQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Book.class};
  }

  @Test
  public void test() {
    LOGGER.debug("test");

    Book _book =
        doInJPA(
            entityManager -> {
              Book book = new Book();
              book.setPublisherId(1);
              book.setTitle("High-Performance Java Persistence");

              entityManager.persist(book);

              return book;
            });

    assertNotNull(_book.getRegistrationNumber());

    doInJPA(
        entityManager -> {
          PK key = new PK(_book.getRegistrationNumber(), 1);

          Book book = entityManager.find(Book.class, key);
          assertEquals("High-Performance Java Persistence", book.getTitle());
        });
  }

  @Entity(name = "Book")
  @Table(name = "book")
  @IdClass(PK.class)
  public static class Book {

    @Id
    @Column(name = "registration_number")
    @GeneratedValue
    private Long registrationNumber;

    @Id
    @Column(name = "publisher_id")
    private Integer publisherId;

    private String title;

    public Long getRegistrationNumber() {
      return registrationNumber;
    }

    public void setRegistrationNumber(Long registrationNumber) {
      this.registrationNumber = registrationNumber;
    }

    public int getPublisherId() {
      return publisherId;
    }

    public void setPublisherId(int publisherId) {
      this.publisherId = publisherId;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }
  }

  public static class PK implements Serializable {

    private Long registrationNumber;

    private Integer publisherId;

    public PK(Long registrationNumber, Integer publisherId) {
      this.registrationNumber = registrationNumber;
      this.publisherId = publisherId;
    }

    private PK() {}

    public Long getRegistrationNumber() {
      return registrationNumber;
    }

    public void setRegistrationNumber(Long registrationNumber) {
      this.registrationNumber = registrationNumber;
    }

    public Integer getPublisherId() {
      return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
      this.publisherId = publisherId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PK pk = (PK) o;
      return Objects.equals(registrationNumber, pk.registrationNumber)
          && Objects.equals(publisherId, pk.publisherId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(registrationNumber, publisherId);
    }
  }
}
