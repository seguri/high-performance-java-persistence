package com.vladmihalcea.hpjp.hibernate.identifier;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.junit.Test;

public class HiloPooledMigrationTest {

  private HiloIdentifierTest hiloIdentifierTest = new HiloIdentifierTest();

  private PooledSequenceIdentifierTest pooledIdentifierTest =
      new PooledSequenceIdentifierTest() {
        @Override
        protected void additionalProperties(Properties properties) {
          properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "none");
        }
      };

  @Test
  public void testMigration() {
    try {
      DataSource dataSource = hiloIdentifierTest.database().dataSourceProvider().dataSource();

      try (Connection connection = dataSource.getConnection();
          Statement statement = connection.createStatement()) {

        statement.executeUpdate("DROP TABLE IF EXISTS post");
        statement.executeUpdate("DROP SEQUENCE IF EXISTS post_sequence");
      } catch (Exception e) {
        fail(e.getMessage());
      }
      hiloIdentifierTest.init();
      hiloIdentifierTest.testHiloIdentifierGenerator();

      try (Connection connection = dataSource.getConnection();
          Statement statement = connection.createStatement()) {

        statement.execute("SELECT setval('post_sequence', (SELECT MAX(id) FROM post) + 1)");
        statement.execute("ALTER SEQUENCE post_sequence INCREMENT BY 3");
      } catch (Exception e) {
        fail(e.getMessage());
      }

      pooledIdentifierTest.init();
      pooledIdentifierTest.testPooledIdentifierGenerator();
    } finally {
      hiloIdentifierTest.destroy();
    }
  }
}
