package com.example.kservertask;

import org.springframework.boot.SpringApplication;

public class TestKServerTaskApplication {

    public static void main(String[] args) {
        SpringApplication.from(KServerTaskApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
