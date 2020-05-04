package net.jmecn.zkxui.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.jmecn.zkxui.client.vo.Env;

/**
 * @title EnvironmentList
 * @author yanmaoyuan
 * @date 2020年5月2日
 * @version 1.0
 */
public enum ZkServerList {

    INSTANCE;

    final static String NAME = "zkServer.cfg";

    private File file = new File(NAME);

    private List<Env> list = new ArrayList<Env>();

    ZkServerList() {
        if (file.exists()) {
            load();
        }
    }

    private void load() {
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // skip empty lists
                if (line.trim().length() == 0) {
                    continue;
                }

                // skip comments
                if (line.startsWith("#")) {
                    continue;
                }

                // parse
                if (line.matches(".+=.+")) {
                    int idx = line.indexOf('=');
                    String name = line.substring(0, idx);
                    String value = line.substring(idx+1, line.length());
                    list.add(new Env(name ,value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Env> list() {
        return list;
    }

    public Env get(int index) {
        return list.get(index);
    }

    public void add(String name, String zkServer) {
        list.add(new Env(name, zkServer));
    }

    public void save() {
        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            for (Env e : list) {
                out.printf("%s=%s\n", e.getName(), e.getZkServers());
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}