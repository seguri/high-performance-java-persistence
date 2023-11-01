package com.vladmihalcea.hpjp.jdbc.connection;

import com.vladmihalcea.hpjp.util.AbstractOracleIntegrationTest;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vlad Mihalcea
 */
public class OracleConnectionCallTest extends AbstractOracleIntegrationTest {

  private final Logger LOGGER = LoggerFactory.getLogger(getClass());

  private int callCount = 1000;

  @Override
  protected Class<?>[] entities() {
    return new Class[] {};
  }

  @Test
  @Ignore
  public void testConnections() throws SQLException {
    LOGGER.info("Test without pooling for {}", dataSourceProvider().database());
    simulateLowLatencyTransactions(dataSourceProvider().dataSource(), 10);
    simulateLowLatencyTransactions(dataSourceProvider().dataSource(), 35);
  }

  private void simulateLowLatencyTransactions(DataSource dataSource, int waitMillis)
      throws SQLException {
    for (int i = 0; i < callCount; i++) {
      try {
        try (Connection connection = dataSource.getConnection()) {
          sleep(waitMillis);
        }
      } catch (SQLException e) {
        LOGGER.error("Exception on iteration " + i, e);
      }
    }
  }
}
