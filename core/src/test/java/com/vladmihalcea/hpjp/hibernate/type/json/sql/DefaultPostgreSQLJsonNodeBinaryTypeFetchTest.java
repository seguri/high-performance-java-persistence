package com.vladmihalcea.hpjp.hibernate.type.json.sql;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.hibernate.type.json.PostgreSQLJsonNodeBinaryTypeTest;
import io.hypersistence.utils.hibernate.type.json.internal.JacksonUtil;
import java.util.Map;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class DefaultPostgreSQLJsonNodeBinaryTypeFetchTest extends PostgreSQLJsonNodeBinaryTypeTest {

  @Test
  public void testFetchJsonPropertyUsingNativeSQL() {
    doInJPA(
        entityManager -> {
          String properties =
              (String)
                  entityManager
                      .createNativeQuery(
                          """
                SELECT properties
                FROM book
                WHERE isbn = :isbn
                """)
                      .setParameter("isbn", "978-9730228236")
                      .getSingleResult();

          assertEquals(
              "High-Performance Java Persistence",
              JacksonUtil.fromString(properties, Map.class).get("title"));
        });
  }
}
