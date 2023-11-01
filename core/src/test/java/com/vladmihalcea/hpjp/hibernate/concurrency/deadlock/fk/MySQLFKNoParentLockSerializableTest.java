package com.vladmihalcea.hpjp.hibernate.concurrency.deadlock.fk;

import jakarta.persistence.EntityManager;
import java.sql.Connection;
import org.hibernate.Session;

/**
 * @author Vlad Mihalcea
 */
public class MySQLFKNoParentLockSerializableTest extends MySQLFKNoParentLockRRTest {

  protected void prepareConnection(EntityManager entityManager) {
    entityManager
        .unwrap(Session.class)
        .doWork(
            connection -> {
              connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
              setJdbcTimeout(connection);
            });
  }
}
