package dk.via.pro3.slaughterhouse;

import dk.via.pro3.slaughterhouse.model.ProductRecord;
import dk.via.pro3.slaughterhouse.service.NotFoundException;
import dk.via.pro3.slaughterhouse.service.TraceabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TraceabilityServiceTest {

  private TraceabilityService service;

  @BeforeEach
  void setUp() {
    // Constructor injection: we hand the service a fake repository by hand
    service = new TraceabilityService(new FakeTraceabilityRepository());
  }

  @Test
  void getAnimalsByProduct_returnsAllAnimalsInvolved() {
    assertEquals(List.of("DK-0002", "DK-0003"), service.getAnimalsByProduct(3));
  }

  @Test
  void getAnimalsByProduct_unknownProduct_throwsNotFound() {
    assertThrows(NotFoundException.class, () -> service.getAnimalsByProduct(999));
  }

  @Test
  void getProductsByAnimal_returnsAllProductsTheAnimalIsIn() {
    List<ProductRecord> products = service.getProductsByAnimal("DK-0002");
    assertEquals(3, products.size());
    assertEquals(1L, products.get(0).id());
    assertEquals("Half pig", products.get(0).description());
  }

  @Test
  void getProductsByAnimal_unknownAnimal_throwsNotFound() {
    assertThrows(NotFoundException.class, () -> service.getProductsByAnimal("XX-9999"));
  }
}