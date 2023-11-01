package com.vladmihalcea.hpjp.spring.data.query.hint;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.spring.data.query.hint.config.SpringDataJPAQueryHintConfiguration;
import com.vladmihalcea.hpjp.spring.data.query.hint.domain.Post;
import com.vladmihalcea.hpjp.spring.data.query.hint.domain.PostComment;
import com.vladmihalcea.hpjp.spring.data.query.hint.repository.PostRepository;
import com.vladmihalcea.hpjp.spring.data.query.hint.service.ForumService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Vlad Mihalcea
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringDataJPAQueryHintConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringDataJPAQueryHintTest {

  protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

  @Autowired private TransactionTemplate transactionTemplate;

  @Autowired private PostRepository postRepository;

  @Autowired private ForumService forumService;

  @Autowired private DataSource dataSource;

  @Before
  public void init() {
    try {
      transactionTemplate.execute(
          (TransactionCallback<Void>)
              transactionStatus -> {
                int COMMENT_COUNT = 10;

                LocalDateTime timestamp = LocalDateTime.of(2023, 3, 22, 12, 0, 0, 0);

                LongStream.rangeClosed(1, 10)
                    .forEach(
                        postId -> {
                          Post post =
                              new Post()
                                  .setId(postId)
                                  .setTitle(
                                      String.format(
                                          "High-Performance Java Persistence - Chapter %d", postId))
                                  .setCreatedOn(timestamp.plusMinutes(postId));

                          LongStream.rangeClosed(1, COMMENT_COUNT)
                              .forEach(
                                  commentOffset -> {
                                    long commentId = ((postId - 1) * COMMENT_COUNT) + commentOffset;

                                    post.addComment(
                                        new PostComment()
                                            .setId(commentId)
                                            .setReview(
                                                String.format(
                                                    "Comment nr. %d - A must-read!", commentId))
                                            .setCreatedOn(timestamp.plusMinutes(commentId)));
                                  });

                          postRepository.persist(post);
                        });

                return null;
              });
    } catch (TransactionException e) {
      LOGGER.error("Failure", e);
    }
  }

  @Test
  public void testFindTopNWithCommentsByTitle() {
    List<Post> posts = forumService.findAllByIdWithComments(List.of(1L, 2L, 3L));

    assertEquals(3, posts.size());
  }
}
