package dk.via.pro3.slaughterhouse;

import dk.via.pro3.slaughterhouse.generated.AnimalsResponse;
import dk.via.pro3.slaughterhouse.generated.GetAnimalsByProductRequest;
import dk.via.pro3.slaughterhouse.generated.GetProductsByAnimalRequest;
import dk.via.pro3.slaughterhouse.generated.ProductsResponse;
import dk.via.pro3.slaughterhouse.generated.TraceabilityServiceGrpc;
import dk.via.pro3.slaughterhouse.server.GrpcTraceabilityServer;
import dk.via.pro3.slaughterhouse.service.TraceabilityService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GrpcTraceabilityServerTest {

  private Server server;
  private ManagedChannel channel;
  private TraceabilityServiceGrpc.TraceabilityServiceBlockingStub stub;

  @BeforeEach
  void setUp() throws IOException {
    String name = InProcessServerBuilder.generateName();
    var service = new TraceabilityService(new FakeTraceabilityRepository());

    server = InProcessServerBuilder.forName(name)
        .directExecutor()
        .addService(new GrpcTraceabilityServer(service))
        .build()
        .start();
    channel = InProcessChannelBuilder.forName(name).directExecutor().build();
    stub = TraceabilityServiceGrpc.newBlockingStub(channel);
  }

  @AfterEach
  void tearDown() {
    channel.shutdownNow();
    server.shutdownNow();
  }

  @Test
  void getAnimalsByProduct_returnsRegistrationNumbers() {
    AnimalsResponse response = stub.getAnimalsByProduct(
        GetAnimalsByProductRequest.newBuilder().setProductId(1).build());

    assertEquals(List.of("DK-0001", "DK-0002"), response.getRegistrationNumbersList());
  }

  @Test
  void getAnimalsByProduct_unknownProduct_isNotFound() {
    StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
        () -> stub.getAnimalsByProduct(
            GetAnimalsByProductRequest.newBuilder().setProductId(999).build()));

    assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
  }

  @Test
  void getProductsByAnimal_returnsAllProducts() {
    ProductsResponse response = stub.getProductsByAnimal(
        GetProductsByAnimalRequest.newBuilder().setRegistrationNumber("DK-0002").build());

    assertEquals(3, response.getProductsCount());
    assertEquals("Half pig", response.getProducts(0).getDescription());
  }

  @Test
  void getProductsByAnimal_unknownAnimal_isNotFound() {
    StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
        () -> stub.getProductsByAnimal(
            GetProductsByAnimalRequest.newBuilder().setRegistrationNumber("XX-9999").build()));

    assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
  }
}