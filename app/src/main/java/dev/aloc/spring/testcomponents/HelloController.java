package dev.aloc.spring.testcomponents;

import dev.aloc.spring.annotation.Controller;
import dev.aloc.spring.annotation.GetMapping;

// 구현한 뒤 import 필요!!
@Controller
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello At Sunset Next Year A Blooming Spring!!!";
  }

}
