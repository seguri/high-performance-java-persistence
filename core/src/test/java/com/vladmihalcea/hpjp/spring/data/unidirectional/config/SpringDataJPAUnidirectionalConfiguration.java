package com.vladmihalcea.hpjp.spring.data.unidirectional.config;

import com.vladmihalcea.hpjp.spring.data.base.config.SpringDataJPABaseConfiguration;
import com.vladmihalcea.hpjp.spring.data.unidirectional.domain.Post;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import java.util.Properties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Vlad Mihalcea
 */
@ComponentScan(basePackages = {"com.vladmihalcea.hpjp.spring.data.unidirectional"})
@EnableJpaRepositories(
    basePackages = "com.vladmihalcea.hpjp.spring.data.unidirectional.repository",
    repositoryBaseClass = BaseJpaRepositoryImpl.class)
public class SpringDataJPAUnidirectionalConfiguration extends SpringDataJPABaseConfiguration {

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
