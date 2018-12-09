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
import javax.servlet.http.HttpSession;

import com.deem.zkui.utils.CmdUtil;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
//@RequestMapping("/")
public class Monitor {

    private final static Logger logger = LoggerFactory.getLogger(Monitor.class);
    
	@Value("${zkui.zkServer}")
	private String zkServer;

    @GetMapping("/monitor")
    public ModelAndView doGet(HttpSession session,ModelAndView mv){
        logger.debug("Monitor Action!");
        try {
            String[] zkServerLst = zkServer.split(",");

            StringBuffer stats = new StringBuffer();
            for (String zkObj : zkServerLst) {
                stats.append("<br/><hr/><br/>").append("Server: ").append(zkObj).append("<br/><hr/><br/>");
                String[] monitorZKServer = zkObj.split(":");
                stats.append(CmdUtil.executeCmd("stat", monitorZKServer[0], monitorZKServer[1]));
                stats.append(CmdUtil.executeCmd("envi", monitorZKServer[0], monitorZKServer[1]));
            }
            mv.setViewName("monitor");
            mv.addObject("stats", stats);

        } catch (IOException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            mv.setViewName("error");
            mv.addObject("error", ex.getMessage());
        }
        
        return mv;
    }
}
