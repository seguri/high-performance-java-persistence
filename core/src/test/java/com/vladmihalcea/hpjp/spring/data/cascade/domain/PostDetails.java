package com.vladmihalcea.hpjp.spring.data.cascade.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Vlad Mihalcea
 */
@Entity(name = "PostDetails")
@Table(name = "post_details")
public class PostDetails {

  @Id private Long id;

  @Column(name = "created_on")
  private LocalDateTime createdOn;

  @Column(name = "created_by")
  private String createdBy;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "id")
  private Post post;

  public Long getId() {
    return id;
  }

  public PostDetails setId(Long id) {
    this.id = id;
    return this;
  }

  public Post getPost() {
    return post;
  }

  public PostDetails setPost(Post post) {
    this.post = post;
    return this;
  }

  public LocalDateTime getCreatedOn() {
    return createdOn;
  }

  public PostDetails setCreatedOn(LocalDateTime createdOn) {
    this.createdOn = createdOn;
    return this;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public PostDetails setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }
}
