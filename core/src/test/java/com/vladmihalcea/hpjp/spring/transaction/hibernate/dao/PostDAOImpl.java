package com.vladmihalcea.hpjp.spring.transaction.hibernate.dao;

import com.vladmihalcea.hpjp.hibernate.forum.dto.PostDTO;
import com.vladmihalcea.hpjp.hibernate.transaction.forum.Post;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository
public class PostDAOImpl extends GenericDAOImpl<Post, Long> implements PostDAO {

  protected PostDAOImpl() {
    super(Post.class);
  }

  @Override
  public List<Post> findByTitle(String title) {
    return getSession()
        .createQuery("select p from Post p where p.title = :title", Post.class)
        .setParameter("title", title)
        .getResultList();
  }

  @Override
  public PostDTO getPostDTOById(Long id) {
    return getSession()
        .createQuery(
            "select new PostDTO(p.id, p.title) " + "from Post p " + "where p.id = :id",
            PostDTO.class)
        .setParameter("id", id)
        .getSingleResult();
  }
}
