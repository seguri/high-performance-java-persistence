package com.vladmihalcea.hpjp.hibernate.type.json.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

/**
 * @author Vlad Mihalcea
 */
@MappedSuperclass
public class BaseEntity {

  @Id private Long id;

  @Version private Short version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Short getVersion() {
    return version;
  }
}
