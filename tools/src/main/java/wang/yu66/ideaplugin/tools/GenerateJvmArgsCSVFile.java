package wang.yu66.ideaplugin.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by wangming on 2017/6/29.
 */
public class GenerateJvmArgsCSVFile {

    private static final String FILE_NAME = "../gui/resources/jvm8_unix.json";

    private static final String[] SYSTEMS = {"unix",    // unix, linux, macos  使用的都是这个页面下的参数
//            "windows",
    };

    private static FileWriter fileWriter = null;

    static {
        File file = new File(FILE_NAME);
        try {
            if (file.exists()) {
                file.delete();
            }
            file = new File(FILE_NAME);
            file.createNewFile();
            fileWriter = new FileWriter(FILE_NAME, true);  // 创建test.csv的字符输出流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        parseById();
    }

    private static void parseById() {
        try {
            Document doc = getDocument();
            String[] ids = {"BABDJJFI",
                    "BABHDABI",
                    "BABCBGHF",
                    "BABDDFII",
                    "BABFJDIC",
                    "BABFAFAE"};

            JSONWriter writer = new JSONWriter(fileWriter);
            writer.startArray();

            for (String id : ids) {
                Element parent = doc.getElementById(id).parent();
                String argType = parent.child(1).text();

                Element args = parent.child(3);
                Elements children = args.children();
                for (int i = 0; i < children.size();) {
                    Element nameElement = children.get(i++);
                    Element descElement = children.get(i++);
                    String name = nameElement.text();
                    String desc = "";
                    if (descElement.tagName().equals("dt")) {   // name
                        // 当前没有描述, 可能是上一个的缩写
                        name = name + " / " + descElement.text();
                        desc = children.get(i++).html();
                    } else if (descElement.tagName().equals("dd")) {    // desc
                        desc = children.get(--i).html();
                        i++;
                    }
                    System.out.println(i + " --> " + name + " : " + desc);
                    JvmArg jvmArg = new JvmArg();
                    jvmArg.type = argType;
                    jvmArg.arg = name;
                    jvmArg.comment = desc;
                    writer.writeObject(jvmArg);
                }
            }
            writer.endArray();
            writer.close();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Document getDocument() throws IOException {
        return Jsoup.connect("http://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html").get();
    }

    public static class JvmArg {
        public String type;
        public String arg;
        public String value;
        public String comment;
    }
}
