package com.vladmihalcea.hpjp.hibernate.identifier;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.AbstractMySQLIntegrationTest;
import com.vladmihalcea.hpjp.util.providers.Database;
import jakarta.persistence.*;
import org.junit.Test;

public class AssignedIdentifierTest extends AbstractMySQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Book.class,
    };
  }

  @Override
  protected Database database() {
    return Database.POSTGRESQL;
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          entityManager.persist(
              new Book()
                  .setIsbn(9789730228236L)
                  .setTitle("High-Performance Java Persistence")
                  .setAuthor("Vlad Mihalcea"));
        });
    doInJPA(
        entityManager -> {
          Book book = entityManager.find(Book.class, 9789730228236L);
          assertEquals("High-Performance Java Persistence", book.getTitle());
        });
  }

  @Entity(name = "Book")
  @Table(name = "book")
  public static class Book {

    @Id private Long isbn;

    private String title;

    private String author;

    // Getters and setters omitted for brevity

    public Long getIsbn() {
      return isbn;
    }

    public Book setIsbn(Long isbn) {
      this.isbn = isbn;
      return this;
    }

    public String getTitle() {
      return title;
    }

    public Book setTitle(String title) {
      this.title = title;
      return this;
    }

    public String getAuthor() {
      return author;
    }

    public Book setAuthor(String author) {
      this.author = author;
      return this;
    }
  }
}
