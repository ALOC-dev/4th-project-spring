package dev.aloc.spring.testcomponents;

import dev.aloc.spring.annotation.Controller;
import dev.aloc.spring.annotation.GetMapping;

@Controller
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
      return "{\"ok\":true,\"from\":\"hello\"}";
  }
}
