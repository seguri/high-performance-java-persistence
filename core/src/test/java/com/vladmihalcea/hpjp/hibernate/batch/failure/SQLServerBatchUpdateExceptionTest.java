package com.vladmihalcea.hpjp.hibernate.batch.failure;

import static org.junit.Assert.assertSame;

import com.vladmihalcea.hpjp.util.providers.Database;
import java.sql.BatchUpdateException;
import java.util.Arrays;

/**
 * @author Vlad Mihalcea
 */
public class SQLServerBatchUpdateExceptionTest extends AbstractBatchUpdateExceptionTest {

  @Override
  protected Database database() {
    return Database.SQLSERVER;
  }

  @Override
  protected void onBatchUpdateException(BatchUpdateException e) {
    assertSame(3, e.getUpdateCounts().length);
    LOGGER.info(e.getMessage());
    LOGGER.info(
        "Batch has managed to process {} entries",
        Arrays.stream(e.getUpdateCounts()).asLongStream().filter(l -> l > 0).count());
  }
}
