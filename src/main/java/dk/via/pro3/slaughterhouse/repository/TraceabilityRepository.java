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

  /**
   * Registration numbers of all animals that may be part of the product:
   * product -> trays it was packed from -> parts in those trays -> animals.
   */
  public List<String> findAnimalRegistrationsByProduct(long productId) {
    String sql = """
                SELECT DISTINCT a.registration_number
                FROM product_tray pt
                JOIN part p   ON p.tray_id = pt.tray_id
                JOIN animal a ON a.animal_id = p.animal_id
                WHERE pt.product_id = ?
                ORDER BY a.registration_number
                """;
    return jdbcTemplate.queryForList(sql, String.class, productId);
  }

  /**
   * All products that may contain parts of the animal:
   * animal -> its parts -> the trays they are in -> products packed from those trays.
   */
  public List<ProductRecord> findProductsByAnimal(String registrationNumber) {
    String sql = """
                SELECT DISTINCT pr.product_id, pr.description
                FROM animal a
                JOIN part p          ON p.animal_id = a.animal_id
                JOIN product_tray pt ON pt.tray_id = p.tray_id
                JOIN product pr      ON pr.product_id = pt.product_id
                WHERE a.registration_number = ?
                ORDER BY pr.product_id
                """;
    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new ProductRecord(rs.getLong("product_id"), rs.getString("description")),
        registrationNumber);
  }
}