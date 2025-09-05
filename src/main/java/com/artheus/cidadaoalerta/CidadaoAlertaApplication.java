package com.artheus.cidadaoalerta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.processing.Generated;


@SpringBootApplication
@Generated("jacoco.ignore") //ignora nos reports do jacoco
public class CidadaoAlertaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CidadaoAlertaApplication.class, args);
	}

}
