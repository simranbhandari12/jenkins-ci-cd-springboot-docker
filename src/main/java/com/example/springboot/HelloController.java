package com.example.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/DevOps")
	public String DevOps() {
		return "Flow; fast feedback; learning & experimentation";
	}

	@PostMapping("/DevOps")
	public String DevOps(@RequestParam(value = "name", defaultValue = "Unit") String name) {
		return name + " testing";
	}

	@PostMapping("/YorkU")
	public String YorkU(@RequestParam(value = "name", defaultValue = "Zoom") String name) {
		return name + " Day";
	}
}
