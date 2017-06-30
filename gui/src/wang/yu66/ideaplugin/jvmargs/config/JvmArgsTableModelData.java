package wang.yu66.ideaplugin.jvmargs.config;

import com.alibaba.fastjson.JSONReader;

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


    public static final int TABBED_PANEL_WIDTH = 500;
    public static final int TABBED_PANEL_HEIGHT = 70;

    public static final int EACH_ROW_HEIGHT = 20;
    public static final int[] COMLUMN_MAX_WIDTH = {350, 350, 50};

    public static final String[] COMLUMN_HEADER = new String[]{"参数", "值", "选中"};
    public static final boolean[] COMLUMN_MODIFY = {false, true, true};

    public static Map<String, Object[][]> ALL_ARGS = new HashMap<>();
    public static Map<String, List<JvmArg>> allArgs = new HashMap<>();
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
            InputStream inputStream = JvmArgsTableModelData.class.getClassLoader().getResourceAsStream("jvm8_unix.json");
            InputStreamReader fileReader = new InputStreamReader(inputStream);
            JSONReader jsonReader = new JSONReader(fileReader);
            jsonReader.startArray();
            while(jsonReader.hasNext()) {
                JvmArg jvmArg  = jsonReader.readObject(JvmArg.class);
                jvmArg.value = "";

                List<JvmArg> args = allArgs.get(jvmArg.type);
                if (args == null) {
                    args = new ArrayList<>();
                    allArgs.put(jvmArg.type, args);
                }
                args.add(jvmArg);
            }
            jsonReader.endArray();
            jsonReader.close();
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
