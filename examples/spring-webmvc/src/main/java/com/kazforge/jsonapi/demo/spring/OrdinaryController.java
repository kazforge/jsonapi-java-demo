package com.kazforge.jsonapi.demo.spring;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Unmarked handlers retain Spring's ordinary JSON converters and negotiation. */
@RestController
public final class OrdinaryController {
  @GetMapping("/ordinary")
  public Map<String, String> get() {
    return Map.of("message", "ordinary MVC");
  }

  @PostMapping("/ordinary")
  public Map<String, String> echo(@RequestBody Map<String, String> input) {
    return input;
  }
}
