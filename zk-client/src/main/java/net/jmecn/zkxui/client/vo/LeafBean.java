package net.jmecn.zkxui.client.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class LeafBean implements Comparable<LeafBean> {

    private final static Logger logger = LoggerFactory.getLogger(LeafBean.class);
    private String path;
    private String name;
    private byte[] value;

    public LeafBean(String path, String name, byte[] value) {
        super();
        this.path = path;
        this.name = name;
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public String getStrValue() {
        try {
            if (value == null) {
                return "";
            }

            return new String(this.value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
        }
        return null;
    }

    @Override
    public int compareTo(LeafBean o) {
        return (this.path + this.name).compareTo((o.path + o.name));
    }

    @Override
    public String toString() {
        return "LeafBean [path=" + path + ", name=" + name + ", value=" + getStrValue() + "]";
    }
}
