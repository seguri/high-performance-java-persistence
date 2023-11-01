package com.vladmihalcea.hpjp.spring.transaction.hibernate.dao;

import com.vladmihalcea.hpjp.hibernate.transaction.forum.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository
public class TagDAOImpl extends GenericDAOImpl<Tag, Long> implements TagDAO {

  protected TagDAOImpl() {
    super(Tag.class);
  }

  @Override
  public List<Tag> findByName(String... tags) {
    if (tags.length == 0) {
      throw new IllegalArgumentException("There's no tag name to search for!");
    }
    return getSession()
        .createQuery("select t " + "from Tag t " + "where t.name in :tags")
        .setParameterList("tags", Arrays.asList(tags))
        .list();
  }
}
