package com.clinicore.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClinicoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicoreApplication.class, args);

        System.out.println("DB_URL = " + System.getenv("DB_URL"));
        System.out.println("DB_USER = " + System.getenv("DB_USER"));
        System.out.println("DB_PASS = " + System.getenv("DB_PASS"));
    }

}
