package com.vladmihalcea.hpjp.hibernate.concurrency.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vladmihalcea.hpjp.util.AbstractTest;
import com.vladmihalcea.hpjp.util.exception.ExceptionUtil;
import io.hypersistence.utils.hibernate.type.json.internal.JacksonUtil;
import jakarta.persistence.*;
import org.hibernate.StaleStateException;
import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class VersionTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[] {Product.class};
  }

  @Override
  protected void afterInit() {
    doInJPA(
        entityManager -> {
          Product product = new Product();
          product.setId(1L);

          entityManager.persist(product);
        });

    doInJPA(
        entityManager -> {
          Product product = entityManager.find(Product.class, 1L);
          assertEquals(0, product.getVersion());

          product.setQuantity(5);
        });

    doInJPA(
        entityManager -> {
          Product product = entityManager.find(Product.class, 1L);
          assertEquals(1, product.getVersion());
        });
  }

  @Test
  public void testOptimisticLockingException() {
    try {
      doInJPA(
          entityManager -> {
            Product product = entityManager.find(Product.class, 1L);

            executeSync(
                () ->
                    doInJPA(
                        _entityManager -> {
                          LOGGER.info("Batch processor updates product stock");

                          Product _product = _entityManager.find(Product.class, 1L);
                          _product.setQuantity(0);
                        }));

            LOGGER.info("Changing the previously loaded Product entity");
            product.setQuantity(4);
          });
    } catch (Exception expected) {
      LOGGER.error("Throws", expected);

      assertEquals(OptimisticLockException.class, expected.getCause().getClass());
      assertTrue(ExceptionUtil.rootCause(expected) instanceof StaleStateException);
    }
  }

  @Test
  public void testDelete() {
    doInJPA(
        entityManager -> {
          Product product = entityManager.getReference(Product.class, 1L);

          entityManager.remove(product);
        });
  }

  @Test
  public void testChangeVersion() {
    doInJPA(
        entityManager -> {
          Product product = entityManager.find(Product.class, 1L);

          product.setVersion(100);
        });
  }

  @Test
  public void testMerge() {

    String productJsonString =
        doInJPA(
            entityManager -> {
              return JacksonUtil.toString(entityManager.find(Product.class, 1L));
            });

    executeSync(
        () ->
            doInJPA(
                entityManager -> {
                  LOGGER.info("Batch processor updates product stock");

                  Product product = entityManager.find(Product.class, 1L);
                  product.setQuantity(0);
                }));

    LOGGER.info("Changing the previously loaded Product entity");
    ObjectNode productJsonNode = (ObjectNode) JacksonUtil.toJsonNode(productJsonString);
    int quantity = productJsonNode.get("quantity").asInt();
    productJsonNode.put("quantity", String.valueOf(--quantity));

    try {
      doInJPA(
          entityManager -> {
            LOGGER.info("Merging the Product entity");

            Product product = JacksonUtil.fromString(productJsonNode.toString(), Product.class);
            entityManager.merge(product);
          });
    } catch (Exception expected) {
      LOGGER.error("Throws", expected);
      assertEquals(OptimisticLockException.class, expected.getClass());
      assertTrue(ExceptionUtil.rootCause(expected) instanceof StaleStateException);
    }
  }

  @Entity(name = "Product")
  @Table(name = "product")
  public static class Product {

    @Id private Long id;

    private int quantity;

    @Version private short version;

    public Long getId() {
      return id;
    }

    public Product setId(Long id) {
      this.id = id;
      return this;
    }

    public int getQuantity() {
      return quantity;
    }

    public Product setQuantity(int quantity) {
      this.quantity = quantity;
      return this;
    }

    public int getVersion() {
      return version;
    }

    public Product setVersion(int version) {
      this.version = (short) version;
      return this;
    }
  }
}
