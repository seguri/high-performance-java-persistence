package com.vladmihalcea.hpjp.hibernate.fetching;

import static org.junit.Assert.*;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import org.hibernate.LazyInitializationException;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class FindVsGetReferenceTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {
      Post.class, PostComment.class,
    };
  }

  @Override
  protected void afterInit() {
    doInJPA(
        entityManager -> {
          entityManager.persist(new Post().setId(1L).setTitle("High-Performance Java Persistence"));
        });
  }

  @Test
  public void testFind() {
    doInJPA(
        entityManager -> {
          PostComment comment = new PostComment();
          comment.setReview("Just awesome!");

          Post post = entityManager.find(Post.class, 1L);
          comment.setPost(post);

          entityManager.persist(comment);
        });

    Post post =
        doInJPA(
            entityManager -> {
              return entityManager.find(Post.class, 1L);
            });

    assertEquals("High-Performance Java Persistence", post.getTitle());
  }

  @Test
  public void testGetReference() {
    doInJPA(
        entityManager -> {
          PostComment comment = new PostComment();
          comment.setReview("Just awesome!");

          Post post = entityManager.getReference(Post.class, 1L);
          comment.setPost(post);

          entityManager.persist(comment);
        });

    Post post =
        doInJPA(
            entityManager -> {
              return entityManager.getReference(Post.class, 1L);
            });

    try {
      post.getTitle();

      fail("Should throw LazyInitializationException");
    } catch (LazyInitializationException e) {
      LOGGER.info("Failure expected", e);
    }
  }

  @Test
  public void testDummyPojo() {
    Post _post =
        doInJPA(
            entityManager -> {
              PostComment comment = new PostComment();
              comment.setReview("Just awesome!");

              Post post = new Post();
              post.setId(1L);
              comment.setPost(post);

              entityManager.persist(comment);

              return post;
            });

    assertNull(_post.getTitle());
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

    @Id @GeneratedValue private Long id;

    private String review;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public Long getId() {
      return id;
    }

    public PostComment setId(Long id) {
      this.id = id;
      return this;
    }

    public String getReview() {
      return review;
    }

    public PostComment setReview(String review) {
      this.review = review;
      return this;
    }

    public Post getPost() {
      return post;
    }

    public PostComment setPost(Post post) {
      this.post = post;
      return this;
    }
  }
}
