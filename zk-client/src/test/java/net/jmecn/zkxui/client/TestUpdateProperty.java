/**
 * Huatu.com Inc.
 * Copyright (c) 2014-2020 All Rights Reserved.
 */
package net.jmecn.zkxui.client;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import net.jmecn.zkxui.client.ZkClient;

/**
 * @title TestAddProperty
 * @author yanmaoyuan
 * @date 2020年5月2日
 * @version 1.0
 */
public class TestUpdateProperty {

    // /config/shortUrl-service-dev=foo=bar


    private String huatu_test = "172.30.9.98:2181,172.30.9.99:2181,172.30.9.100:2181";

    @Test
    @Ignore
    // 更新key=value属性
    public void updateProperty() {
        ZkClient client = new ZkClient(huatu_test);
        ZooKeeper zk = client.getZookeeper();
        if (zk == null) {
            System.out.print("Connection failed.");
            return;
        }
        try {
            client.setPropertyValue("/config/skynet-config-dev/", "foo", "bar");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
