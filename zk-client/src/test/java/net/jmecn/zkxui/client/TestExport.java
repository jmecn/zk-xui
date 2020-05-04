package net.jmecn.zkxui.client;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.Set;

import net.jmecn.zkxui.client.vo.LeafBean;

/**
 * @title TestExport
 * @author yanmaoyuan
 * @date 2020年5月1日
 * @version 1.0
 */
public class TestExport {


    private String zkServer = "172.30.70.4:2181,172.30.70.5:2181,172.30.70.6:2181";

    private String huatu_test = "172.30.9.98:2181,172.30.9.99:2181,172.30.9.100:2181";

    //private String zkPath = "/config/weixin-service-dev/application-jdbc.yml";
    private String zkPath = "/config/skynet-config-dev";

    @Test
    @Ignore
    public void export() {
        ZkClient client = new ZkClient(huatu_test);
        ZooKeeper zk = client.getZookeeper();
        if (zk == null) {
            System.out.print("Connection failed.");
            return;
        }

        StringBuilder output = new StringBuilder();
        output.append("#App Config Dashboard (ACD) dump created on :").append(new Date()).append("\n");
        Set<LeafBean> leaves;
        try {
            leaves = client.exportTree(zkPath);
            for (LeafBean leaf : leaves) {
                output.append(leaf.getPath()).append('=').append(leaf.getName()).append('=').append(client.externalizeNodeValue(leaf.getValue())).append('\n');
            }
            System.out.println(output.toString());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}