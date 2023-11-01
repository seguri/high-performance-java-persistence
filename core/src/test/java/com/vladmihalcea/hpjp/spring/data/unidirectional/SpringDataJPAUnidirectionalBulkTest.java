package com.vladmihalcea.hpjp.spring.data.unidirectional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.vladmihalcea.hpjp.spring.data.unidirectional.config.SpringDataJPAUnidirectionalConfiguration;
import com.vladmihalcea.hpjp.spring.data.unidirectional.domain.*;
import com.vladmihalcea.hpjp.spring.data.unidirectional.repository.*;
import com.vladmihalcea.hpjp.spring.data.unidirectional.service.ForumService;
import com.vladmihalcea.hpjp.util.exception.ExceptionUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Vlad Mihalcea
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringDataJPAUnidirectionalConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringDataJPAUnidirectionalBulkTest {

  protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

  @Autowired private TransactionTemplate transactionTemplate;

  @Autowired private PostRepository postRepository;

  @Autowired private DefaultPostRepository defaultPostRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private UserVoteRepository userVoteRepository;

  @Autowired private PostDetailsRepository postDetailsRepository;

  @Autowired private PostCommentRepository postCommentRepository;

  @Autowired private TagRepository tagRepository;

  @Autowired private PostTagRepository postTagRepository;

  @Autowired private ForumService forumService;

  @PersistenceContext private EntityManager entityManager;

  @Before
  public void init() {
    transactionTemplate.execute(
        (TransactionCallback<Void>)
            transactionStatus -> {
              Post post = new Post().setId(1L).setTitle("High-Performance Java Persistence");
              postRepository.persist(post);

              postDetailsRepository.persist(
                  new PostDetails().setCreatedBy("Vlad Mihalcea").setPost(post));

              PostComment comment1 =
                  new PostComment().setReview("Best book on JPA and Hibernate!").setPost(post);

              PostComment comment2 =
                  new PostComment()
                      .setReview("A must-read for every Java developer!")
                      .setPost(post);

              postCommentRepository.persist(comment1);
              postCommentRepository.persist(comment2);

              User alice = new User().setId(1L).setName("Alice");

              User bob = new User().setId(2L).setName("Bob");

              userRepository.persist(alice);
              userRepository.persist(bob);

              userVoteRepository.persist(
                  new UserVote()
                      .setUser(alice)
                      .setComment(comment1)
                      .setScore(Math.random() > 0.5 ? 1 : -1));

              userVoteRepository.persist(
                  new UserVote()
                      .setUser(bob)
                      .setComment(comment2)
                      .setScore(Math.random() > 0.5 ? 1 : -1));

              Tag jdbc = new Tag().setName("JDBC");
              Tag hibernate = new Tag().setName("Hibernate");
              Tag jOOQ = new Tag().setName("jOOQ");

              tagRepository.persist(jdbc);
              tagRepository.persist(hibernate);
              tagRepository.persist(jOOQ);

              postTagRepository.persist(new PostTag(post, jdbc));
              postTagRepository.persist(new PostTag(post, hibernate));
              postTagRepository.persist(new PostTag(post, jOOQ));

              return null;
            });
  }

  @Test
  public void testDefaultDeleteById() {
    try {
      final JpaRepository postRepository = defaultPostRepository;
      transactionTemplate.execute(
          (TransactionCallback<Void>)
              transactionStatus -> {
                postRepository.deleteById(1L);

                return null;
              });

      fail("Should have thrown ConstraintViolationException");
    } catch (Exception e) {
      LOGGER.info("Expected", e);
      assertTrue(ExceptionUtil.isCausedBy(e, ConstraintViolationException.class));
    }
  }

  @Test
  public void testDeleteWithBulk() {
    forumService.deletePostById(1L);
  }
}
