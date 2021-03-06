package net.jmecn.zkxui.gui.dialog;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkServerList;
import net.jmecn.zkxui.client.ZkXuiApp;
import net.jmecn.zkxui.client.vo.Env;
import net.jmecn.zkxui.gui.task.ProgressDialog;
import net.jmecn.zkxui.gui.task.ProgressTask;

@Slf4j
public class ZkServerDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private ZkXuiApp result;

    private JList<Env> list;

    public ZkServerDialog(ZkBrowserDialog parent) {
        super(parent);
        this.setTitle("ZooKeeper Server List");
        this.setSize(600, 400);
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        CenterUtils.center(parent, this);

        getContentPane().add(getEnvList(), BorderLayout.CENTER);
        getContentPane().add(getToolBox(), BorderLayout.SOUTH);
    }

    private JScrollPane getEnvList() {
        list = new JList<>();
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(list);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    // Do I need to handle right click?
                } else if (SwingUtilities.isLeftMouseButton(evt)) {
                    if (evt.getClickCount() == 2) {
                        int index = list.locationToIndex(evt.getPoint());
                        ok(index);
                    }
                }
            }
        });

        list.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ok(list.getSelectedIndex());
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    add();
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    del();
                }
            }
        });

        List<Env> envList = ZkServerList.INSTANCE.list();
        list.setListData(envList.toArray(new Env[0]));

        list.setSelectedIndex(0);
        return scroll;
    }

    private JMenuBar getToolBox() {
        JMenuBar bar = new JMenuBar();

        JButton add = new JButton("Add");
        add.addActionListener((e) -> {
            add();
        });

        JButton connect = new JButton("Connect");
        connect.addActionListener((e) -> {
            ok(list.getSelectedIndex());
        });

        JButton edit = new JButton("Edit");
        edit.addActionListener((e) -> {
            edit();
        });

        JButton delete = new JButton("Delete");
        delete.addActionListener((e) -> {
            del();
        });

        bar.add(add);
        bar.add(connect);
        bar.add(edit);
        bar.add(delete);
        return bar;
    }

    private void loadList() {
        List<Env> envList = ZkServerList.INSTANCE.list();
        list.setListData(envList.toArray(new Env[0]));
    }

    private void edit() {
        int idx = list.getSelectedIndex();
        List<Env> envlist = ZkServerList.INSTANCE.list();
        if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
            // out of index
            return;
        }

        Env old = envlist.get(idx);
        ZkEnvDialog dialog = new ZkEnvDialog(old);
        dialog.setVisible(true);

        Env result = dialog.getResult();
        if (result != null && dialog.isModified()) {
            old.setName(result.getName());
            old.setZkServers(result.getZkServers());
            ZkServerList.INSTANCE.save();
            repaint();
        }
    }

    private void ok(int idx) {
        List<Env> envlist = ZkServerList.INSTANCE.list();
        if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
            // out of index
            return;
        }

        Env env = envlist.get(idx);

        // Connection
        ProgressTask<ZkXuiApp> task = new ProgressTask<ZkXuiApp>() {
            @Override
            public ZkXuiApp call() throws Exception {
                ZkXuiApp app = new ZkXuiApp(env);
                if (!app.connect()) {
                    app = null;
                }
                return app;
            }
        };

        // Display a progress
        ProgressDialog<ZkXuiApp> dialog = new ProgressDialog<>("Connect", task);
        dialog.setVisible(true);

        // Connection result
        ZkXuiApp app = dialog.getResult();
        if (app == null) {
            JOptionPane.showMessageDialog(this, "Connection to [" + env.getZkServers() + "] failed.", "WARN",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.result = app;
        dispose();
    }

    private void add() {
        List<Env> envlist = ZkServerList.INSTANCE.list();

        ZkEnvDialog dialog = new ZkEnvDialog();
        dialog.setVisible(true);

        Env result = dialog.getResult();
        if (result != null && dialog.isModified()) {
            envlist.add(result);
            ZkServerList.INSTANCE.save();
            log.info("Add: {}", result);

            loadList();
            list.setSelectedIndex(envlist.size() - 1);// track the last

            repaint();
        }

    }

    private void del() {
        List<Env> envlist = ZkServerList.INSTANCE.list();
        int idx = list.getSelectedIndex();

        if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
            // out of index
            return;
        }

        Env env = envlist.get(idx);

        int result = JOptionPane.showConfirmDialog(this, //
            "Are you sure to delete the selected ZooKeeper environment?\nNAME: "//
                + env.getName() + "\nVALUE: " + env.getZkServers(), //
            "DELETE", //
            JOptionPane.YES_NO_OPTION, //
            JOptionPane.QUESTION_MESSAGE);//

        if (JOptionPane.YES_OPTION != result) {
            return;
        }

        // 移除
        envlist.remove(idx);
        ZkServerList.INSTANCE.save();
        log.info("Delete: {}", env);

        loadList();

        // 刷新界面
        if (envlist.size() > idx) {
            list.setSelectedIndex(idx);
        } else if (idx > 0) {
            list.setSelectedIndex(idx - 1);
        } else {
            list.setSelectedIndex(0);
        }

        repaint();
    }

    public ZkXuiApp getResult() {
        return result;
    }
}