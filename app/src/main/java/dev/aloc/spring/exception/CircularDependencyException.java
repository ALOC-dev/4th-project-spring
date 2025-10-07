package dev.aloc.spring.exception;

public class CircularDependencyException extends RuntimeException {

  public CircularDependencyException(String message) {
    super(message);
  }
}
