package com.neohope.zkui.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.deem.zkui.utils.ServletUtil;

@SpringBootApplication
public class Main {
	@Value("${zkui.zkServer}")
	public void setZkServer(String zkServer) {
		ServletUtil.zkServer = zkServer;
	}
	
	@Value("${zkui.zkSessionTimeout}")
	public void setZkSessionTimeout(Integer zkSessionTimeout) {
		ServletUtil.zkSessionTimeout = zkSessionTimeout;
	}
	
	@Value("${zkui.defaultAcl}")
	public void setDefaultAcl(String defaultAcl) {
		ServletUtil.defaultAcl = defaultAcl;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
