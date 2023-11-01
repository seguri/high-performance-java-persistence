package com.vladmihalcea.hpjp.spring.transaction.hibernate.service;

import static org.junit.Assert.*;

import com.vladmihalcea.hpjp.hibernate.forum.dto.PostDTO;
import com.vladmihalcea.hpjp.hibernate.transaction.forum.Post;
import com.vladmihalcea.hpjp.spring.transaction.hibernate.dao.PostDAO;
import com.vladmihalcea.hpjp.spring.transaction.hibernate.dao.TagDAO;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vlad Mihalcea
 */
@Service
public class ForumServiceImpl implements ForumService {

  @Autowired private PostDAO postDAO;

  @Autowired private TagDAO tagDAO;

  @Autowired private SessionFactory sessionFactory;

  @Override
  @Transactional
  public Post newPost(String title, String... tags) {
    Post post = new Post();
    post.setTitle(title);
    post.getTags().addAll(tagDAO.findByName(tags));
    return postDAO.persist(post);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Post> findAllByTitle(String title) {
    List<Post> posts = postDAO.findByTitle(title);

    Session session = sessionFactory.getCurrentSession();
    PersistenceContext persistenceContext =
        ((SharedSessionContractImplementor) session).getPersistenceContext();

    for (Post post : posts) {
      assertTrue(session.contains(post));

      EntityEntry entityEntry = persistenceContext.getEntry(post);
      assertNull(entityEntry.getLoadedState());
    }

    return posts;
  }

  @Override
  @Transactional
  public Post findById(Long id) {
    Post post = postDAO.findById(id);

    Session session = sessionFactory.getCurrentSession();
    PersistenceContext persistenceContext =
        ((SharedSessionContractImplementor) session).getPersistenceContext();

    EntityEntry entityEntry = persistenceContext.getEntry(post);
    assertNotNull(entityEntry.getLoadedState());

    return post;
  }

  @Override
  @Transactional(readOnly = true)
  public PostDTO getPostDTOById(Long id) {
    return postDAO.getPostDTOById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public void processData() {
    // Application-level processing
  }
}
