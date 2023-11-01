package com.vladmihalcea.hpjp.spring.data.bidirectional.config;

import com.vladmihalcea.hpjp.spring.data.base.config.SpringDataJPABaseConfiguration;
import com.vladmihalcea.hpjp.spring.data.bidirectional.domain.Post;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import java.util.Properties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Vlad Mihalcea
 */
@ComponentScan(basePackages = {"com.vladmihalcea.hpjp.spring.data.bidirectional"})
@EnableJpaRepositories(
    basePackages = "com.vladmihalcea.hpjp.spring.data.bidirectional.repository",
    repositoryBaseClass = BaseJpaRepositoryImpl.class)
public class SpringDataJPABidirectionalConfiguration extends SpringDataJPABaseConfiguration {

  @Override
  protected String packageToScan() {
    return Post.class.getPackageName();
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.put("hibernate.jdbc.batch_size", "100");
    properties.put("hibernate.order_inserts", "true");
  }
}
