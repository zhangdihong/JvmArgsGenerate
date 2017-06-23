package wang.yu66.ideaplugin.jvmargs.config;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangming on 2017/6/22.
 */
public class JvmArgsContent {

    public static final String[] COMLUMN_HEADER = new String[]{"参数", "值", "注释", "选中"};

    public static Object[][] MEMORY;
    public static Object[][] GC;
    public static Object[][] GC_OUTPUT;

    static {
        List<JvmArg> memory = new ArrayList<>();
        List<JvmArg> gc = new ArrayList<>();
        List<JvmArg> gcOutput = new ArrayList<>();
        load(memory, gc, gcOutput);

        MEMORY = new Object[memory.size()][4];
        for (int i = 0; i < memory.size(); i++) {
            JvmArg arg = memory.get(i);
            MEMORY[i] = new Object[]{arg.arg, arg.value, arg.comment, new Boolean(false)};
        }
        GC = new Object[gc.size()][4];
        for (int i = 0; i < gc.size(); i++) {
            JvmArg arg = gc.get(i);
            GC[i] = new Object[]{arg.arg, arg.value, arg.comment, new Boolean(false)};
        }
        GC_OUTPUT = new Object[gcOutput.size()][4];
        for (int i = 0; i < gcOutput.size(); i++) {
            JvmArg arg = gcOutput.get(i);
            GC_OUTPUT[i] = new Object[]{arg.arg, arg.value, arg.comment, new Boolean(false)};
        }
    }

    private static void load(List<JvmArg> memory, List<JvmArg> gc, List<JvmArg> gcOutput) {
        try {
            Reader in = new FileReader("D:\\workspace\\idea-plugin\\jvmargs\\resources\\jvm.csv");
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
                switch (jvmArg.type) {
                    case "1": {
                        memory.add(jvmArg);
                        break;
                    }
                    case "2": {
                        gc.add(jvmArg);
                        break;
                    }
                    case "3": {
                        gcOutput.add(jvmArg);
                        break;
                    }
                }
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
