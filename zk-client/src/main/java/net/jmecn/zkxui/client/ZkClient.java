package net.jmecn.zkxui.client;

import net.jmecn.zkxui.client.vo.*;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

/**
 * @title ZkClient
 * @author yanmaoyuan
 * @date 2020年5月2日
 * @version 1.0
 */
@Slf4j
public class ZkClient {

    private ZooKeeper zk = null;

    private Integer zkSessionTimeout = 5000;

    private final static int CONNECT_WAIT = 100;

    private String zkServer = "127.0.0.1:2181";

    public final static Integer MAX_CONNECT_ATTEMPT = 100;

    public final static String ZK_ROOT_NODE = "/";

    // ZK internal folder (quota info, etc) - have to stay away from it
    public final static String ZK_SYSTEM_NODE = "zookeeper";

    public ZkClient() {
    }

    public ZkClient(String zkServer) {
        this.zkServer = zkServer;
    }

    public ZooKeeper getZookeeper() {
        String defaultAcl = "";

        try {
            if (zk == null || zk.getState() != ZooKeeper.States.CONNECTED) {
                zk = createZKConnection(zkServer, zkSessionTimeout);
                setDefaultAcl(defaultAcl);
                if (zk.getState() != ZooKeeper.States.CONNECTED) {
                    closeZooKeeper(zk);
                    zk = null;
                }
            }
            return zk;
        } catch (IOException | InterruptedException ex) {
            log.error("Failed connect zookeeper", ex);
        }
        return null;
    }

    public ZooKeeper createZKConnection(String url, Integer zkSessionTimeout) throws IOException, InterruptedException {
        Integer connectAttempt = 0;
        ZKClientConfig zkClientConfig = new ZKClientConfig();
        if (zkSessionTimeout != null) {
            zkClientConfig.setProperty("zookeeper.request.timeout", String.valueOf(zkSessionTimeout));
        }
        ZooKeeper zk = new ZooKeeper(url, zkSessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                log.debug("Connecting to ZK, event={}", event);
            }
        }, zkClientConfig);

        // Wait till connection is established.
        int maxConnectAttempt = zkSessionTimeout / CONNECT_WAIT;
        if (maxConnectAttempt <= 0) {
            maxConnectAttempt = MAX_CONNECT_ATTEMPT;
        }
        while (zk.getState() != ZooKeeper.States.CONNECTED) {
            Thread.sleep(CONNECT_WAIT);
            connectAttempt++;
            if (connectAttempt >= maxConnectAttempt) {
                log.info("Connection timeout:{}", zkSessionTimeout);
                break;
            }
        }
        return zk;
    }

    private ArrayList<ACL> defaultAcl = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    private ArrayList<ACL> defaultAcl() {
        return defaultAcl;
    }

    public void setZkSessionTimeout(Integer timeout) {
        if (timeout == null) {
            throw new IllegalArgumentException("zkSessionTimeout is null");
        }
        this.zkSessionTimeout = timeout;
    }

    public void setDefaultAcl(String jsonAcl) {
        if (jsonAcl == null || jsonAcl.trim().length() == 0) {
            log.debug("Using UNSAFE ACL. Anyone on your LAN can change your Zookeeper data");
            defaultAcl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
            return;
        }
        // Don't let things happen in a half-baked state, build the new ACL and then set it into defaultAcl
        ArrayList<ACL> newDefault = new ArrayList<>();
        try {
            JSONArray acls = (JSONArray) ((JSONObject) new JSONParser().parse(jsonAcl)).get("acls");
            for (Iterator<?> it = acls.iterator(); it.hasNext();) {
                JSONObject acl = (JSONObject) it.next();
                String scheme = ((String) acl.get("scheme")).trim();
                String id = ((String) acl.get("id")).trim();
                int perms = 0;
                String permStr = ((String) acl.get("perms")).toLowerCase().trim();
                for (char c : permStr.toCharArray()) {
                    switch (c) {
                    case 'a':
                        perms += ZooDefs.Perms.ADMIN;
                        break;
                    case 'c':
                        perms += ZooDefs.Perms.CREATE;
                        break;
                    case 'd':
                        perms += ZooDefs.Perms.DELETE;
                        break;
                    case 'r':
                        perms += ZooDefs.Perms.READ;
                        break;
                    case 'w':
                        perms += ZooDefs.Perms.WRITE;
                        break;
                    case '*':
                        perms += ZooDefs.Perms.ALL;
                        break;
                    default:
                        throw new RuntimeException("Illegal permission character in ACL " + c);
                    }
                }
                newDefault.add(new ACL(perms, new Id(scheme, id)));
            }
        } catch (ParseException e) {
            // Throw it all the way up to the error handlers
            throw new RuntimeException("Unable to parse default ACL " + jsonAcl, e);
        }
        defaultAcl = newDefault;
    }

    public Set<LeafBean> searchTree(String searchString, String authRole) throws InterruptedException, KeeperException {
        // Export all nodes and then search.
        Set<LeafBean> searchResult = new TreeSet<>();
        Set<LeafBean> leaves = new TreeSet<>();
        exportTreeInternal(leaves, ZK_ROOT_NODE);
        for (LeafBean leaf : leaves) {
            String leafValue = externalizeNodeValue(leaf.getValue());
            if (leaf.getPath().contains(searchString) || leaf.getName().contains(searchString)
                    || leafValue.contains(searchString)) {
                searchResult.add(leaf);
            }
        }
        return searchResult;

    }

    public String externalizeNodeValue(byte[] value) {
        return value == null ? "" : new String(value).replaceAll("\\n", "\\\\n").replaceAll("\\r", "");
    }

    public Set<LeafBean> exportTree(String zkPath) throws InterruptedException, KeeperException {
        // 1. Collect nodes
        long startTime = System.currentTimeMillis();
        Set<LeafBean> leaves = new TreeSet<>();
        exportTreeInternal(leaves, zkPath);
        long estimatedTime = System.currentTimeMillis() - startTime;
        log.trace("Elapsed Time in Secs for Export: " + estimatedTime / 1000);
        return leaves;
    }

    private void exportTreeInternal(Set<LeafBean> entries, String path) throws InterruptedException, KeeperException {
        // 1. List leaves
        entries.addAll(this.listLeaves(path));
        // 2. Process folders
        for (String folder : this.listFolders(path)) {
            exportTreeInternal(entries, this.getNodePath(path, folder));
        }
    }

    public void importData(List<String> importFile, Boolean overwrite) throws InterruptedException, KeeperException {

        for (String line : importFile) {
            log.debug("Importing line " + line);
            // Delete Operation
            if (line.startsWith("-")) {
                String nodeToDelete = line.substring(1);
                deleteNodeIfExists(nodeToDelete);
            } else {
                int firstEq = line.indexOf('=');
                int secEq = line.indexOf('=', firstEq + 1);

                String path = line.substring(0, firstEq);
                if ("/".equals(path)) {
                    path = "";
                }
                String name = line.substring(firstEq + 1, secEq);
                String value = readExternalizedNodeValue(line.substring(secEq + 1));
                String fullNodePath = path + "/" + name;

                // Skip import of system node
                if (fullNodePath.startsWith(ZK_SYSTEM_NODE)) {
                    log.debug("Skipping System Node Import: " + fullNodePath);
                    continue;
                }
                boolean nodeExists = nodeExists(fullNodePath);

                if (!nodeExists) {
                    // If node doesnt exist then create it.
                    createPathAndNode(path, name, value.getBytes(), true);
                } else {
                    // If node exists then update only if overwrite flag is set.
                    if (overwrite) {
                        setPropertyValue(path, name, value);
                    } else {
                        log.debug("Skipping update for existing property " + path + "/" + name
                                + " as overwrite is not enabled!");
                    }
                }

            }

        }
    }

    private String readExternalizedNodeValue(String raw) {
        return raw.replaceAll("\\\\n", "\n");
    }

    private void createPathAndNode(String path, String name, byte[] data, boolean force)
            throws InterruptedException, KeeperException {
        log.debug("add entry, path={}, name={}, value={}", path, name, Arrays.toString(data));
        // 1. Create path nodes if necessary
        StringBuilder currPath = new StringBuilder();
        for (String folder : path.split("/")) {
            if (folder.length() == 0) {
                continue;
            }
            currPath.append('/');
            currPath.append(folder);

            if (!nodeExists(currPath.toString())) {
                createIfDoesntExist(currPath.toString(), new byte[0], true);
            }
        }

        // 2. Create leaf node
        createIfDoesntExist(path + '/' + name, data, force);
    }

    private void createIfDoesntExist(String path, byte[] data, boolean force)
            throws InterruptedException, KeeperException {
        try {
            zk.create(path, data, defaultAcl(), CreateMode.PERSISTENT);
        } catch (KeeperException ke) {
            // Explicit Overwrite
            if (KeeperException.Code.NODEEXISTS.equals(ke.code())) {
                if (force) {
                    zk.delete(path, -1);
                    zk.create(path, data, defaultAcl(), CreateMode.PERSISTENT);
                }
            } else {
                throw ke;
            }
        }
    }

    public ZkNode listNodeEntries(String path) throws KeeperException, InterruptedException {
        List<String> folders = new ArrayList<>();
        List<LeafBean> leaves = new ArrayList<>();

        List<String> children = zk.getChildren(path, false);
        if (children != null) {
            for (String child : children) {
                if (!child.equals(ZK_SYSTEM_NODE)) {

                    List<String> subChildren = zk.getChildren(path + ("/".equals(path) ? "" : "/") + child, false);
                    boolean isFolder = subChildren != null && !subChildren.isEmpty();
                    if (isFolder) {
                        folders.add(child);
                    } else {
                        String childPath = getNodePath(path, child);
                        leaves.add(this.getNodeValue(path, childPath, child));
                    }

                }

            }
        }

        Collections.sort(folders);
        Collections.sort(leaves, new Comparator<LeafBean>() {
            @Override
            public int compare(LeafBean o1, LeafBean o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        ZkNode zkNode = new ZkNode();
        zkNode.setLeafBeanLSt(leaves);
        zkNode.setNodeLst(folders);
        return zkNode;
    }

    @Deprecated
    public List<LeafBean> listLeaves(String path) throws InterruptedException, KeeperException {
        List<LeafBean> leaves = new ArrayList<>();

        List<String> children = zk.getChildren(path, false);
        if (children != null) {
            for (String child : children) {
                String childPath = getNodePath(path, child);
                List<String> subChildren = Collections.emptyList();
                subChildren = zk.getChildren(childPath, false);
                boolean isFolder = subChildren != null && !subChildren.isEmpty();
                if (!isFolder) {
                    leaves.add(this.getNodeValue(path, childPath, child));
                }
            }
        }

        Collections.sort(leaves, new Comparator<LeafBean>() {
            @Override
            public int compare(LeafBean o1, LeafBean o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return leaves;
    }

    @Deprecated
    public List<String> listFolders(String path) throws KeeperException, InterruptedException {
        List<String> folders = new ArrayList<>();
        List<String> children = zk.getChildren(path, false);
        if (children != null) {
            for (String child : children) {
                if (!child.equals(ZK_SYSTEM_NODE)) {
                    List<String> subChildren = zk.getChildren(path + ("/".equals(path) ? "" : "/") + child, false);
                    boolean isFolder = subChildren != null && !subChildren.isEmpty();
                    if (isFolder) {
                        folders.add(child);
                    }
                }

            }
        }

        Collections.sort(folders);
        return folders;
    }

    public String getNodePath(String path, String name) {
        return path + ("/".equals(path) ? "" : "/") + name;

    }

    public LeafBean getNodeValue(String path, String childPath, String child) {
        // Reason exception is caught here is so that lookup can continue to happen if a
        // particular property is not
        // found at parent level.
        try {
            log.trace("Lookup: path=" + path + ",childPath=" + childPath + ",child=" + child);
            byte[] dataBytes = zk.getData(childPath, false, new Stat());
            return (new LeafBean(path, child, dataBytes));
        } catch (KeeperException | InterruptedException ex) {
            log.error(ex.getMessage());
        }
        return null;

    }

    public void createNode(String path, String name, String value) throws KeeperException, InterruptedException {
        String nodePath = path + "/" + name;
        log.debug("Creating node " + nodePath + " with value " + value);
        zk.create(nodePath, value == null ? null : value.getBytes(), defaultAcl(), CreateMode.PERSISTENT);
    }

    public void createFolder(String folderPath, String propertyName, String propertyValue)
            throws KeeperException, InterruptedException {
        log.debug("Creating folder " + folderPath + " with property " + propertyName + " and value " + propertyValue);
        zk.create(folderPath, "".getBytes(), defaultAcl(), CreateMode.PERSISTENT);
        zk.create(folderPath + "/" + propertyName, propertyValue == null ? null : propertyValue.getBytes(),
                defaultAcl(), CreateMode.PERSISTENT);

    }

    public void setPropertyValue(String path, String name, String value) throws KeeperException, InterruptedException {
        String nodePath = path + "/" + name;
        log.debug("Setting property " + nodePath + " to " + value);
        zk.setData(nodePath, value.getBytes(), -1);

    }

    public boolean nodeExists(String nodeFullPath) throws KeeperException, InterruptedException {
        log.trace("Checking if exists: " + nodeFullPath);
        return zk.exists(nodeFullPath, false) != null;
    }

    public void deleteFolders(List<String> folderNames) throws KeeperException, InterruptedException {

        for (String folderPath : folderNames) {
            deleteFolderInternal(folderPath);
        }

    }

    private void deleteFolderInternal(String folderPath) throws KeeperException, InterruptedException {

        log.debug("Deleting folder " + folderPath);
        for (String child : zk.getChildren(folderPath, false)) {
            deleteFolderInternal(getNodePath(folderPath, child));
        }
        zk.delete(folderPath, -1);
    }

    public void deleteLeaves(List<String> leafNames) throws InterruptedException, KeeperException {
        for (String leafPath : leafNames) {
            log.debug("Deleting leaf " + leafPath);
            zk.delete(leafPath, -1);
        }
    }

    private void deleteNodeIfExists(String path) throws InterruptedException, KeeperException {
        zk.delete(path, -1);
        log.debug("delete entry:{}", path);
    }

    public void closeZooKeeper() {
        closeZooKeeper(zk);
    }

    public void closeZooKeeper(ZooKeeper zk) {
        log.trace("Closing ZooKeeper");
        if (zk != null) {
            try {
                zk.close();
                log.trace("Closed ZooKeeper");
            } catch (InterruptedException e) {
                log.error("Failed closing ZooKeeper", e);
            }
        }
    }
}