package com.vladmihalcea.hpjp.hibernate.time.utc;

import java.util.Properties;
import org.hibernate.cfg.AvailableSettings;

/**
 * @author Vlad Mihalcea
 */
public class UTCTimeZonePostgreSQLTimestampTest extends DefaultPostgreSQLTimestampTest {

  @Override
  protected Properties properties() {
    Properties properties = super.properties();
    properties.setProperty(AvailableSettings.JDBC_TIME_ZONE, "UTC");
    return properties;
  }

  @Override
  protected String expectedServerTimestamp() {
    return "2016-08-25 11:23:46";
  }
}
