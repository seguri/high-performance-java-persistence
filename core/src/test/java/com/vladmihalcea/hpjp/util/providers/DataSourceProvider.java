package com.vladmihalcea.hpjp.util.providers;

import com.vladmihalcea.hpjp.util.ReflectionUtils;
import com.vladmihalcea.hpjp.util.providers.queries.Queries;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.dialect.Dialect;

/**
 * @author Vlad Mihalcea
 */
public interface DataSourceProvider {

  enum IdentifierStrategy {
    IDENTITY,
    SEQUENCE
  }

  String hibernateDialect();

  DataSource dataSource();

  Class<? extends DataSource> dataSourceClassName();

  Properties dataSourceProperties();

  String url();

  String username();

  String password();

  Database database();

  Queries queries();

  default Class<? extends Dialect> hibernateDialectClass() {
    return ReflectionUtils.getClass(hibernateDialect());
  }
}
