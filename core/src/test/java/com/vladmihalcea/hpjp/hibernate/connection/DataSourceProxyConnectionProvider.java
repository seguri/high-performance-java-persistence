package com.vladmihalcea.hpjp.hibernate.connection;

import java.util.Map;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;

/**
 * @author Vlad Mihalcea
 */
public class DataSourceProxyConnectionProvider extends DatasourceConnectionProviderImpl {

  @Override
  public void configure(Map configValues) {
    super.configure(configValues);
    DataSource dataSource =
        ProxyDataSourceBuilder.create(getDataSource())
            .name(getClass().getSimpleName())
            .listener(new SLF4JQueryLoggingListener())
            .build();
    super.setDataSource(dataSource);
  }
}
