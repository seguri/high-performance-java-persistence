package com.vladmihalcea.hpjp.hibernate.batch.identity.stateless.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLInsert;

/**
 * @author Vlad Mihalcea
 */
@Entity(name = "Post")
@Table(name = "post")
@SQLInsert(sql = "insert into post (id, title) values (default, ?)")
public class Post extends AbstractPost<Post> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
