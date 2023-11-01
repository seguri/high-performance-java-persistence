package com.vladmihalcea.hpjp.hibernate.query.hierarchical;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.providers.DataSourceProvider;
import com.vladmihalcea.hpjp.util.providers.OracleDataSourceProvider;
import java.util.List;
import org.hibernate.query.NativeQuery;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class TreeConnectByTest extends AbstractTreeTest {

  @Override
  protected DataSourceProvider dataSourceProvider() {
    return new OracleDataSourceProvider();
  }

  @Test
  public void test() {

    List<PostComment> comments =
        doInJPA(
            entityManager -> {
              return (List<PostComment>)
                  entityManager
                      .createNativeQuery(
                          "SELECT * "
                              + "FROM PostComment c "
                              + "WHERE c.status = :status "
                              + "CONNECT BY PRIOR c.id = c.parent_id "
                              + "START WITH c.parent_id IS NULL AND lower(c.description) like :token ")
                      .setParameter("status", Status.APPROVED.name())
                      .setParameter("token", "high-performance%")
                      .unwrap(NativeQuery.class)
                      .addEntity(PostComment.class)
                      .setResultTransformer(PostCommentTreeTransformer.INSTANCE)
                      .list();
            });
    assertEquals(1, comments.size());
  }
}
