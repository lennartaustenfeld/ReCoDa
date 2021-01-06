package org.dice_research.opal.licenses.demo;

import org.dice_research.opal.licenses.demo.model.BaseCc;
import org.dice_research.opal.licenses.demo.model.BaseCcLcc;
import org.dice_research.opal.licenses.demo.model.BaseEdpLcm;
import org.dice_research.opal.licenses.demo.model.Bases;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String... args) {
		initialize();
		SpringApplication.run(Application.class, args);
	}

	private static void initialize() {
		Bases.INSTANCE.initializeBase(new BaseCc());
		Bases.INSTANCE.initializeBase(new BaseCcLcc());
		Bases.INSTANCE.initializeBase(new BaseEdpLcm());
	}

}