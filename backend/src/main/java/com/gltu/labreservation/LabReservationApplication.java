package com.gltu.labreservation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.gltu.labreservation.mapper")
@SpringBootApplication
public class LabReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabReservationApplication.class, args);
    }
}

