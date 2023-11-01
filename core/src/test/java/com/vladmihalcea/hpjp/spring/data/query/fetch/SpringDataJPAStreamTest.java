package com.vladmihalcea.hpjp.spring.data.query.fetch;

import static org.junit.Assert.*;

import com.vladmihalcea.hpjp.spring.data.query.fetch.config.SpringDataJPAJoinFetchPaginationConfiguration;
import com.vladmihalcea.hpjp.spring.data.query.fetch.domain.Post;
import com.vladmihalcea.hpjp.spring.data.query.fetch.domain.PostComment;
import com.vladmihalcea.hpjp.spring.data.query.fetch.repository.PostRepository;
import com.vladmihalcea.hpjp.spring.data.query.fetch.service.ForumService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
@ContextConfiguration(classes = SpringDataJPAJoinFetchPaginationConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringDataJPAStreamTest {

  protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

  @Autowired private TransactionTemplate transactionTemplate;

  @Autowired private PostRepository postRepository;

  @Autowired private ForumService forumService;

  @Autowired private CacheManager cacheManager;

  @Autowired private ExecutorService executorService;

  public static final int POST_COUNT = 10;
  public static final int COMMENT_COUNT = 10;

  @Before
  public void init() {
    try {
      transactionTemplate.execute(
          (TransactionCallback<Void>)
              transactionStatus -> {
                LocalDateTime timestamp = LocalDate.now().atStartOfDay().plusHours(12);

                LongStream.rangeClosed(1, POST_COUNT)
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
  public void testFindAllPostsPublishedToday() {
    List<Post> posts = forumService.findAllPostsPublishedToday();

    assertEquals(POST_COUNT, posts.size());
  }

  @Test
  public void testUpdateCache() throws InterruptedException {
    Cache postCache = cacheManager.getCache(Post.class.getSimpleName());

    assertNull(postCache.get(1L));
    forumService.updatePostCache();

    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.SECONDS);
    assertNotNull(postCache.get(1L));
  }
}
