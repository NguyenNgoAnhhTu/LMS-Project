package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Hello world!
 *
 */
//@SpringBootApplication // This annotation is used to mark the main class of a Spring Boot application
@SpringBootApplication()
public class App
{
    public static void main( String[] args )
    {
        // This will start the Spring Boot application
        SpringApplication.run(App.class, args);
    }
}
