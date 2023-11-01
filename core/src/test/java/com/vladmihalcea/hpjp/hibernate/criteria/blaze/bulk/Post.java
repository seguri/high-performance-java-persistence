package com.vladmihalcea.hpjp.hibernate.criteria.blaze.bulk;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Vlad Mihalcea
 */
@Entity(name = "Post")
@Table(name = "post")
public class Post extends PostModerate<Post> {

  @Id private Long id;

  private String title;

  private String message;

  public Long getId() {
    return id;
  }

  public Post setId(Long id) {
    this.id = id;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public Post setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public Post setMessage(String message) {
    this.message = message;
    return this;
  }
}
