package com.vladmihalcea.hpjp.hibernate.fetching;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.LongStream;
import org.hibernate.Hibernate;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class FetchAllAssociationsTest extends AbstractPostgreSQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Post.class, PostDetails.class, PostComment.class, Tag.class};
  }

  @Override
  public void init() {
    super.init();
    int commentsSize = 2;
    doInJPA(
        entityManager -> {
          Tag java = new Tag();
          java.setName("java");
          entityManager.persist(java);

          Tag hibernate = new Tag();
          hibernate.setName("Hibernate");
          entityManager.persist(hibernate);

          LongStream.range(0, 5)
              .forEach(
                  i -> {
                    Post post = new Post(i);
                    post.setTitle(String.format("Post nr. %d", i));

                    post.getTags().add(java);
                    post.getTags().add(hibernate);

                    post.addDetails(new PostDetails());
                    post.getDetails().setCreatedBy("Vlad");
                    post.getDetails().setCreatedOn(new Date());

                    LongStream.range(0, commentsSize)
                        .forEach(
                            j -> {
                              PostComment comment = new PostComment();
                              comment.setId((i * commentsSize) + j);
                              comment.setReview(
                                  String.format("Good review nr. %d", comment.getId()));
                              post.addComment(comment);
                            });
                    entityManager.persist(post);
                  });
        });
  }

  @Test
  public void test() {
    Post post =
        doInJPA(
            entityManager -> {
              Post _post =
                  entityManager
                      .createQuery(
                          "select p "
                              + "from Post p "
                              + "join fetch p.comments "
                              + "join fetch p.details "
                              + "where p.id = :id",
                          Post.class)
                      .setParameter("id", 1L)
                      .getSingleResult();

              Hibernate.initialize(_post.getTags());

              return _post;
            });

    assertEquals(2, post.getTags().size());
    assertEquals(2, post.getComments().size());
    assertEquals("Vlad", post.getDetails().getCreatedBy());
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

    public void setDetails(PostDetails details) {
      this.details = details;
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

    @Id private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
