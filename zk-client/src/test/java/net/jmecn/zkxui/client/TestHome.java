package net.jmecn.zkxui.client;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkClient;
import net.jmecn.zkxui.client.vo.LeafBean;
import net.jmecn.zkxui.client.vo.ZkNode;

/**
 * @title TestCLI
 * @author yanmaoyuan
 * @date 2020年5月1日
 * @version 1.0
 */
@Slf4j
public class TestHome {

    private String zkServer = "172.30.70.4:2181,172.30.70.5:2181,172.30.70.6:2181";

    private String huatu_test = "172.30.9.98:2181,172.30.9.99:2181,172.30.9.100:2181";

    //private String zkPath = "/config/weixin-service-dev/application-jdbc.yml";
    private String zkPath = "/";

    @Test
    @Ignore
    public void home() {
        ZkClient client = new ZkClient(huatu_test);
        ZooKeeper zk = client.getZookeeper();
        if (zk == null) {
            System.out.print("Connection failed.");
            return;
        }

        List<String> nodeLst;
        List<LeafBean> leafLst;
        String currentPath, parentPath, displayPath;

        if (zkPath == null) {
            zkPath = "/";
        }

        try {
            ZkNode zkNode = client.listNodeEntries(zkPath);
            nodeLst = zkNode.getNodeLst();
            leafLst = zkNode.getLeafBeanLSt();

            currentPath = zkPath;
            displayPath = zkPath;
            parentPath = zkPath.substring(0, zkPath.lastIndexOf("/"));
            if (parentPath.equals("")) {
                parentPath = "/";
            }

            log.info("displayPath={}", displayPath);
            log.info("parentPath={}", parentPath);
            log.info("currentPath={}", currentPath);
            log.info("nodeLst={}", nodeLst);
            log.info("leafLst={}", leafLst);
            log.info("breadCrumbLst={}", Arrays.asList(displayPath.split("/")));

        } catch (KeeperException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
