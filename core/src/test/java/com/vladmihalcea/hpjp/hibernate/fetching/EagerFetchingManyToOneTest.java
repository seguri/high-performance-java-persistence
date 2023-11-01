package com.vladmihalcea.hpjp.hibernate.fetching;

import static org.junit.Assert.*;

import com.vladmihalcea.hpjp.hibernate.logging.validator.sql.SQLStatementCountValidator;
import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import jakarta.persistence.*;
import java.util.List;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class EagerFetchingManyToOneTest extends AbstractPostgreSQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Post.class, PostComment.class,
    };
  }

  @Override
  public void afterInit() {
    String[] reviews = new String[] {"amazing", "awesome", "excellent"};

    doInJPA(
        entityManager -> {
          long pastId = 1;
          long commentId = 1;

          for (long i = 1; i <= 3; i++) {
            Post post =
                new Post()
                    .setId(pastId++)
                    .setTitle(String.format("High-Performance Java Persistence, part %d", i));
            entityManager.persist(post);

            for (int j = 0; j < 3; j++) {
              entityManager.persist(
                  new PostComment()
                      .setId(commentId++)
                      .setPost(post)
                      .setReview(String.format("The part %d was %s", i, reviews[j])));
            }
          }
        });
  }

  /*private HypersistenceOptimizer hypersistenceOptimizer;

  protected EntityManagerFactory newEntityManagerFactory() {
      EntityManagerFactory emf = super.newEntityManagerFactory();
      hypersistenceOptimizer = new HypersistenceOptimizer(
          new JpaConfig(emf)
      );
      return emf;
  }*/

  @Test
  public void testFindOne() {
    doInJPA(
        entityManager -> {
          PostComment comment = entityManager.find(PostComment.class, 1L);

          assertNotNull(comment);
        });

    // assertTrue(hypersistenceOptimizer.getEvents().isEmpty());
  }

  @Test
  public void testFindOneWithQuery() {
    doInJPA(
        entityManager -> {
          PostComment comment =
              entityManager
                  .createQuery(
                      """
                select pc
                from PostComment pc
                where pc.id = :id
                """,
                      PostComment.class)
                  .setParameter("id", 1L)
                  .getSingleResult();

          assertNotNull(comment);
        });
  }

  @Test
  public void testFindWithQuery() {
    doInJPA(
        entityManager -> {
          List<PostComment> comments =
              entityManager
                  .createQuery(
                      """
                select pc
                from PostComment pc
                join pc.post p
                where p.title like :titlePatttern
                """,
                      PostComment.class)
                  .setParameter("titlePatttern", "High-Performance Java Persistence%")
                  .getResultList();

          assertEquals(9, comments.size());
        });
  }

  @Test
  public void testFindWithQueryAndFetch() {
    doInJPA(
        entityManager -> {
          Long commentId = 1L;
          PostComment comment =
              entityManager
                  .createQuery(
                      """
                select pc
                from PostComment pc
                left join fetch pc.post p
                where pc.id = :id
                """,
                      PostComment.class)
                  .setParameter("id", commentId)
                  .getSingleResult();
          assertNotNull(comment);
        });
  }

  @Test
  public void testNPlusOneDetection() {
    try {
      LOGGER.info("Detect N+1");
      SQLStatementCountValidator.reset();

      List<PostComment> _comments =
          doInJPA(
              entityManager -> {
                List<PostComment> comments =
                    entityManager
                        .createQuery(
                            """
                    select pc
                    from PostComment pc
                    where pc.review like :reviewPattern
                    """,
                            PostComment.class)
                        .setParameter("reviewPattern", "%excellent")
                        .getResultList();

                return comments;
              });

      assertEquals(3, _comments.size());

      SQLStatementCountValidator.assertSelectCount(1);
    } catch (Exception expected) {
      LOGGER.error("Exception", expected);
    }
  }

  @Test
  public void testFixingNPlusOne() {
    LOGGER.info("Fixing N+1");
    SQLStatementCountValidator.reset();

    doInJPA(
        entityManager -> {
          List<PostComment> comments =
              entityManager
                  .createQuery(
                      """
                select pc
                from PostComment pc
                join fetch pc.post
                where pc.review like :reviewPattern
                """,
                      PostComment.class)
                  .setParameter("reviewPattern", "%excellent")
                  .getResultList();

          assertEquals(3, comments.size());

          SQLStatementCountValidator.assertSelectCount(1);
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id private Long id;

    private String title;

    public Long getId() {
      return id;
    }

    public Post setId(Long id) {
      this.id = id;
      return this;
    }

    public String getTitle() {
      return title;
    }

    public Post setTitle(String title) {
      this.title = title;
      return this;
    }
  }

  @Entity(name = "PostComment")
  @Table(name = "post_comment")
  public static class PostComment {

    @Id private Long id;

    private String review;

    @ManyToOne private Post post;

    public PostComment setId(Long id) {
      this.id = id;
      return this;
    }

    public Post getPost() {
      return post;
    }

    public PostComment setPost(Post post) {
      this.post = post;
      return this;
    }

    public String getReview() {
      return review;
    }

    public PostComment setReview(String review) {
      this.review = review;
      return this;
    }
  }
}
