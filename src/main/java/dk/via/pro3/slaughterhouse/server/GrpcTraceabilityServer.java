package dk.via.pro3.slaughterhouse.server;

import dk.via.pro3.slaughterhouse.generated.AnimalsResponse;
import dk.via.pro3.slaughterhouse.generated.GetAnimalsByProductRequest;
import dk.via.pro3.slaughterhouse.generated.GetProductsByAnimalRequest;
import dk.via.pro3.slaughterhouse.generated.Product;
import dk.via.pro3.slaughterhouse.generated.ProductsResponse;
import dk.via.pro3.slaughterhouse.generated.TraceabilityServiceGrpc;
import dk.via.pro3.slaughterhouse.model.ProductRecord;
import dk.via.pro3.slaughterhouse.service.NotFoundException;
import dk.via.pro3.slaughterhouse.service.TraceabilityService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class GrpcTraceabilityServer extends TraceabilityServiceGrpc.TraceabilityServiceImplBase {

  private static final Logger LOG = LoggerFactory.getLogger(GrpcTraceabilityServer.class);

  private final TraceabilityService traceabilityService;

  // Constructor injection: the container injects the service
  public GrpcTraceabilityServer(TraceabilityService traceabilityService) {
    this.traceabilityService = traceabilityService;
  }

  @Override
  public void getAnimalsByProduct(GetAnimalsByProductRequest request,
      StreamObserver<AnimalsResponse> responseObserver) {
    try {
      var registrations = traceabilityService.getAnimalsByProduct(request.getProductId());
      var response = AnimalsResponse.newBuilder()
          .addAllRegistrationNumbers(registrations)
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
      LOG.info("Product {} involves {} animal(s)", request.getProductId(), registrations.size());
    } catch (NotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND
          .withDescription(e.getMessage())
          .asRuntimeException());
    } catch (Exception e) {
      LOG.error("getAnimalsByProduct failed", e);
      responseObserver.onError(Status.INTERNAL
          .withDescription("Internal error while reading from the database")
          .asRuntimeException());
    }
  }

  @Override
  public void getProductsByAnimal(GetProductsByAnimalRequest request,
      StreamObserver<ProductsResponse> responseObserver) {
    try {
      var products = traceabilityService.getProductsByAnimal(request.getRegistrationNumber());
      var builder = ProductsResponse.newBuilder();
      for (ProductRecord product : products) {
        builder.addProducts(ProductMapper.toProto(product));
      }
      responseObserver.onNext(builder.build());
      responseObserver.onCompleted();
      LOG.info("Animal {} was involved in {} product(s)", request.getRegistrationNumber(), products.size());
    } catch (NotFoundException e) {
      responseObserver.onError(Status.NOT_FOUND
          .withDescription(e.getMessage())
          .asRuntimeException());
    } catch (Exception e) {
      LOG.error("getProductsByAnimal failed", e);
      responseObserver.onError(Status.INTERNAL
          .withDescription("Internal error while reading from the database")
          .asRuntimeException());
    }
  }

  private static class ProductMapper {
    static Product toProto(ProductRecord record) {
      return Product.newBuilder()
          .setProductId(record.id())
          .setDescription(record.description())
          .build();
    }
  }
}