package wang.yu66.ideaplugin.jvmargs.config;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by wangming on 2017/6/22.
 */
public class JvmArgsTableModelData {

    private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL;

    public static final int TABBED_PANEL_WIDTH = 500;
    public static final int TABBED_PANEL_HEIGHT = 70;

    public static final int EACH_ROW_HEIGHT = 20;
    public static final int[] COMLUMN_MAX_WIDTH = {350, 350, 50};

    public static final String[] COMLUMN_HEADER = new String[]{"参数", "值", "选中"};
    public static final boolean[] COMLUMN_MODIFY = {false, true, true};

    public static Map<String, Object[][]> ALL_ARGS = new HashMap<>();
    public static Map<String, List<JvmArg>> allArgs = new HashMap<>();
    private static final String[] FILE_NAMES = {"D:\\workspace\\JvmArgsGenerate\\gui\\resources\\jvm8_unix.csv", "./jvm8_unix.csv", "jvm8_unix.csv", "/jvm8_unix.csv"};
    static {

        load();
        for (Map.Entry<String, List<JvmArg>> entry : allArgs.entrySet()) {
            String type = entry.getKey();
            List<JvmArg> list = entry.getValue();
            Object[][] args = ALL_ARGS.get(type);
            if (args == null) {
                args = new Object[list.size()][COMLUMN_HEADER.length];
                ALL_ARGS.put(type, args);
            }

            for (int i = 0; i < list.size(); i++) {
                JvmArg arg = list.get(i);
                args[i] = new Object[]{arg.arg, arg.value, new Boolean(false)};
            }
        }
    }

    private static void load() {

        try {
            File file = null;
            for (String fileName : FILE_NAMES) {
                file = new File(fileName);
                if (file.exists()) {
                    break;
                }
            }
            if (!file.exists()) {
                System.err.println("找不到jvm.csv文件");
            }
            FileReader in = new FileReader(file);
            Iterable<CSVRecord> records = new CSVParser(in, CSV_FORMAT);
            for (CSVRecord record : records) {
                JvmArg jvmArg = new JvmArg();
                jvmArg.type = record.get(0);
                jvmArg.arg = record.get(1);
                jvmArg.comment = record.get(2);
                jvmArg.value = "";

                List<JvmArg> args = allArgs.get(jvmArg.type);
                if (args == null) {
                    args = new ArrayList<>();
                    allArgs.put(jvmArg.type, args);
                }
                args.add(jvmArg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class JvmArg {
        public String type;
        public String arg;
        public String value;
        public String comment;
    }


}
