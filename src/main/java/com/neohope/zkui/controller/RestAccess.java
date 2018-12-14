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

import com.deem.zkui.utils.ServletUtil;
import com.deem.zkui.utils.ZooKeeperUtil;
import com.deem.zkui.vo.LeafBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestAccess extends BaseController{

    private final static Logger logger = LoggerFactory.getLogger(RestAccess.class);
    
	@Value("${zkui.zkServer}")
	private String zkServer;
	
	@Value("${zkui.blockPwdOverRest}")
	private Boolean blockPwdOverRest;

    @GetMapping("/acd/appconfig")
    public void doGet(HttpSession session, HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(name="cluster",required=false) String clusterName, 
    		@RequestParam(name="app",required=false) String appName, 
    		@RequestParam(name="host",required=false) String hostName, 
    		@RequestParam(name="zkPath",required=false) String zkPath) throws ServletException, IOException {
        logger.debug("Rest Action!");
        ZkConfig cfg = (ZkConfig)getBean("ZkConfig");
        ZooKeeper zk = null;
        try {
            String accessRole = ZooKeeperUtil.ROLE_USER;
            if (!blockPwdOverRest) {
                accessRole = ZooKeeperUtil.ROLE_ADMIN;
            }
            StringBuilder resultOut = new StringBuilder();
            String[] propNames = request.getParameterValues("propNames");
            String propValue = "";
            LeafBean propertyNode;

            if (hostName == null) {
                hostName = request.getRemoteAddr();
            }
            zk = ServletUtil.getZookeeper(session,cfg);
            //get the path of the hosts entry.
            LeafBean hostsNode = null;
            //If app name is mentioned then lookup path is appended with it.
            if (appName != null && ZooKeeperUtil.nodeExists(ZooKeeperUtil.ZK_HOSTS + "/" + hostName + ":" + appName, zk)) {
                hostsNode = ZooKeeperUtil.getNodeValue(zk, ZooKeeperUtil.ZK_HOSTS, ZooKeeperUtil.ZK_HOSTS + "/" + hostName + ":" + appName, hostName + ":" + appName, accessRole);
            } else {
                hostsNode = ZooKeeperUtil.getNodeValue(zk, ZooKeeperUtil.ZK_HOSTS, ZooKeeperUtil.ZK_HOSTS + "/" + hostName, hostName, accessRole);
            }

            String lookupPath = hostsNode.getStrValue();
            logger.trace("Root Path:" + lookupPath);
            String[] pathElements = lookupPath.split("/");

            //Form all combinations of search path you want to look up the property in.
            List<String> searchPath = new ArrayList<>();

            StringBuilder pathSubSet = new StringBuilder();
            for (String pathElement : pathElements) {
                pathSubSet.append(pathElement);
                pathSubSet.append("/");
                searchPath.add(pathSubSet.substring(0, pathSubSet.length() - 1));
            }

            //You specify a cluster or an app name to group.
            if (clusterName != null && appName == null) {
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + hostName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + clusterName, zk)) {
                    searchPath.add(lookupPath + "/" + clusterName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + clusterName + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + clusterName + "/" + hostName);
                }

            } else if (appName != null && clusterName == null) {

                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + hostName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + appName, zk)) {
                    searchPath.add(lookupPath + "/" + appName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + appName + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + appName + "/" + hostName);
                }

            } else if (appName != null && clusterName != null) {
                //Order in which these paths are listed is important as the lookup happens in that order.
                //Precedence is give to cluster over app.
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + hostName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + appName, zk)) {
                    searchPath.add(lookupPath + "/" + appName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + appName + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + appName + "/" + hostName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + clusterName, zk)) {
                    searchPath.add(lookupPath + "/" + clusterName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + clusterName + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + clusterName + "/" + hostName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + clusterName + "/" + appName, zk)) {
                    searchPath.add(lookupPath + "/" + clusterName + "/" + appName);
                }
                if (ZooKeeperUtil.nodeExists(lookupPath + "/" + clusterName + "/" + appName + "/" + hostName, zk)) {
                    searchPath.add(lookupPath + "/" + clusterName + "/" + appName + "/" + hostName);
                }

            }

            //Search the property in all lookup paths.
            for (String propName : propNames) {
                propValue = null;
                for (String path : searchPath) {
                    logger.trace("Looking up " + path);
                    propertyNode = ZooKeeperUtil.getNodeValue(zk, path, path + "/" + propName, propName, accessRole);
                    if (propertyNode != null) {
                        propValue = propertyNode.getStrValue();
                    }
                }
                if (propValue != null) {
                    resultOut.append(propName).append("=").append(propValue).append("\n");
                }

            }

            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.write(resultOut.toString());
            }

        } catch (KeeperException | InterruptedException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
        } finally {
            if (zk != null) {
                ServletUtil.closeZookeeper(zk);
            }
        }

    }
}
