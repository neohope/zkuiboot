/**
 *
 * Copyright (c) 2014, Deem Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.neohope.zkui.controller;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import com.deem.zkui.utils.ZooKeeperUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.deem.zkui.utils.LdapAuth;
import java.util.Arrays;
import java.util.Date;

@RestController
public class Login extends BaseController{

    private final static Logger logger = LoggerFactory.getLogger(Login.class);
    
    private static String uptime=new Date().toString();
	
	@Value("${zkui.loginMessage}")
	private String loginMessage;
	
	@Value("${zkui.sessionTimeout}")
	private Integer sessionTimeout;
	
	@Value("${zkui.ldapAuth}")
	private Boolean ldapAuth;
	
	@Value("${zkui.ldapUrl}")
	private String ldapUrl;
	
	@Value("${zkui.ldapDomain}")
	private String ldapDomain;
	
	@Value("${zkui.ldapRoleSet}")
	private String ldapRoleSet;
	
	@Value("${zkui.userSet}")
	private String userSet;

    @GetMapping("/login")
    public ModelAndView doGet(HttpSession session, ModelAndView mv) throws ServletException, IOException {
        logger.debug("Login Action!");
    	mv.setViewName("login");
    	mv.addObject("uptime", uptime);
    	mv.addObject("loginMessage", loginMessage);

        return mv;
    }

    @PostMapping("/login")
    public ModelAndView doPost(HttpSession session, ModelAndView mv,
    		@RequestParam(name="username",required=false) String username, 
    		@RequestParam(name="password",required=false) String password) throws ServletException, IOException {
        logger.debug("Login Post Action!");
        try {
            session.setMaxInactiveInterval(sessionTimeout);
            //TODO: Implement custom authentication logic if required.
            String role = null;
            Boolean authenticated = false;
            //if ldap is provided then it overrides roleset.
            if (ldapAuth) {
                authenticated = new LdapAuth().authenticateUser(ldapUrl, username, password, ldapDomain);
                if (authenticated) {
                    JSONArray jsonRoleSet = (JSONArray) ((JSONObject) new JSONParser().parse(ldapRoleSet)).get("users");
                    for (@SuppressWarnings("unchecked")
					Iterator<JSONObject> it = jsonRoleSet.iterator(); it.hasNext();) {
                        JSONObject jsonUser = it.next();
                        if (jsonUser.get("username") != null && jsonUser.get("username").equals("*")) {
                            role = (String) jsonUser.get("role");
                        }
                        if (jsonUser.get("username") != null && jsonUser.get("username").equals(username)) {
                            role = (String) jsonUser.get("role");
                        }
                    }
                    if (role == null) {
                        role = ZooKeeperUtil.ROLE_USER;
                    }
                }
            } else {
                JSONArray jsonRoleSet = (JSONArray) ((JSONObject) new JSONParser().parse(userSet)).get("users");
                for (@SuppressWarnings("unchecked")
				Iterator<JSONObject> it = jsonRoleSet.iterator(); it.hasNext();) {
                    JSONObject jsonUser = it.next();
                    if (jsonUser.get("username").equals(username) && jsonUser.get("password").equals(password)) {
                        authenticated = true;
                        role = (String) jsonUser.get("role");
                    }
                }
            }
            if (authenticated) {
                logger.info("Login successful: " + username);
                session.setAttribute("authName", username);
                session.setAttribute("authRole", role);
                mv= new ModelAndView("redirect:/home");
            } else {
                session.setAttribute("flashMsg", "Invalid Login");
                mv.setViewName("login");
            }

        } catch (ParseException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            mv.setViewName("error");
            mv.addObject("error", ex.getMessage());
        }
        
        return mv;
    }
}
