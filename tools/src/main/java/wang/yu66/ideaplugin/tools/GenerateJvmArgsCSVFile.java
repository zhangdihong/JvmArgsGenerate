package wang.yu66.ideaplugin.tools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangming on 2017/6/29.
 */
public class GenerateJvmArgsCSVFile {

    private static final String FILE_NAME = "../gui/resources/jvm8_unix.csv";

    private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL;
    private static final List<String> writable = new ArrayList();

    private static final String[] types = {
            "Standard Options",
            "Non-Standard Options",
            "Advanced Runtime Options",
            "Advanced JIT Compiler Options",
            "Advanced Serviceability Options",
            "Advanced Garbage Collection Options",
            "",
            "",
    };

    private static final String[] SELECTORS = {
            "#JSSOR624 > div:nth-child(6) > div:nth-child(10) > dl",
            "#JSSOR624 > div:nth-child(6) > div:nth-child(11) > dl",
            "#JSSOR624 > div:nth-child(6) > div:nth-child(12) > dl",
            "#JSSOR624 > div:nth-child(6) > div:nth-child(13) > dl",
            "#JSSOR624 > div:nth-child(6) > div:nth-child(14) > dl",
            "#JSSOR624 > div:nth-child(6) > div:nth-child(15) > dl",
            "#JSSOR624 > div:nth-child(6) > div:nth-child(16) > dl",
            "#JSSOR624 > div:nth-child(6) > div:nth-child(17) > dl"};

    private static final String[] SYSTEMS = {"unix",    // unix, linux, macos  使用的都是这个页面下的参数
//            "windows",
    };

    private static FileWriter fileWriter = null;
    private static CSVPrinter csvPrinter = null;

    static {
        File file = new File(FILE_NAME);
        try {
            if (file.exists()) {
                file.delete();
            }
            file = new File(FILE_NAME);
            file.createNewFile();
            fileWriter = new FileWriter(FILE_NAME, true);  // 创建test.csv的字符输出流
            csvPrinter = new CSVPrinter(fileWriter, CSV_FORMAT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        parseFromJsoup();
    }

    private static void parseFromJsoup() {
        try {
            Document doc = getDocument();
            for (int start = 0; start < SELECTORS.length; start++) {
                String type = types[start];
                String selectorStr = SELECTORS[start];

                for (int i = 1; i < 300; ) {
                    String title = selectorStr + " > dt:nth-child(" + (i++) + ")";
                    String content = selectorStr + " > dd:nth-child(" + (i++) + ")";
                    Elements titleElements = doc.select(title);
                    Elements contentElements = doc.select(content);
                    if (titleElements.size() == 0) {
                        continue;
                    }

                    StringBuffer contentBuffer = new StringBuffer();
                    for (int j = 0; j < contentElements.size(); j++) {
                        Element c = contentElements.get(j);
                        Elements children = c.children();
                        for (Element element : children) {
                            contentBuffer.append(element.text()).append("\n");
                        }
                    }
                    write(type, titleElements.text(), contentElements.html());
//                    write(type, titleElements.text(), contentBuffer.toString());
//                    System.out.println("下载[" + selectorStr + "," + i + "] --> " + titleElements.text() + " ---- " + contentBuffer.toString());
                    System.out.println("下载[" + selectorStr + "," + i + "] ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                csvPrinter.flush();
                fileWriter.flush();
                fileWriter.close();
                csvPrinter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Document getDocument() throws IOException {
        return Jsoup.connect("http://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html").get();
    }

    public static void write(String type, String argument, String comment) {

        try {
            writable.clear();
            writable.add(type);
            writable.add(argument);
            writable.add(comment);
            csvPrinter.printRecord(writable);  // 向.csv文件中添加记录数据
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            try {
                fileWriter.close();
                csvPrinter.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
