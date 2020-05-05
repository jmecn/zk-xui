package net.jmecn.zkxui.gui.model;

import javax.swing.AbstractListModel;

import net.jmecn.zkxui.client.vo.ZkNode;

public class ZkNodeListModel extends AbstractListModel<String> {

	private static final long serialVersionUID = 1L;

	private boolean isRoot = true;

	private ZkNode zkNode = null;

	public void setZkNode(boolean isRoot, ZkNode zkNode) {
		this.isRoot = isRoot;
		this.zkNode = zkNode;
		this.fireContentsChanged(this, 0, zkNode.getNodeLst().size());
	}

	@Override
	public String getElementAt(int index) {
		if (!isRoot && index == 0) {
			return "..";
		}

		int idx = isRoot ? index : index - 1;
		return zkNode.getNodeLst().get(idx);
	}

	@Override
	public int getSize() {
		if (zkNode == null) {
			return 0;
		}

		int size = 0;
		if (!isRoot) {
			size++;
		}

		size += zkNode.getNodeLst().size();
		return size;
	}

}
