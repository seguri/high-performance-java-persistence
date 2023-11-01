package com.vladmihalcea.hpjp.hibernate.sp;

import static com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider.Post;
import static com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider.PostComment;
import static org.junit.Assert.*;

import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import com.vladmihalcea.hpjp.util.ReflectionUtils;
import com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.procedure.ProcedureOutputs;
import org.hibernate.result.Output;
import org.hibernate.result.ResultSetOutput;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLStoredProcedureTest extends AbstractPostgreSQLIntegrationTest {

  private BlogEntityProvider entityProvider = new BlogEntityProvider();

  @Override
  protected Class<?>[] entities() {
    return entityProvider.entities();
  }

  @Override
  protected void beforeInit() {
    executeStatement("DROP FUNCTION IF EXISTS fn_count_comments(bigint)");
    executeStatement("DROP FUNCTION IF EXISTS fn_post_comments(bigint)");
    executeStatement(
        """
            CREATE OR REPLACE FUNCTION fn_count_comments(
               IN postId bigint,
               OUT commentCount bigint)
               RETURNS bigint AS
            $BODY$
                BEGIN
                    SELECT COUNT(*) INTO commentCount
                    FROM post_comment
                    WHERE post_id = postId;
                END;
            $BODY$
            LANGUAGE plpgsql;
            """);
    executeStatement(
        """
            CREATE OR REPLACE FUNCTION fn_post_comments(postId BIGINT)
               RETURNS REFCURSOR AS
            $BODY$
                DECLARE
                    postComments REFCURSOR;
                BEGIN
                    OPEN postComments FOR
                        SELECT *
                        FROM post_comment
                        WHERE post_id = postId;
                    RETURN postComments;
                END;
            $BODY$
            LANGUAGE plpgsql
            """);
    executeStatement("DROP PROCEDURE IF EXISTS count_comments(bigint)");
    executeStatement("DROP PROCEDURE IF EXISTS post_comments(bigint)");
    executeStatement(
        """
            CREATE OR REPLACE PROCEDURE count_comments(
               IN postId bigint,
               OUT commentCount bigint)
            LANGUAGE plpgsql
            AS $$
                BEGIN
                    SELECT COUNT(*) INTO commentCount
                    FROM post_comment
                    WHERE post_id = postId;
                END;
            $$
            """);
    executeStatement(
        """
            CREATE OR REPLACE PROCEDURE post_comments(
                IN postId BIGINT,
                OUT postComments REFCURSOR)
            LANGUAGE plpgsql
            AS $$
                BEGIN
                    OPEN postComments FOR
                        SELECT *
                        FROM post_comment
                        WHERE post_id = postId;
                END;
            $$
            """);
  }

  @Override
  protected void afterInit() {
    doInJPA(
        entityManager -> {
          Post post = new Post(1L);
          post.setTitle("Post");

          PostComment comment1 = new PostComment("Good");
          comment1.setId(1L);
          PostComment comment2 = new PostComment("Excellent");
          comment2.setId(2L);

          post.addComment(comment1);
          post.addComment(comment2);
          entityManager.persist(post);
        });
  }

  @Test
  public void testStoredProcedureOutParameterDefaultClose() {
    doInJPA(
        entityManager -> {
          StoredProcedureQuery query =
              entityManager
                  .createStoredProcedureQuery("count_comments")
                  .registerStoredProcedureParameter("postId", Long.class, ParameterMode.IN)
                  .registerStoredProcedureParameter("commentCount", Long.class, ParameterMode.OUT)
                  .setParameter("postId", 1L);
          query.execute();
          Long commentCount = (Long) query.getOutputParameterValue("commentCount");

          assertEquals(Long.valueOf(2), commentCount);
        });
  }

  @Test
  public void testProcedureCallParameterDefaultClose() {
    doInJPA(
        entityManager -> {
          Session session = entityManager.unwrap(Session.class);

          ProcedureCall call = session.createStoredProcedureCall("count_comments");
          call.registerParameter("postId", Long.class, ParameterMode.IN);
          call.registerParameter("commentCount", Long.class, ParameterMode.OUT);

          call.setParameter("postId", 1L);

          Long commentCount = (Long) call.getOutputs().getOutputParameterValue("commentCount");
          assertEquals(Long.valueOf(2), commentCount);
        });
  }

  @Test
  public void testStoredProcedureOutParameter() {
    doInJPA(
        entityManager -> {
          try {
            StoredProcedureQuery query =
                entityManager
                    .createStoredProcedureQuery("count_comments")
                    .registerStoredProcedureParameter("postId", Long.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("commentCount", Long.class, ParameterMode.OUT)
                    .setParameter("postId", 1L);
            query.execute();
            Long commentCount = (Long) query.getOutputParameterValue("commentCount");
            assertEquals(Long.valueOf(2), commentCount);

            ProcedureOutputs procedureOutputs = query.unwrap(ProcedureOutputs.class);
            CallableStatement callableStatement =
                ReflectionUtils.getFieldValue(procedureOutputs, "callableStatement");
            assertFalse(callableStatement.isClosed());

            procedureOutputs.release();

            assertTrue(callableStatement.isClosed());
          } catch (SQLException e) {
            fail(e.getMessage());
          }
        });
  }

  @Test
  public void testStoredProcedureOutParameterCloseStatement() {
    doInJPA(
        entityManager -> {
          try {
            StoredProcedureQuery query =
                entityManager
                    .createStoredProcedureQuery("count_comments")
                    .registerStoredProcedureParameter("postId", Long.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("commentCount", Long.class, ParameterMode.OUT)
                    .setParameter("postId", 1L);

            try {
              query.execute();
              Long commentCount = (Long) query.getOutputParameterValue("commentCount");

              assertEquals(Long.valueOf(2), commentCount);
            } finally {
              query.unwrap(ProcedureOutputs.class).release();
            }

            CallableStatement callableStatement =
                ReflectionUtils.getFieldValue(
                    query.unwrap(ProcedureOutputs.class), "callableStatement");
            assertTrue(callableStatement.isClosed());
          } catch (SQLException e) {
            fail(e.getMessage());
          }
        });
  }

  @Test
  public void testStoredProcedureRefCursor() {
    doInJPA(
        entityManager -> {
          StoredProcedureQuery query =
              entityManager
                  .createStoredProcedureQuery("post_comments")
                  .registerStoredProcedureParameter(1, Long.class, ParameterMode.IN)
                  .registerStoredProcedureParameter(2, void.class, ParameterMode.REF_CURSOR)
                  .setParameter(1, 1L);

          query.execute();
          try (ResultSet rs = (ResultSet) query.getOutputParameterValue(2)) {
            if (rs.next()) {
              LOGGER.info("Post id: {}", rs.getLong(1));
            }
          } catch (SQLException e) {
            fail(e.getMessage());
          }
        });
  }

  @Test
  public void testHibernateProcedureCallRefCursor() {
    doInJPA(
        entityManager -> {
          Session session = entityManager.unwrap(Session.class);
          ProcedureCall call = session.createStoredProcedureCall("post_comments");
          call.registerParameter(1, Long.class, ParameterMode.IN);
          call.registerParameter(2, void.class, ParameterMode.REF_CURSOR);

          call.setParameter(1, 1L);

          Output output = call.getOutputs().getCurrent();
          if (output.isResultSet()) {
            List<Object[]> postComments = ((ResultSetOutput) output).getResultList();
            assertEquals(2, postComments.size());
          }
        });
  }

  @Test
  public void testFunctionWithJDBC() {
    doInJPA(
        entityManager -> {
          Session session = entityManager.unwrap(Session.class);
          Long commentCount =
              session.doReturningWork(
                  connection -> {
                    try (CallableStatement function =
                        connection.prepareCall("{ ? = call fn_count_comments(?) }")) {
                      function.registerOutParameter(1, Types.BIGINT);
                      function.setLong(2, 1L);
                      function.execute();
                      return function.getLong(1);
                    }
                  });
          assertEquals(Long.valueOf(2), commentCount);
        });
  }

  @Test
  public void testFunctionWithJDBCByName() {
    try {
      doInJPA(
          entityManager -> {
            final AtomicReference<Long> commentCount = new AtomicReference<>();
            Session session = entityManager.unwrap(Session.class);
            session.doWork(
                connection -> {
                  try (CallableStatement function =
                      connection.prepareCall("{ ? = call fn_count_comments(?) }")) {
                    function.registerOutParameter("commentCount", Types.BIGINT);
                    function.setLong("postId", 1L);
                    function.execute();
                    commentCount.set(function.getLong(1));
                  }
                });
            assertEquals(Long.valueOf(2), commentCount.get());
          });
    } catch (Exception e) {
      assertEquals(SQLFeatureNotSupportedException.class, e.getCause().getClass());
    }
  }

  @Test
  public void test_hql_bit_length_function_example() {
    doInJPA(
        entityManager -> {
          List<Number> bits =
              entityManager
                  .createQuery(
                      """
                select bit_length(c.title)
                from Post c
                """,
                      Number.class)
                  .getResultList();
          assertFalse(bits.isEmpty());
        });
  }
}
