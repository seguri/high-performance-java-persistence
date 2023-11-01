package com.vladmihalcea.hpjp.jdbc.transaction;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.vladmihalcea.hpjp.util.providers.DataSourceProvider;
import com.vladmihalcea.hpjp.util.providers.Database;
import com.vladmihalcea.hpjp.util.providers.SQLServerDataSourceProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.util.Arrays;
import org.junit.runners.Parameterized;

/**
 * SQLServerConnectionReadyOnlyTransactionTest - Test to verify SQL Server driver supports read-only
 * transactions
 *
 * @author Vlad Mihalcea
 */
public class SQLServerConnectionReadyOnlyTransactionTest
    extends ConnectionReadyOnlyTransactionTest {

  public SQLServerConnectionReadyOnlyTransactionTest(Database database) {
    super(database);
  }

  @Parameterized.Parameters
  public static Collection<Database[]> databases() {
    List<Database[]> databases = new ArrayList<>();
    databases.add(Arrays.array(Database.SQLSERVER));
    return databases;
  }

  protected DataSourceProvider dataSourceProvider() {
    return new SQLServerDataSourceProvider() {
      @Override
      public DataSource dataSource() {
        SQLServerDataSource dataSource = (SQLServerDataSource) super.dataSource();
        dataSource.setURL(dataSource.getURL() + ";ApplicationIntent=ReadOnly");
        return dataSource;
      }
    };
  }
}
