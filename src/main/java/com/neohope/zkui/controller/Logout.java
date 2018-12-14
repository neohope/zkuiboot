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
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.zookeeper.ZooKeeper;
import com.deem.zkui.utils.ServletUtil;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Logout extends BaseController{

    private final static Logger logger = LoggerFactory.getLogger(Logout.class);

    @GetMapping("/logout")
    public ModelAndView doGet(HttpSession session, ModelAndView mv) throws ServletException, IOException {
    	logger.debug("Logout Action!");
        ZkConfig cfg = (ZkConfig)getBean("ZkConfig");
        try {
            ZooKeeper zk = ServletUtil.getZookeeper(session, cfg);
            session.invalidate();
            zk.close();
            mv= new ModelAndView("redirect:/login");
        } catch (InterruptedException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            mv.setViewName("error");
            mv.addObject("error", ex.getMessage());
        }

        return mv;
    }
}
