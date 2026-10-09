package dk.via.pro3.slaughterhouse;

import dk.via.pro3.slaughterhouse.model.ProductRecord;
import dk.via.pro3.slaughterhouse.repository.TraceabilityRepository;

import java.util.List;
import java.util.Map;

/** Test double: same data as the sample database, no JDBC involved. */
class FakeTraceabilityRepository extends TraceabilityRepository {

  private final Map<Long, List<String>> animalsByProduct = Map.of(
      1L, List.of("DK-0001", "DK-0002"),
      2L, List.of("DK-0001", "DK-0002"),
      3L, List.of("DK-0002", "DK-0003"));

  private final Map<String, List<ProductRecord>> productsByAnimal = Map.of(
      "DK-0001", List.of(new ProductRecord(1, "Half pig"),
          new ProductRecord(2, "Ham package 2x")),
      "DK-0002", List.of(new ProductRecord(1, "Half pig"),
          new ProductRecord(2, "Ham package 2x"),
          new ProductRecord(3, "Loin package 2x")),
      "DK-0003", List.of(new ProductRecord(3, "Loin package 2x")));

  FakeTraceabilityRepository() {
    super(null); // no JdbcTemplate needed, all methods are overridden
  }

  @Override
  public boolean productExists(long productId) {
    return animalsByProduct.containsKey(productId);
  }

  @Override
  public boolean animalExists(String registrationNumber) {
    return productsByAnimal.containsKey(registrationNumber);
  }

  @Override
  public List<String> findAnimalRegistrationsByProduct(long productId) {
    return animalsByProduct.get(productId);
  }

  @Override
  public List<ProductRecord> findProductsByAnimal(String registrationNumber) {
    return productsByAnimal.get(registrationNumber);
  }
}