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

import java.util.List;
import javax.servlet.http.HttpSession;

import com.deem.zkui.dao.Dao;
import com.deem.zkui.domain.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class ChangeLog extends BaseController{

    private final static Logger logger = LoggerFactory.getLogger(ChangeLog.class);

    @GetMapping("/history")
    public ModelAndView doGet(HttpSession session, ModelAndView mv){
        logger.debug("History Get Action!");
        Dao dao = (Dao)getBean("Dao");
        List<History> historyLst = dao.fetchHistoryRecords();
        mv.setViewName("history");
        mv.addObject("historyLst", historyLst);
        mv.addObject("historyNode", "");

        return mv;
    }

    @PostMapping("/history")
    public ModelAndView doPost(HttpSession session, ModelAndView mv,
    		@RequestParam(name="action",required=false) String action,
    		@RequestParam(name="historyNode",required=false) String historyNode){
        logger.debug("History Post Action!");
        
        Dao dao = (Dao)getBean("Dao");
        List<History> historyLst;
        if (action.equals("showhistory")) {
            historyLst = dao.fetchHistoryRecordsByNode("%" + historyNode + "%");
            mv.setViewName("history");
            mv.addObject("historyLst", historyLst);
            mv.addObject("historyNode", historyNode);

        } else {
        	mv= new ModelAndView("redirect:/history");
        }
        
        return mv;
    }
}
