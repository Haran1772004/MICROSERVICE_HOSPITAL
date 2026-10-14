package com.hospital.hospitalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Starts the hospital-service. It owns departments, doctors, patients and patient
 * addresses.
 */
@SpringBootApplication
@EnableCaching
public class HospitalServiceApplication {

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(HospitalServiceApplication.class, args);
    }
}
