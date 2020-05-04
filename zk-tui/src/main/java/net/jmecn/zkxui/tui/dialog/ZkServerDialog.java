package net.jmecn.zkxui.tui.dialog;

import java.util.List;

import jcurses.event.ItemEvent;
import jcurses.event.ItemListener;
import jcurses.system.CharColor;
import jcurses.system.Toolkit;
import jcurses.util.Message;
import jcurses.widgets.Button;
import jcurses.widgets.Dialog;
import jcurses.widgets.GridLayoutManager;
import jcurses.widgets.JCList;
import jcurses.widgets.Panel;
import jcurses.widgets.WidgetsConstants;
import lombok.extern.slf4j.Slf4j;
import net.jmecn.zkxui.client.ZkServerList;
import net.jmecn.zkxui.client.ZkXuiApp;
import net.jmecn.zkxui.client.vo.Env;
import net.jmecn.zkxui.tui.utils.ButtonBuilder;

/**
 * @title ZkServerDialog
 * @author yanmaoyuan
 * @date 2020年5月3日
 * @version 1.0
 */
@Slf4j
public class ZkServerDialog extends Dialog implements WidgetsConstants {

    private JCList<Env> list;

    private boolean locked = false;// 避免重复connect

    public ZkServerDialog() {
        super(84, 20, true, "Zookeeper Server List");

        GridLayoutManager gridLayout = new GridLayoutManager(3, 8);

        getRootPanel().setLayoutManager(gridLayout);
        gridLayout.addWidget(getEnvList(), 0, 0, 3, 7, ALIGNMENT_TOP, ALIGNMENT_LEFT);
        gridLayout.addWidget(getToolBox(), 0, 7, 3, 1, ALIGNMENT_TOP, ALIGNMENT_LEFT);

        loadList();
    }

    private JCList<Env> getEnvList() {
        if (list == null) {
            list = new JCList<>(16, false);
            list.getSelectedItemColors().setColorAttribute(CharColor.BOLD);
            list.addListener(new ItemListener() {
                @Override
                public void stateChanged(ItemEvent event) {
                    if (ItemEvent.CALLED == event.getType()) {
                        connect();
                    }
                }
            });

            List<Env> envList = ZkServerList.INSTANCE.list();
            if (envList.size() > 0) {
                for (Env e : envList) {
                    list.add(e);
                }
            }
        }

        return list;
    }

    private void loadList() {
        list.clear();
        List<Env> envList = ZkServerList.INSTANCE.list();
        if (envList.size() > 0) {
            for (Env e : envList) {
                list.add(e);
            }
        }
    }

    private Panel getToolBox() {
        Panel panel = new Panel(-1, 1);
        
        Button add = ButtonBuilder.newButton("(A)dd", 'a', (e) -> {
            add();
        });

        Button edit = ButtonBuilder.newButton("(E)dit", 'e', (e) -> {
            edit();
        });

        Button delete = ButtonBuilder.newButton("(D)elete", 'd', (e) -> {
            del();
        });

        GridLayoutManager manager = new GridLayoutManager(3, 1);
        panel.setLayoutManager(manager);
        manager.addWidget(add, 0, 0, 1, 1, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_CENTER);
        manager.addWidget(edit, 1, 0, 1, 1, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_CENTER);
        manager.addWidget(delete, 2, 0, 1, 1, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_CENTER);

        return panel;
    }

    private void edit() {
        int idx = list.getTrackedItem();
        List<Env> envlist = ZkServerList.INSTANCE.list();
        if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
            // out of index
            return;
        }

        Env old = envlist.get(idx);
        ZkEnvDialog dialog = new ZkEnvDialog(old);
        dialog.show();

        Env result = dialog.getResult();
        if (result != null && dialog.isModified()) {
            old.setName(result.getName());
            old.setZkServers(result.getZkServers());
            ZkServerList.INSTANCE.save();
            repaint();
        }
    }

    private void connect() {
        if (locked) {
            return;
        }
        locked = true;
        int idx = list.getTrackedItem();
        List<Env> envlist = ZkServerList.INSTANCE.list();
        if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
            // out of index
            locked = false;
            return;
        }

        Env env = envlist.get(idx);
        ZkXuiApp app = new ZkXuiApp(env);

        // Connection failed.
        if (!app.connect()) {
            new Message("WARN", "Connection to [" + env.getZkServers() + "] failed.", "OK").show();
            locked = false;
            return;
        }

        // Browser the ZooKeeper
        log.info("app:{}", app.getEnv());
        ZkBrowserDialog browser = new ZkBrowserDialog(app, Toolkit.getScreenWidth() - 2, Toolkit.getScreenHeight() - 2);
        browser.show();

        if (app != null) {
            app.disconnect();
        }

        locked = false;
    }

    private void add() {
        List<Env> envlist = ZkServerList.INSTANCE.list();

        ZkEnvDialog dialog = new ZkEnvDialog();
        dialog.show();

        Env result = dialog.getResult();
        if (result != null && dialog.isModified()) {
            envlist.add(result);
            ZkServerList.INSTANCE.save();
            list.add(result);
            list.setTrackedItem(envlist.size() - 1);// track the last
            this.repaint();
            log.info("Add: {}", result);
        }

    }

    private void del() {
        List<Env> envlist = ZkServerList.INSTANCE.list();
        int idx = list.getTrackedItem();

        if (envlist.size() <= 0 || idx < 0 || idx >= envlist.size()) {
            // out of index
            return;
        }

        Env env = envlist.get(idx);
        ConfirmDialog confirm =
            new ConfirmDialog("DELETE", "Are you sure to delete the selected ZooKeeper environment?\nNAME: "
                + env.getName() + "\nVALUE: " + env.getZkServers(), "No", "Yes");

        confirm.show();
        if (confirm.getResult() == null || confirm.getResult()) {
            return;
        }

        // 移除
        envlist.remove(idx);
        ZkServerList.INSTANCE.save();

        // 刷新界面
        list.remove(idx);
        if (list.getItemsCount() > idx) {
            list.setTrackedItem(idx);
        } else if (idx > 0) {
            list.setTrackedItem(idx - 1);
        } else {
            list.setTrackedItem(0);
        }
        this.repaint();
        log.info("Delete: {}", env);
    }
}