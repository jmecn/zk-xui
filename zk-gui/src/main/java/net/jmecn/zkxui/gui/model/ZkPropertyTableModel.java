package net.jmecn.zkxui.gui.model;

import javax.swing.table.AbstractTableModel;

import net.jmecn.zkxui.client.vo.LeafBean;
import net.jmecn.zkxui.client.vo.ZkNode;

public class ZkPropertyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private String[] HEADER = {"NAME", "VALUE"};

	private ZkNode zkNode = null;

	public void setZkNode(ZkNode zkNode) {
		this.zkNode = zkNode;
		this.fireTableDataChanged();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return HEADER.length;
	}

	@Override
	public String getColumnName(int index) {
		return HEADER[index];
	}

	@Override
	public int getRowCount() {
		if (zkNode == null) {
			return 0;
		}

		return zkNode.getLeafBeanLSt().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (zkNode == null) {
			return null;
		}

		LeafBean bean = zkNode.getLeafBeanLSt().get(rowIndex);
		if (columnIndex == 0) {
			return bean.getName();
		} else {
			return bean.getStrValue();
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void setValueAt(Object aValue,
            int rowIndex,
            int columnIndex) {
		// Not support
	}

}
