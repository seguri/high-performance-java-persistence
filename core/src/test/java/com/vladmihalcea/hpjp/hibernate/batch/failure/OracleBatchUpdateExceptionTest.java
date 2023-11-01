package com.vladmihalcea.hpjp.hibernate.batch.failure;

import static org.junit.Assert.assertSame;

import com.vladmihalcea.hpjp.util.providers.Database;
import java.sql.*;

/**
 * @author Vlad Mihalcea
 */
public class OracleBatchUpdateExceptionTest extends AbstractBatchUpdateExceptionTest {

  @Override
  protected Database database() {
    return Database.ORACLE;
  }

  @Override
  protected void onBatchUpdateException(BatchUpdateException e) {
    assertSame(2, e.getUpdateCounts().length);
    LOGGER.info(e.getMessage());
    LOGGER.info("Batch has managed to process {} entries", e.getUpdateCounts().length);
  }
}
