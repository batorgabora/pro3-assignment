# Slaughterhouse Traceability Service

PRO3 course assignment, Part 2. A gRPC service that answers two recall questions from a PostgreSQL database:

- which animals (registration numbers) are involved in a product
- which products an animal has been involved in

## Requirements

- JDK 21 or newer
- PostgreSQL (tested with 18)

## Setup

1. Create the database and load the schema and sample data:

```bash
   createdb slaughterhouse
   psql -d slaughterhouse -f db/schema.sql
```

2. Check the connection settings in `src/main/resources/application.properties` and adjust the username and password to match your PostgreSQL installation:

```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/slaughterhouse
   spring.datasource.username=<your user>
   spring.datasource.password=<your password, empty if none>
```

## Run

```bash
./mvnw spring-boot:run
```

The gRPC server listens on port **9090** (plaintext, server reflection enabled).

## Test

Stop the running server first, because the integration tests start the application on the same port. The database must be running with `db/schema.sql` loaded.

```bash
./mvnw test
```

| Test class | Covers | Needs database |
|---|---|---|
| `TraceabilityServiceTest` | Service logic with a fake repository | No |
| `GrpcTraceabilityServerTest` | gRPC layer over an in-process channel | No |
| `TraceabilityRepositoryDatabaseTest` | SQL queries against PostgreSQL | Yes |

## Manual test (Postman)

New gRPC request, URL `localhost:9090`, TLS off, method list via server reflection (or import `src/main/proto/traceability.proto`).

| Method | Message | Result |
|---|---|---|
| `GetAnimalsByProduct` | `{"product_id": 1}` | `DK-0001`, `DK-0002` |
| `GetAnimalsByProduct` | `{"product_id": 999}` | `NOT_FOUND` |
| `GetProductsByAnimal` | `{"registration_number": "DK-0002"}` | products 1, 2, 3 |
| `GetProductsByAnimal` | `{"registration_number": "XX-9999"}` | `NOT_FOUND` |

## Structure

```
src/main/proto/traceability.proto   service contract (stubs are generated at build time)
dk.via.pro3.slaughterhouse
  server/       GrpcTraceabilityServer     @GrpcService, gRPC endpoint
  service/      TraceabilityService        @Service, logic, NotFoundException
  repository/   TraceabilityRepository     @Repository, JdbcTemplate SQL
  model/        ProductRecord
db/schema.sql                       tables and sample data
```

Dependencies are wired with constructor injection by the Spring container.

## How tracing works

A product references the trays its parts came from, and each part belongs to one animal. Both lookups follow this chain:

- product -> trays -> parts -> animals
- animal -> parts -> trays -> products

A tray can hold parts from several animals, so a product *may* contain parts of any of them.
