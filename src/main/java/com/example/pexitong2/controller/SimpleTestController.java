package com.example.pexitong2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleTestController {
    
    @GetMapping("/simple")
    public String simpleTest() {
        return "Hello World - Server is running!";
    }
    
    @GetMapping("/")
    public String root() {
        return "Welcome to Pexitong2 Backend!";
    }
} 