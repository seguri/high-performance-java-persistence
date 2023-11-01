package com.vladmihalcea.hpjp.spring.data.bidirectional.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.NaturalId;

/**
 * @author Vlad Mihalcea
 */
@Entity(name = "Tag")
@Table(name = "tag")
public class Tag {

  @Id @GeneratedValue private Long id;

  @NaturalId private String name;

  @ManyToMany(mappedBy = "tags")
  private List<Post> posts = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public Tag setId(Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Tag setName(String name) {
    this.name = name;
    return this;
  }

  public List<Post> getPosts() {
    return posts;
  }
}
