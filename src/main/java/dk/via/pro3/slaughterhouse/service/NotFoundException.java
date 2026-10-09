package dk.via.pro3.slaughterhouse.service;

public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}