package com.chamrong.iecommerce.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Serves custom Thymeleaf error pages for explicit error routes. */
@Controller
@RequestMapping("/error")
public class ErrorPageController {

  @GetMapping("/401")
  public String unauthorized() {
    return "error/401";
  }

  @GetMapping("/403")
  public String forbidden() {
    return "error/403";
  }

  @GetMapping("/404")
  public String notFound() {
    return "error/404";
  }

  @GetMapping("/500")
  public String internalServerError() {
    return "error/500";
  }
}
