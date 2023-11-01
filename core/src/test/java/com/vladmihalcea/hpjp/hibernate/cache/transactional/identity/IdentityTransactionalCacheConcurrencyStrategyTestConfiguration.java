package com.vladmihalcea.hpjp.hibernate.cache.transactional.identity;

import com.vladmihalcea.hpjp.util.spring.config.jta.HSQLDBJtaTransactionManagerConfiguration;
import java.util.Properties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityTransactionalCacheConcurrencyStrategyTestConfiguration
    extends HSQLDBJtaTransactionManagerConfiguration {

  @Override
  protected Properties additionalProperties() {
    Properties properties = super.additionalProperties();
    properties.put("hibernate.cache.region.factory_class", "jcache");
    properties.put("hibernate.generate_statistics", Boolean.TRUE.toString());
    return properties;
  }

  @Override
  protected Class configurationClass() {
    return IdentityTransactionalEntities.class;
  }
}
