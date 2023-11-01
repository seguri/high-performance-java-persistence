package com.vladmihalcea.hpjp.spring.data.query.hint.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.vladmihalcea.hpjp.spring.data.query.hint.domain.Post;
import com.vladmihalcea.hpjp.spring.data.query.hint.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vlad Mihalcea
 */
@Service
public class ForumService {

  @Autowired private PostRepository postRepository;

  @PersistenceContext private EntityManager entityManager;

  @Transactional
  public List<Post> findAllByIdWithComments(List<Long> ids) {
    List<Post> posts = postRepository.findAllByIdWithComments(ids);
    SharedSessionContractImplementor session =
        entityManager.unwrap(SharedSessionContractImplementor.class);
    org.hibernate.engine.spi.PersistenceContext persistenceContext =
        session.getPersistenceContext();

    for (Post post : posts) {
      assertTrue(entityManager.contains(post));

      EntityEntry entityEntry = persistenceContext.getEntry(post);
      assertNull(entityEntry.getLoadedState());
    }
    return posts;
  }
}
