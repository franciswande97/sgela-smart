package com.app.sgelasmart;

import org.springframework.boot.SpringApplication;

public class TestSgelasmartApplication {

	public static void main(String[] args) {
		SpringApplication.from(SgelasmartApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
