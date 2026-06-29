package com.npl.na_ponta_do_lapis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class NaPontaDoLapisApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
                
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );


		SpringApplication.run(NaPontaDoLapisApplication.class, args);

	}

}
