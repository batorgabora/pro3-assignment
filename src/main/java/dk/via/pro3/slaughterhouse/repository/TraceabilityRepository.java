package dk.via.pro3.slaughterhouse.repository;

import dk.via.pro3.slaughterhouse.model.ProductRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TraceabilityRepository {

  private final JdbcTemplate jdbcTemplate;

  // Constructor injection: Spring creates the JdbcTemplate (using the
  // datasource settings in application.properties) and injects it here
  public TraceabilityRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public boolean productExists(long productId) {
    Boolean exists = jdbcTemplate.queryForObject(
        "SELECT EXISTS (SELECT 1 FROM product WHERE product_id = ?)",
        Boolean.class,
        productId);
    return Boolean.TRUE.equals(exists);
  }

  public boolean animalExists(String registrationNumber) {
    Boolean exists = jdbcTemplate.queryForObject(
        "SELECT EXISTS (SELECT 1 FROM animal WHERE registration_number = ?)",
        Boolean.class,
        registrationNumber);
    return Boolean.TRUE.equals(exists);
  }

  /** Registration numbers of all animals that contributed parts to the product. */
  public List<String> findAnimalRegistrationsByProduct(long productId) {
    String sql = """
                SELECT DISTINCT a.registration_number
                FROM product_part pp
                JOIN part p   ON p.part_id = pp.part_id
                JOIN animal a ON a.animal_id = p.animal_id
                WHERE pp.product_id = ?
                ORDER BY a.registration_number
                """;
    return jdbcTemplate.queryForList(sql, String.class, productId);
  }

  /** All products that contain at least one part from the animal. */
  public List<ProductRecord> findProductsByAnimal(String registrationNumber) {
    String sql = """
                SELECT DISTINCT pr.product_id, pr.description
                FROM animal a
                JOIN part p          ON p.animal_id = a.animal_id
                JOIN product_part pp ON pp.part_id = p.part_id
                JOIN product pr      ON pr.product_id = pp.product_id
                WHERE a.registration_number = ?
                ORDER BY pr.product_id
                """;
    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new ProductRecord(rs.getLong("product_id"), rs.getString("description")),
        registrationNumber);
  }
}