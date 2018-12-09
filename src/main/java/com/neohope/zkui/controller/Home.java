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

import com.deem.zkui.dao.Dao;
import com.deem.zkui.utils.ServletUtil;
import com.deem.zkui.utils.ZooKeeperUtil;
import com.deem.zkui.vo.LeafBean;
import com.deem.zkui.vo.ZKNode;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Home {

    private final static Logger logger = LoggerFactory.getLogger(Home.class);
	
	@Value("${zkui.scmRepo}")
	private String scmRepo;
	
	@Value("${zkui.scmRepoPath}")
	private String scmRepoPath;
	
    @GetMapping("/home")
    public ModelAndView doGet(HttpSession session, ModelAndView mv, HttpServletRequest request,
    		@RequestParam(name="zkPath",required=false) String zkPath, 
    		@RequestParam(name="navigate",required=false) String navigate) {
        logger.debug("Home Get Action!");
        try {

            ZooKeeper zk = ServletUtil.getZookeeper(session);
            List<String> nodeLst;
            List<LeafBean> leafLst;
            String currentPath, parentPath, displayPath;
            String authRole = (String) session.getAttribute("authRole");
            if (authRole == null) {
                authRole = ZooKeeperUtil.ROLE_USER;
            }

            if (zkPath == null || zkPath.equals("/")) {
                mv.addObject("zkpath", "/");
                ZKNode zkNode = ZooKeeperUtil.listNodeEntries(zk, "/", authRole);
                nodeLst = zkNode.getNodeLst();
                leafLst = zkNode.getLeafBeanLSt();
                currentPath = "/";
                displayPath = "/";
                parentPath = "/";
            } else {
                mv.addObject("zkPath", zkPath);
                ZKNode zkNode = ZooKeeperUtil.listNodeEntries(zk, zkPath, authRole);
                nodeLst = zkNode.getNodeLst();
                leafLst = zkNode.getLeafBeanLSt();
                currentPath = zkPath + "/";
                displayPath = zkPath;
                parentPath = zkPath.substring(0, zkPath.lastIndexOf("/"));
                if (parentPath.equals("")) {
                    parentPath = "/";
                }
            }

            mv.setViewName("home");
            if (session.getAttribute("flashMsg") != null) {
            	mv.addObject("flashMsg", session.getAttribute("flashMsg"));
                session.setAttribute("flashMsg", null);
            }
            mv.addObject("authName", session.getAttribute("authName"));
            mv.addObject("authRole", session.getAttribute("authRole"));
            mv.addObject("displayPath", displayPath);
            mv.addObject("parentPath", parentPath);
            mv.addObject("currentPath", currentPath);
            mv.addObject("nodeLst", nodeLst);
            mv.addObject("leafLst", leafLst);
            mv.addObject("breadCrumbLst", displayPath.split("/"));
            mv.addObject("scmRepo", scmRepo);
            mv.addObject("scmRepoPath", scmRepoPath);
            mv.addObject("navigate", navigate);

        } catch (KeeperException | InterruptedException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            mv.setViewName("error");
            mv.addObject("error", ex.getMessage());
        }

        return mv;
    }

    @PostMapping("/home")
    public ModelAndView doPost(HttpSession session, ModelAndView mv, HttpServletRequest request,
    		@RequestParam(name="action",required=false) String action, 
    		@RequestParam(name="currentPath",required=false) String currentPath,
    		@RequestParam(name="displayPath",required=false) String displayPath,
    		@RequestParam(name="newProperty",required=false) String newProperty,
    		@RequestParam(name="newValue",required=false) String newValue,
    		@RequestParam(name="newNode",required=false) String newNode,
    		@RequestParam(name="searchStr",required=false) String searchStr,
    		@RequestParam(name="authRole",required=false) String authRole){
        logger.debug("Home Post Action!");
        try {
            Dao dao = new Dao();
            String[] nodeChkGroup = request.getParameterValues("nodeChkGroup");
            String[] propChkGroup = request.getParameterValues("propChkGroup");
            searchStr = searchStr.trim();
            
            if (authRole == null) {
            	authRole = (String) session.getAttribute("authRole");
            }
            if (authRole == null) {
                authRole = ZooKeeperUtil.ROLE_USER;
            }

            switch (action) {
                case "Save Node":
                    if (!newNode.equals("") && !currentPath.equals("") && authRole.equals(ZooKeeperUtil.ROLE_ADMIN)) {
                        //Save the new node.
                        ZooKeeperUtil.createFolder(currentPath + newNode, "foo", "bar", ServletUtil.getZookeeper(session));
                        session.setAttribute("flashMsg", "Node created!");
                        dao.insertHistory((String) session.getAttribute("authName"), request.getRemoteAddr(), "Creating node: " + currentPath + newNode);
                    }
                    mv= new ModelAndView("redirect:/home?zkPath="+ displayPath);
                    break;
                case "Save Property":
                    if (!newProperty.equals("") && !currentPath.equals("") && authRole.equals(ZooKeeperUtil.ROLE_ADMIN)) {
                        //Save the new node.
                        ZooKeeperUtil.createNode(currentPath, newProperty, newValue, ServletUtil.getZookeeper(session));
                        session.setAttribute("flashMsg", "Property Saved!");
                        if (ZooKeeperUtil.checkIfPwdField(newProperty)) {
                            newValue = ZooKeeperUtil.SOPA_PIPA;
                        }
                        dao.insertHistory((String) session.getAttribute("authName"), request.getRemoteAddr(), "Saving Property: " + currentPath + "," + newProperty + "=" + newValue);
                    }
                    mv= new ModelAndView("redirect:/home?zkPath="+ displayPath);
                    break;
                case "Update Property":
                    if (!newProperty.equals("") && !currentPath.equals("") && authRole.equals(ZooKeeperUtil.ROLE_ADMIN)) {
                        //Save the new node.
                        ZooKeeperUtil.setPropertyValue(currentPath, newProperty, newValue, ServletUtil.getZookeeper(session));
                        session.setAttribute("flashMsg", "Property Updated!");
                        if (ZooKeeperUtil.checkIfPwdField(newProperty)) {
                            newValue = ZooKeeperUtil.SOPA_PIPA;
                        }
                        dao.insertHistory((String) session.getAttribute("authName"), request.getRemoteAddr(), "Updating Property: " + currentPath + "," + newProperty + "=" + newValue);
                    }
                    mv= new ModelAndView("redirect:/home?zkPath="+ displayPath);
                    break;
                case "Search":
                    Set<LeafBean> searchResult = ZooKeeperUtil.searchTree(searchStr, ServletUtil.getZookeeper(session), authRole);
                    mv.addObject("searchResult", searchResult);
                    mv.setViewName("search");
                    break;
                case "Delete":
                    if (authRole.equals(ZooKeeperUtil.ROLE_ADMIN)) {

                        if (propChkGroup != null) {
                            for (String prop : propChkGroup) {
                                List<String> delPropLst = Arrays.asList(prop);
                                ZooKeeperUtil.deleteLeaves(delPropLst, ServletUtil.getZookeeper(session));
                                session.setAttribute("flashMsg", "Delete Completed!");
                                dao.insertHistory((String) session.getAttribute("authName"), request.getRemoteAddr(), "Deleting Property: " + delPropLst.toString());
                            }
                        }
                        if (nodeChkGroup != null) {
                            for (String node : nodeChkGroup) {
                                List<String> delNodeLst = Arrays.asList(node);
                                ZooKeeperUtil.deleteFolders(delNodeLst, ServletUtil.getZookeeper(session));
                                session.setAttribute("flashMsg", "Delete Completed!");
                                dao.insertHistory((String) session.getAttribute("authName"), request.getRemoteAddr(), "Deleting Nodes: " + delNodeLst.toString());
                            }
                        }

                    }
                    mv= new ModelAndView("redirect:/home?zkPath="+ displayPath);
                    break;
                default:
                	mv= new ModelAndView("redirect:/home");
            }

        } catch (InterruptedException | KeeperException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            mv.setViewName("error");
            mv.addObject("error", ex.getMessage());
        }
        
        return mv;
    }
}
