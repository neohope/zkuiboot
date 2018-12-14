package com.neohope.zkui.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZkConfig {
    @Value("${zkui.zkServer}")
    public String zkServer;
    
	public Integer zkSessionTimeout;
    
	@Value("${zkui.zkSessionTimeout}")
	public void setZkSessionTimeout(Integer zkSessionTimeout) {
		this.zkSessionTimeout = zkSessionTimeout*1000;
	}
	
	@Value("${zkui.defaultAcl}")
	public String defaultAcl;
	
	@Bean(name="ZkConfig")
    public ZkConfig getZkConfig(){
		return new ZkConfig();
	}
	
	public ZkConfig(){
	}
}
