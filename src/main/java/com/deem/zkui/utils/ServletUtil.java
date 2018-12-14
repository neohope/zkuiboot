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
package com.deem.zkui.utils;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.http.HttpSession;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neohope.zkui.controller.ZkConfig;

public class ServletUtil {
	
    private final static Logger logger = LoggerFactory.getLogger(ServletUtil.class);

    public static ZooKeeper getZookeeper(HttpSession session,ZkConfig cfg) {
        try {
        	
        	String[] zkServerLst = cfg.zkServer.split(",");
            ZooKeeper zk = (ZooKeeper) session.getAttribute("zk");
            if (zk == null || zk.getState() != ZooKeeper.States.CONNECTED) {
                zk = ZooKeeperUtil.createZKConnection(zkServerLst[0], cfg.zkSessionTimeout);
                ZooKeeperUtil.setDefaultAcl(cfg.defaultAcl);
                if (zk.getState() != ZooKeeper.States.CONNECTED) {
                    session.setAttribute("zk", null);
                } else {
                    session.setAttribute("zk", zk);
                }

            }
            return zk;
        } catch (IOException | InterruptedException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
        }
        return null;
    }

    public static void closeZookeeper(ZooKeeper zk) {
        try {
            zk.close();
        } catch (Exception ex) {
            logger.error("Error in closing zk,will cause problem in zk! " + ex.getMessage());
        }

    }

    public static String externalizeNodeValue(byte[] value) {
        return value == null ? "" : new String(value).replaceAll("\\n", "\\\\n").replaceAll("\\r", "");
        // We might want to BASE64 encode it
    }
}
