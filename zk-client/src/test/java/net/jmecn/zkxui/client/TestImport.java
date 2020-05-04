/**
 * Huatu.com Inc.
 * Copyright (c) 2014-2020 All Rights Reserved.
 */
package net.jmecn.zkxui.client;

import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @title TestImport
 * @author yanmaoyuan
 * @date 2020年5月2日
 * @version 1.0
 */
@Slf4j
public class TestImport {

    private String zkServer = "172.30.70.4:2181,172.30.70.5:2181,172.30.70.6:2181";

    private String huatu_test = "172.30.9.98:2181,172.30.9.99:2181,172.30.9.100:2181";

    private InputStream getTestData() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("# first line\n");
        sb.append("-/config/skynet-config-dev/for\n");
        sb.append("/config/skynet-config-dev=foo=bar\n");
        sb.append("/config/skynet-config-dev=key1=value1\n");
        sb.append("/config/skynet-config-dev=key2=value2\n");
        sb.append("/config/skynet-config-dev=key3=value3\n");
        sb.append("/config/skynet-config-dev=key4=value4\n");
        sb.append("/config/skynet-config-dev/test-dir=foo=bar\n");
        sb.append("/config/skynet-config-dev/dir1=foo=bar\n");
        sb.append("/config/skynet-config-dev/dir1=key1=value1\n");
        sb.append("/config/skynet-config-dev/dir1=key2=value2\n");
        sb.append("/config/skynet-config-dev/dir1=key3=value3\n");
        sb.append("/config/skynet-config-dev/dir1/dir2=showError=true\n");
        sb.toString();
        
        ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes());
        return in;
        
    }

    @Test
    @Ignore
    public void testImport() {
        ZkClient client = new ZkClient(huatu_test);
        ZooKeeper zk = client.getZookeeper();
        if (zk == null) {
            System.out.print("Connection failed.");
            return;
        }

        InputStream inpStream = getTestData();

        Boolean overwrite = false;
        try {
            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader(inpStream));
            String inputLine;
            List<String> importFile = new ArrayList<>();
            Integer lineCnt = 0;
            while ((inputLine = br.readLine()) != null) {
                lineCnt++;
                // Empty or comment?
                if (inputLine.trim().equals("") || inputLine.trim().startsWith("#")) {
                    continue;
                }
                if (inputLine.startsWith("-")) {
                    //DO nothing.
                } else if (!inputLine.matches("/.+=.+=.*")) {
                    throw new IOException("Invalid format at line " + lineCnt + ": " + inputLine);
                }

                importFile.add(inputLine);
            }
            br.close();

            client.importData(importFile, overwrite);
            for (String line : importFile) {
                if (line.startsWith("-")) {
                    log.info("delete entry:{}", line);
                } else {
                    log.info("add entry:{}", line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
