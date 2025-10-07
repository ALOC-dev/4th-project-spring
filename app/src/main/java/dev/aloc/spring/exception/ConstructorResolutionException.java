package dev.aloc.spring.exception;

public class ConstructorResolutionException extends RuntimeException {

  public ConstructorResolutionException(String message) {
    super(message);
  }
}
