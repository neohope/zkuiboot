package com.neohope.zkui.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="com.neohope.zkui.controller.**;com.deem.zkui.dao.**;")
public class Main extends BaseController {
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
