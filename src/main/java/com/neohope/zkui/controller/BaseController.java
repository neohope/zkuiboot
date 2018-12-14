package com.neohope.zkui.controller;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BaseController implements ApplicationContextAware{
	
	protected ApplicationContext appContext = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext=applicationContext;
	}
	
    public Object getBean(String name){
        return appContext.getBean(name);
    }
    
    public <T>T getBean(Class<T> clazz){
        return appContext.getBean(clazz);
    }
    
    public <T> T getBean(String name,Class<T> clazz){
        return appContext.getBean(name, clazz);
    }
}
