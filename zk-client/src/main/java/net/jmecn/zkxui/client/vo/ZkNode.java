package net.jmecn.zkxui.client.vo;

import java.util.ArrayList;
import java.util.List;

public class ZkNode {

    List<String> nodeLst;
    List<LeafBean> leafBeanLSt;

    public ZkNode() {
        nodeLst = new ArrayList<>();
        leafBeanLSt = new ArrayList<>();
    }

    public List<String> getNodeLst() {
        return nodeLst;
    }

    public void setNodeLst(List<String> nodeLst) {
        this.nodeLst = nodeLst;
    }

    public List<LeafBean> getLeafBeanLSt() {
        return leafBeanLSt;
    }

    public void setLeafBeanLSt(List<LeafBean> leafBeanLSt) {
        this.leafBeanLSt = leafBeanLSt;
    }

}
