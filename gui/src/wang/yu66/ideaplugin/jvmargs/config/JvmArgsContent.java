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
 * Created by wangming on 2017/6/22.
 */
public class JvmArgsContent {

    public static final String[] COMLUMN_HEADER = new String[]{"参数", "值", "注释", "选中"};

    public static Map<String, Object[][]> ALL_ARGS = new HashMap<>();

    static {

        Map<String, List<JvmArg>> allArgs = load();
        for (Map.Entry<String, List<JvmArg>> entry : allArgs.entrySet()) {
            String type = entry.getKey();
            List<JvmArg> list = entry.getValue();
            Object[][] args = ALL_ARGS.get(type);
            if (args == null) {
                args = new Object[list.size()][4];
                ALL_ARGS.put(type, args);
            }

            for (int i = 0; i < list.size(); i++) {
                JvmArg arg = list.get(i);
                args[i] = new Object[]{arg.arg, arg.value, arg.comment, new Boolean(false)};
            }
        }
    }

    private static Map<String, List<JvmArg>> load() {
        Map<String, List<JvmArg>> allArgs = new HashMap<>();
        try {
            Reader in = new FileReader("D:\\workspace\\JvmArgsGenerate\\resources\\jvm.csv");
            if (in == null) {
                in = new FileReader("./jvm.csv");
            }
            if (in == null) {
                in = new FileReader("jvm.csv");
            }
            if (in == null) {
                in = new FileReader("/jvm.csv");
            }
            Iterable<CSVRecord> records = new CSVParser(in, CSVFormat.EXCEL);
            for (CSVRecord record : records) {
                JvmArg jvmArg = new JvmArg();
                jvmArg.type = record.get(0);
                jvmArg.arg = record.get(1);
                jvmArg.value = record.get(2);
                jvmArg.comment = record.get(3);

                String[] array = jvmArg.arg.split("=");
                if (array.length > 1 && (jvmArg.value == null || jvmArg.value.equals(""))) {
                    jvmArg.arg = array[0];
                    jvmArg.value = array[1];
                }

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
        return allArgs;
    }

    public static class JvmArg {
        public String type;
        public String arg;
        public String value;
        public String comment;
    }


}
