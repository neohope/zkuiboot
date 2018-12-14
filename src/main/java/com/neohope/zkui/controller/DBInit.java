package com.neohope.zkui.controller;

import javax.servlet.http.HttpSession;

import com.deem.zkui.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DBInit extends BaseController{

    private final static Logger logger = LoggerFactory.getLogger(DBInit.class);

    @GetMapping("/dbinit")
    public String doGet(HttpSession session){
        logger.debug("dbinit Action!");
    	Dao dao = (Dao)getBean("Dao");
    	dao.checkNCreate();

        return "dbinit ok";
    }
}
