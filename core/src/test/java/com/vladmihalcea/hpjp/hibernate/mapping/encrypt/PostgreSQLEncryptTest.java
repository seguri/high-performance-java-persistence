package com.vladmihalcea.hpjp.hibernate.mapping.encrypt;

import static org.junit.Assert.assertEquals;

import com.vladmihalcea.hpjp.util.AbstractPostgreSQLIntegrationTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.ColumnTransformer;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLEncryptTest extends AbstractPostgreSQLIntegrationTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Vault.class};
  }

  @Override
  protected void afterInit() {
    executeStatement("CREATE EXTENSION IF NOT EXISTS \"pgcrypto\"");
  }

  @Override
  public void afterDestroy() {
    executeStatement("DROP EXTENSION \"pgcrypto\" CASCADE");
  }

  @Test
  public void test() {
    doInJPA(
        entityManager -> {
          Vault user = new Vault();
          user.setId(1L);
          user.setStorage("my_secret_key");

          entityManager.persist(user);
        });
    doInJPA(
        entityManager -> {
          String encryptedStorage =
              (String)
                  entityManager
                      .createNativeQuery(
                          """
				select encode(storage, 'base64')
				from Vault
				where id = :id
				""")
                      .setParameter("id", 1L)
                      .getSingleResult();

          LOGGER.info("Encoded storage:\n{}", encryptedStorage);
        });
    doInJPA(
        entityManager -> {
          Vault vault = entityManager.find(Vault.class, 1L);
          assertEquals("my_secret_key", vault.getStorage());

          vault.setStorage("another_secret_key");
        });

    doInJPA(
        entityManager -> {
          Vault vault = entityManager.find(Vault.class, 1L);
          assertEquals("another_secret_key", vault.getStorage());
        });
  }

  @Entity(name = "Vault")
  public static class Vault {

    @Id private Long id;

    @ColumnTransformer(
        read =
            """
				pgp_sym_decrypt(
				    storage,
				    current_setting('encrypt.key')
				)
				""",
        write =
            """
				pgp_sym_encrypt(
				    ?,
				    current_setting('encrypt.key')
				)
				""")
    @Column(columnDefinition = "bytea")
    private String storage;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getStorage() {
      return storage;
    }

    public void setStorage(String storage) {
      this.storage = storage;
    }
  }
}
