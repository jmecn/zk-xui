package net.jmecn.zkxui.client;

import org.apache.zookeeper.KeeperException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.vo.Env;
import net.jmecn.zkxui.client.vo.LeafBean;
import net.jmecn.zkxui.client.vo.ZkNode;

/**
 * @title ZkXuiApp
 * @author yanmaoyuan
 * @date 2020年5月2日
 * @version 1.0
 */
@Slf4j
public class ZkXuiApp {

    private Env env;

    private ZkClient client;

    private String currentPath = ZkClient.ZK_ROOT_NODE;

    public ZkXuiApp(Env env) {
        this.env = env;
        this.client = new ZkClient(env.getZkServers());
    }

    public Env getEnv() {
        return env;
    }

    public boolean connect() {
        if (client.getZookeeper() == null) {
            log.warn("Connection to {} failed.", env);
            return false;
        }
        return true;
    }

    public void disconnect() {
        client.closeZooKeeper();
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public boolean isRootNode() {
        return ZkClient.ZK_ROOT_NODE.equals(currentPath);
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public String getParentPath() {
        return getParentPath(currentPath);
    }

    public String getParentPath(String path) {
        String parentPath = path.substring(0, path.lastIndexOf("/"));
        if (parentPath.equals("")) {
            parentPath = "/";
        }
        return parentPath;
    }

    public ZkNode list() {
        return list(currentPath);
    }

    public ZkNode list(String path) {
        try {
            ZkNode zkNode = client.listNodeEntries(path);
            return zkNode;
        } catch (KeeperException | InterruptedException ex) {
            log.error("Failed getting ZkNode, path={}", path, ex);
            return null;
        }
    }

    public void addNode(String node) {
        addNode(currentPath, node);
    }

    public void addNode(String path, String node) {
        try {
            if ("/".equals(path)) {
                client.createFolder("/" + node, "foo", "bar");
            } else {
                client.createFolder(path + "/" + node, "foo", "bar");
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("Failed add node, path={}, node={}", path, node, e);
        }
    }

    public void addProperty(String key, String value) {
        addProperty(currentPath, key, value);
    }

    public void addProperty(String path, String key, String value) {
        try {
            client.createNode(path, key, value);
        } catch (KeeperException | InterruptedException e) {
            log.error("Failed add property, path={}, key={}, value={}", path, key, value, e);
        }
    }

    public void updateProperty(String key, String value) {
        updateProperty(currentPath, key, value);
    }

    public void updateProperty(String path, String key, String value) {
        try {
            client.setPropertyValue(path, key, value);
        } catch (KeeperException | InterruptedException e) {
            log.error("Failed update property, path={}, key={}, value={}", path, key, value, e);
        }
    }

    public void deleteNode(List<String> nodes) {
        deleteNode(currentPath, nodes);
    }

    public void deleteNode(String path, List<String> nodes) {
        try {
            List<String> pathList = new ArrayList<>();
            for (String node : nodes) {
                if ("/".equals(path)) {
                    pathList.add("/" + node);
                } else {
                    pathList.add(path + "/" + node);
                }
            }
            // 删除节点
            client.deleteFolders(pathList);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteProperties(List<String> keys) {
        deleteProperties(currentPath, keys);
    }

    public void deleteProperties(String path, List<String> keys) {
        try {
            List<String> pathList = new ArrayList<>();
            for (String key : keys) {
                if ("/".equals(path)) {
                    pathList.add("/" + key);
                } else {
                    pathList.add(path + "/" + key);
                }
            }
            // 删除节点
            client.deleteLeaves(pathList);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String export() {
        return export(currentPath);
    }

    public String export(String path) {
        StringBuilder output = new StringBuilder();
        output.append("#App Config Dashboard (ACD) dump created on :").append(new Date()).append("\n");
        Set<LeafBean> leaves;
        try {
            leaves = client.exportTree(path);
            for (LeafBean leaf : leaves) {
                output.append(leaf.getPath()).append('=').append(leaf.getName()).append('=')
                        .append(client.externalizeNodeValue(leaf.getValue())).append('\n');
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        return output.toString();
    }

    public void importData(InputStream in, Boolean overwrite) {
        List<String> importFile = new ArrayList<>();

        try {
            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String inputLine;
            Integer lineCnt = 0;
            while ((inputLine = br.readLine()) != null) {
                lineCnt++;
                // Empty or comment?
                if (inputLine.trim().equals("") || inputLine.trim().startsWith("#")) {
                    continue;
                }
                if (inputLine.startsWith("-")) {
                    // DO nothing.
                } else if (!inputLine.matches("/.+=.+=.*")) {
                    log.debug("Invalid format at line {}: {}", lineCnt, inputLine);
                    continue;
                }

                importFile.add(inputLine);
            }
            br.close();
        } catch (IOException e) {
            log.error("Failed read import data", e);
        }

        importData(importFile, overwrite);
    }

    public void importData(String data, Boolean overwrite) {
        List<String> importFile = new ArrayList<>();

        data = data.replaceAll("\r", "");// replace window \r\n to \n

        String[] lines = data.split("\n");
        Integer lineCnt = 0;
        for (String inputLine : lines) {
            lineCnt++;
            // Empty or comment?
            if (inputLine.trim().equals("") || inputLine.trim().startsWith("#")) {
                continue;
            }
            if (inputLine.startsWith("-")) {
                // DO nothing.
            } else if (!inputLine.matches("/.+=.+=.*")) {
                log.debug("Invalid format at line {}: {}", lineCnt, inputLine);
                continue;
            }

            importFile.add(inputLine);
        }

        importData(importFile, overwrite);
    }

    private void importData(List<String> lines, Boolean overwrite) {
        if (lines == null || lines.size() == 0) {
            return;
        }

        try {
            client.importData(lines, overwrite);
        } catch (InterruptedException | KeeperException e) {
            log.error("Failed import data.", e);
        }
    }

}
