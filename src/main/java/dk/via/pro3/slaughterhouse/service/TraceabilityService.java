package dk.via.pro3.slaughterhouse.service;

import dk.via.pro3.slaughterhouse.model.ProductRecord;
import dk.via.pro3.slaughterhouse.repository.TraceabilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraceabilityService {

  private final TraceabilityRepository repository;

  // Constructor injection: the container injects the repository
  public TraceabilityService(TraceabilityRepository repository) {
    this.repository = repository;
  }

  public List<String> getAnimalsByProduct(long productId) {
    if (!repository.productExists(productId)) {
      throw new NotFoundException("No product with id " + productId);
    }
    return repository.findAnimalRegistrationsByProduct(productId);
  }

  public List<ProductRecord> getProductsByAnimal(String registrationNumber) {
    if (!repository.animalExists(registrationNumber)) {
      throw new NotFoundException("No animal with registration number " + registrationNumber);
    }
    return repository.findProductsByAnimal(registrationNumber);
  }
}