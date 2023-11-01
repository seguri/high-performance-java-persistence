package com.vladmihalcea.hpjp.jdbc.fetching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.vladmihalcea.hpjp.util.DatabaseProviderIntegrationTest;
import com.vladmihalcea.hpjp.util.providers.Database;
import com.vladmihalcea.hpjp.util.providers.entity.BlogEntityProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runners.Parameterized;

/**
 * SQLServerResultSetLimitTest - Test limiting result set vs fetching and discarding rows
 *
 * @author Vlad Mihalcea
 */
public class SQLServerResultSetLimitTest extends DatabaseProviderIntegrationTest {
  public static final String INSERT_POST = "insert into post (title, version, id) values (?, ?, ?)";

  public static final String INSERT_POST_COMMENT =
      "insert into post_comment (post_id, review, version, id) values (?, ?, ?, ?)";

  public static final String SELECT_POST_COMMENT_1 =
      "SELECT pc.id AS pc_id, p.id AS p_id  "
          + "FROM post_comment pc "
          + "INNER JOIN post p ON p.id = pc.post_id ";

  public static final String SELECT_POST_COMMENT_2 = "SELECT *  " + "FROM post_comment pc ";

  private BlogEntityProvider entityProvider = new BlogEntityProvider();

  public SQLServerResultSetLimitTest(Database database) {
    super(database);
  }

  @Parameterized.Parameters
  public static Collection<Database[]> databases() {
    List<Database[]> databases = new ArrayList<>();
    databases.add(Arrays.array(Database.SQLSERVER));
    return databases;
  }

  @Override
  protected Class<?>[] entities() {
    return entityProvider.entities();
  }

  @Override
  public void init() {
    super.init();
    doInJDBC(
        connection -> {
          try (PreparedStatement postStatement = connection.prepareStatement(INSERT_POST);
              PreparedStatement postCommentStatement =
                  connection.prepareStatement(INSERT_POST_COMMENT); ) {
            int postCount = getPostCount();
            int postCommentCount = getPostCommentCount();

            int index;

            for (int i = 0; i < postCount; i++) {
              if (i > 0 && i % 100 == 0) {
                postStatement.executeBatch();
              }
              index = 0;
              postStatement.setString(++index, String.format("Post no. %1$d", i));
              postStatement.setInt(++index, 0);
              postStatement.setLong(++index, i);
              postStatement.addBatch();
            }
            postStatement.executeBatch();

            for (int i = 0; i < postCount; i++) {
              for (int j = 0; j < postCommentCount; j++) {
                index = 0;
                postCommentStatement.setLong(++index, i);
                postCommentStatement.setString(++index, String.format("Post comment %1$d", j));
                postCommentStatement.setInt(++index, (int) (Math.random() * 1000));
                postCommentStatement.setLong(++index, (postCommentCount * i) + j);
                postCommentStatement.addBatch();
                if (j % 100 == 0) {
                  postCommentStatement.executeBatch();
                }
              }
            }
            postCommentStatement.executeBatch();
          } catch (SQLException e) {
            fail(e.getMessage());
          }
        });
  }

  @Test
  public void testLimit() {
    long startNanos = System.nanoTime();
    doInJDBC(
        connection -> {
          try (PreparedStatement statement1 = connection.prepareStatement(SELECT_POST_COMMENT_1);
              PreparedStatement statement11 = connection.prepareStatement(SELECT_POST_COMMENT_1);
              PreparedStatement statement2 = connection.prepareStatement(SELECT_POST_COMMENT_2); ) {
            statement1.setMaxRows(getMaxRows());
            assertEquals(getMaxRows(), processResultSet(statement1));
            assertEquals(getPostCommentCount() * getPostCount(), processResultSet(statement11));
            assertEquals(getPostCommentCount() * getPostCount(), processResultSet(statement2));
          } catch (SQLException e) {
            fail(e.getMessage());
          }
        });
    LOGGER.info(
        "{} Result Set with limit took {} millis",
        dataSourceProvider().database(),
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
  }

  protected int processResultSet(PreparedStatement statement) throws SQLException {
    statement.execute();
    int count = 0;
    ResultSet resultSet = statement.getResultSet();
    while (resultSet.next()) {
      resultSet.getLong(1);
      count++;
    }
    return count;
  }

  protected int getPostCount() {
    return 100;
  }

  protected int getPostCommentCount() {
    return 10;
  }

  protected int getMaxRows() {
    return 5;
  }

  @Override
  protected boolean proxyDataSource() {
    return false;
  }
}
