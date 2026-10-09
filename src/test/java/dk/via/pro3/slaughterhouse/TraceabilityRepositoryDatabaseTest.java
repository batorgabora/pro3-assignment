package dk.via.pro3.slaughterhouse;

import dk.via.pro3.slaughterhouse.model.ProductRecord;
import dk.via.pro3.slaughterhouse.repository.TraceabilityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Integration test: needs PostgreSQL running with db/schema.sql loaded. */
@SpringBootTest
class TraceabilityRepositoryDatabaseTest {

  @Autowired
  private TraceabilityRepository repository;

  @Test
  void animalsOfHalfPig_comeFromTheDatabase() {
    assertEquals(List.of("DK-0001", "DK-0002"),
        repository.findAnimalRegistrationsByProduct(1));
  }

  @Test
  void productsOfAnimalDk0002_comeFromTheDatabase() {
    List<ProductRecord> products = repository.findProductsByAnimal("DK-0002");

    assertEquals(List.of(1L, 2L, 3L), products.stream().map(ProductRecord::id).toList());
  }

  @Test
  void existenceChecks() {
    assertTrue(repository.productExists(1));
    assertFalse(repository.productExists(999));
    assertTrue(repository.animalExists("DK-0001"));
    assertFalse(repository.animalExists("XX-9999"));
  }
}