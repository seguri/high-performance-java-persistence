package com.vladmihalcea.hpjp.jdbc.batch;

import static org.junit.Assert.fail;

import com.vladmihalcea.hpjp.util.DatabaseProviderIntegrationTest;
import com.vladmihalcea.hpjp.util.providers.Database;
import com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/**
 * AbstractBatchStatementTest - Base class for testing JDBC Statement batching
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractBatchStatementTest extends DatabaseProviderIntegrationTest {

  public static final String INSERT_POST =
      "insert into post (title, version, id) values ('Post no. %1$d', 0, %1$d)";

  public static final String INSERT_POST_COMMENT =
      "insert into post_comment (post_id, review, version, id) values (%1$d, 'Post comment %2$d', 0, %2$d)";

  private final BlogEntityProvider entityProvider = new BlogEntityProvider();

  public AbstractBatchStatementTest(Database database) {
    super(database);
  }

  @Override
  protected Class<?>[] entities() {
    return entityProvider.entities();
  }

  @Test
  public void testInsert() {
    if (!ENABLE_LONG_RUNNING_TESTS) {
      return;
    }
    LOGGER.info("Test batch insert");
    AtomicInteger statementCount = new AtomicInteger();
    long startNanos = System.nanoTime();
    doInJDBC(
        connection -> {
          try (Statement statement = connection.createStatement()) {
            int postCount = getPostCount();
            int postCommentCount = getPostCommentCount();

            if (mix()) {
              for (int i = 0; i < postCount; i++) {
                executeStatement(statement, String.format(INSERT_POST, i), statementCount);
                for (int j = 0; j < postCommentCount; j++) {
                  executeStatement(
                      statement,
                      String.format(INSERT_POST_COMMENT, i, (postCommentCount * i) + j),
                      statementCount);
                }
              }
              onEnd(statement);
            } else {
              for (int i = 0; i < postCount; i++) {
                executeStatement(statement, String.format(INSERT_POST, i), statementCount);
              }
              onEnd(statement);

              for (int i = 0; i < postCount; i++) {
                for (int j = 0; j < postCommentCount; j++) {
                  executeStatement(
                      statement,
                      String.format(INSERT_POST_COMMENT, i, (postCommentCount * i) + j),
                      statementCount);
                }
              }
              onEnd(statement);
            }
          } catch (SQLException e) {
            fail(e.getMessage());
          }
        });
    LOGGER.info(
        "{}.testInsert for {} took {} millis",
        getClass().getSimpleName(),
        dataSourceProvider().getClass().getSimpleName(),
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
  }

  protected abstract void onFlush(Statement statement) throws SQLException;

  private void executeStatement(Statement statement, String dml, AtomicInteger statementCount)
      throws SQLException {
    onStatement(statement, dml);
    int count = statementCount.incrementAndGet();
    if (count % getBatchSize() == 0) {
      onFlush(statement);
    }
  }

  protected abstract void onStatement(Statement statement, String dml) throws SQLException;

  protected abstract void onEnd(Statement statement) throws SQLException;

  protected int getPostCount() {
    return 1000;
  }

  protected int getPostCommentCount() {
    return 4;
  }

  protected int getBatchSize() {
    return 50;
  }

  protected boolean mix() {
    return false;
  }
}
