package net.jmecn.zkxui.client.utils;

import java.util.StringTokenizer;

/**
 * @title ConfigTokenizer
 * @author yanmaoyuan
 * @date 2020年5月7日
 * @version 1.0
 */
public class ConfigTokenizer {

    private final static String STATEMENT = "^/.*=.*=.*";

    private final static String DEL_STATEMENT = "^-/.*";

    private final static String COMMENT = "^#.*";

    public void test(String content) {
        StringTokenizer tokenizer = new StringTokenizer(content, "\r\n", false);

        long length = content.length();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            int len = line.length();
            if (isBlank(line.trim())) {
                continue;
            } else if (isComment(line.trim())) {
                // color
            } else if (isStatement(line)) {
                getStatement(line);
            } else if (isDelStatement(line)) {
                // color
            } else {
                // skip
            }
        }
    }

    public boolean isBlank(String line) {
        return StringUtils.isBlank(line);
    }

    public boolean isComment(String line) {
        line = line.trim();
        return (line.matches(COMMENT));
    }

    public boolean isStatement(String line) {
        return (line.matches(STATEMENT));
    }

    public void getStatement(String line) {
        int start = 0;
        int first = line.indexOf('=');

        int firstEnd = start + first;
        int second = line.indexOf('=', first + 1);

        int secondStart = firstEnd + 1;
        int secondEnd = start + second;

        int lastStart = start + second + 1;
        int lastEnd = start + line.length();

        System.out.printf("first:%d,%d,%s, second=%d,%d,%s, last=%d,%d,%s\n",//
            start, firstEnd, line.substring(start, firstEnd),//
            secondStart, secondEnd, line.subSequence(secondStart, secondEnd),//
            lastStart, lastEnd, line.substring(lastStart));
    }

    public boolean isDelStatement(String line) {
        return (line.matches(DEL_STATEMENT));
    }

    public final static void main(String[] args) {
        ConfigTokenizer app = new ConfigTokenizer();
        app.test(testData());
    }

    public static String testData() {
        StringBuilder sb = new StringBuilder();
        sb.append("# this is a ").append("\r\n");
        sb.append(" ").append("\r\n");
        sb.append(" 123").append("\r\n");
        sb.append("/config=123=dbb").append("\r\n");
        sb.append("/config=bbb=ccc  ").append("\r\n");
        sb.append("#/config=bbb=ccc  ").append("\r\n");
        sb.append("nfig=bbb=ccc").append("\r\n");
        sb.append("-/danfig").append("\r\n");

        return sb.toString();
    }
}