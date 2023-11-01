package com.vladmihalcea.hpjp.hibernate.connection;

import com.vladmihalcea.flexypool.FlexyPoolDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;

/**
 * @author Vlad Mihalcea
 */
public class FlexyPoolHibernateConnectionProvider extends DatasourceConnectionProviderImpl {

  private transient FlexyPoolDataSource<DataSource> flexyPoolDataSource;

  @Override
  public void configure(Map props) {
    super.configure(props);
    flexyPoolDataSource = new FlexyPoolDataSource<>(getDataSource());
  }

  @Override
  public Connection getConnection() throws SQLException {
    return flexyPoolDataSource.getConnection();
  }

  @Override
  public boolean isUnwrappableAs(Class unwrapType) {
    return super.isUnwrappableAs(unwrapType) || getClass().isAssignableFrom(unwrapType);
  }

  @Override
  public void stop() {
    flexyPoolDataSource.stop();
    super.stop();
  }
}
