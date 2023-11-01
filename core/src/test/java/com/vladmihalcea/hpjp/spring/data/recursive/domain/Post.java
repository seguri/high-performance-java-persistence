package com.vladmihalcea.hpjp.spring.data.recursive.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Vlad Mihalcea
 */
@Entity(name = "Post")
@Table(name = "post")
public class Post {

  @Id private Long id;

  private String title;

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
}
