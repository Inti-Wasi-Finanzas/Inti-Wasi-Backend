package com.intiwasi.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class IntiWasiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntiWasiApplication.class, args);
    }

}
