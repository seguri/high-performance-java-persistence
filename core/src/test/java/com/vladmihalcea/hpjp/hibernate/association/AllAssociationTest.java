package com.vladmihalcea.hpjp.hibernate.association;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.AbstractTest;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class AllAssociationTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class, PostDetails.class, PostComment.class, Tag.class};
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          Post post = new Post(1L);
          post.title = "Postit";

          PostComment comment1 = new PostComment();
          comment1.id = 1L;
          comment1.review = "Good";

          PostComment comment2 = new PostComment();
          comment2.id = 2L;
          comment2.review = "Excellent";

          post.addComment(comment1);
          post.addComment(comment2);
          entityManager.persist(post);
        });

    doInJPA(
        entityManager -> {
          LOGGER.info("No alias");
          List<Post> posts =
              entityManager
                  .createQuery(
                      """
                select p
                from Post p
                join fetch p.comments
                where p.title = :title
                """,
                      Post.class)
                  .setParameter("title", "Postit")
                  .getResultList();

          assertEquals(1, posts.size());
        });
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id private Long id;

    private String title;

    public Post() {}

    public Post(Long id) {
      this.id = id;
    }

    public Post(String title) {
      this.title = title;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @OneToOne(
        cascade = CascadeType.ALL,
        mappedBy = "post",
        orphanRemoval = true,
        fetch = FetchType.LAZY)
    private PostDetails details;

    @ManyToMany
    @JoinTable(
        name = "post_tag",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public List<PostComment> getComments() {
      return comments;
    }

    public PostDetails getDetails() {
      return details;
    }

    public List<Tag> getTags() {
      return tags;
    }

    public void addComment(PostComment comment) {
      comments.add(comment);
      comment.setPost(this);
    }

    public void addDetails(PostDetails details) {
      this.details = details;
      details.setPost(this);
    }

    public void removeDetails() {
      this.details.setPost(null);
      this.details = null;
    }
  }

  @Entity(name = "PostDetails")
  @Table(name = "post_details")
  public static class PostDetails {

    @Id private Long id;

    @Column(name = "created_on")
    private Date createdOn;

    @Column(name = "created_by")
    private String createdBy;

    public PostDetails() {
      createdOn = new Date();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Post post;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Post getPost() {
      return post;
    }

    public void setPost(Post post) {
      this.post = post;
    }

    public Date getCreatedOn() {
      return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
      this.createdOn = createdOn;
    }

    public String getCreatedBy() {
      return createdBy;
    }

    public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
    }
  }

  @Entity(name = "PostComment")
  @Table(name = "post_comment")
  public static class PostComment {

    @Id private Long id;

    @ManyToOne private Post post;

    private String review;

    public PostComment() {}

    public PostComment(String review) {
      this.review = review;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Post getPost() {
      return post;
    }

    public void setPost(Post post) {
      this.post = post;
    }

    public String getReview() {
      return review;
    }

    public void setReview(String review) {
      this.review = review;
    }
  }

  @Entity(name = "Tag")
  @Table(name = "tag")
  public static class Tag {

    @Id private Long id;

    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
