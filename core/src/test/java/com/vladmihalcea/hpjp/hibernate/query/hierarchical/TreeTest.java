package com.vladmihalcea.hpjp.hibernate.query.hierarchical;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.hibernate.*;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class TreeTest extends AbstractTreeTest {

  @Test
  public void test() {

    List<PostComment> comments =
        doInJPA(
            entityManager -> {
              return (List<PostComment>)
                  entityManager
                      .unwrap(Session.class)
                      .createQuery("SELECT c " + "FROM PostComment c " + "WHERE c.status = :status")
                      .setParameter("status", Status.APPROVED)
                      .setResultTransformer(PostCommentTreeTransformer.INSTANCE)
                      .getResultList();
            });
    assertEquals(2, comments.size());
  }
}
