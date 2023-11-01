package com.vladmihalcea.hpjp.util.spring.config.jta;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.vladmihalcea.hpjp.util.DataSourceProxyType;
import com.vladmihalcea.hpjp.util.logging.InlineQueryLogEntryCreator;
import java.util.Properties;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.transaction.jta.platform.internal.JBossStandAloneJtaPlatform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Vlad Mihalcea
 */
@EnableTransactionManagement
@EnableAspectJAutoProxy
public abstract class AbstractJTATransactionManagerConfiguration {

  public static final String DATA_SOURCE_PROXY_NAME = DataSourceProxyType.DATA_SOURCE_PROXY.name();

  @Value("${btm.config.journal:null}")
  private String btmJournal;

  @Value("${hibernate.dialect}")
  private String hibernateDialect;

  @Bean
  public static PropertySourcesPlaceholderConfigurer properties() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public abstract DataSource actualDataSource();

  @DependsOn("actualDataSource")
  public DataSource dataSource() {
    SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
    loggingListener.setQueryLogEntryCreator(new InlineQueryLogEntryCreator());
    return ProxyDataSourceBuilder.create(actualDataSource())
        .name(DATA_SOURCE_PROXY_NAME)
        .listener(loggingListener)
        .build();
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean =
        new LocalContainerEntityManagerFactoryBean();
    localContainerEntityManagerFactoryBean.setJtaDataSource(dataSource());
    localContainerEntityManagerFactoryBean.setPackagesToScan(packagesToScan());

    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
    localContainerEntityManagerFactoryBean.setJpaProperties(additionalProperties());
    return localContainerEntityManagerFactoryBean;
  }

  protected String[] packagesToScan() {
    return new String[] {configurationClass().getPackage().getName()};
  }

  protected abstract Class configurationClass();

  @Bean
  public UserTransactionImple jtaUserTransaction() {
    UserTransactionImple userTransactionManager = new UserTransactionImple();
    return userTransactionManager;
  }

  @Bean
  public TransactionManagerImple jtaTransactionManager() {
    TransactionManagerImple transactionManager = new TransactionManagerImple();
    return transactionManager;
  }

  @Bean
  public JtaTransactionManager transactionManager() {
    JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
    jtaTransactionManager.setTransactionManager(jtaTransactionManager());
    jtaTransactionManager.setUserTransaction(jtaUserTransaction());
    jtaTransactionManager.setAllowCustomIsolationLevels(true);
    return jtaTransactionManager;
  }

  @Bean
  public TransactionTemplate transactionTemplate() {
    return new TransactionTemplate(transactionManager());
  }

  protected Properties additionalProperties() {
    Properties properties = new Properties();
    properties.put(AvailableSettings.JTA_PLATFORM, JBossStandAloneJtaPlatform.class);
    properties.setProperty("hibernate.dialect", hibernateDialect);
    properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    return properties;
  }
}
