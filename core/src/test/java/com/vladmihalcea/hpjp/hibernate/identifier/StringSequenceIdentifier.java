package com.vladmihalcea.hpjp.hibernate.identifier;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * @author Vlad Mihalcea
 */
public class StringSequenceIdentifier extends SequenceStyleGenerator implements Configurable {

  public static final String SEQUENCE_PREFIX = "sequence_prefix";

  private String sequencePrefix;

  private String sequenceCallSyntax;

  @Override
  public void configure(Type type, Properties params, ServiceRegistry serviceRegistry)
      throws MappingException {
    super.configure(type, params, serviceRegistry);

    final JdbcEnvironment jdbcEnvironment = serviceRegistry.getService(JdbcEnvironment.class);

    final Dialect dialect = jdbcEnvironment.getDialect();

    final ConfigurationService configurationService =
        serviceRegistry.getService(ConfigurationService.class);

    String globalEntityIdentifierPrefix =
        configurationService.getSetting("entity.identifier.prefix", String.class, "SEQ_");

    sequencePrefix =
        ConfigurationHelper.getString(SEQUENCE_PREFIX, params, globalEntityIdentifierPrefix);

    final String sequencePerEntitySuffix =
        ConfigurationHelper.getString(
            SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX,
            params,
            SequenceStyleGenerator.DEF_SEQUENCE_SUFFIX);

    final String defaultSequenceName =
        params.getProperty(JPA_ENTITY_NAME) + sequencePerEntitySuffix;

    sequenceCallSyntax =
        dialect
            .getSequenceSupport()
            .getSequenceNextValString(
                ConfigurationHelper.getString(
                    SequenceStyleGenerator.SEQUENCE_PARAM, params, defaultSequenceName));
  }

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object obj) {
    long seqValue =
        ((Number) session.createNativeQuery(sequenceCallSyntax).uniqueResult()).longValue();

    return sequencePrefix + String.format("%011d%s", 0, seqValue);
  }
}
