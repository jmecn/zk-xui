/**
 * Huatu.com Inc.
 * Copyright (c) 2014-2020 All Rights Reserved.
 */
package net.jmecn.zkxui.client;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import net.jmecn.zkxui.client.ZkClient;

/**
 * @title TestDelete
 * @author yanmaoyuan
 * @date 2020年5月2日
 * @version 1.0
 */
public class TestDelete {

    private String huatu_test = "172.30.9.98:2181,172.30.9.99:2181,172.30.9.100:2181";

    @Test
    @Ignore
    public void testDelete() {
        ZkClient client = new ZkClient(huatu_test);
        ZooKeeper zk = client.getZookeeper();
        if (zk == null) {
            System.out.print("Connection failed.");
            return;
        }
        
        try {
            // 删除属性
            client.deleteLeaves(Collections.singletonList("/config/skynet-config-dev/key3"));
            // 删除节点
            client.deleteFolders(Collections.singletonList("/config/skynet-config-dev/dir1/dir2"));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}