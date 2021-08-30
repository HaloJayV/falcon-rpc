import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class jsonUtil {

    public static void main(String[] args) throws IOException {
        List<String> a = readTxtFileIntoStringArrList("./device.json");
        char b = '\n';
        String c = listToString3(a, b);
    }



    public static List<String> readTxtFileIntoStringArrList(String filePath) {
        List<String> list = new ArrayList<String>();
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    list.add(lineTxt);
                }
                bufferedReader.close();
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return list;
    }

    public static String listToString3(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public void select() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("13");

        System.out.println(readTxtFileIntoStringArrList("C:\\Users\\xft32\\Desktop\\13.txt"));
        List<String> a = readTxtFileIntoStringArrList("C:\\Users\\xft32\\Desktop\\13.txt");
        char b = '\n';
        String c = listToString3(a, b);

        JSONObject myJSONObject;
        try {
            myJSONObject = new JSONObject(c);
            JSONObject storeInfo = myJSONObject.getJSONObject("storeInfo");
            JSONArray activities = storeInfo.getJSONArray("activities");

            for (int j = 0; j < activities.length(); j++) {
                JSONObject q = activities.getJSONObject(j);

                String description = q.getString("description");
                String icon_name = q.getString("icon_name");

                System.out.println("description=" + description);
                System.out.println("icon_name=" + icon_name);
                String name = null;
                if (q.isNull("name")) {

                } else {
                    name = q.getString("name");
                    System.out.println("name=" + name);
                }
                XSSFRow row = sheet.createRow(j);
                row.createCell(0).setCellValue("description=" + description);
                row.createCell(1).setCellValue("icon_name=" + icon_name);
                row.createCell(2).setCellValue("name=" + name);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("异常");
        }

        FileOutputStream out = new FileOutputStream("C:\\Users\\xft32\\Desktop\\13.xlsx");
        workbook.write(out);
        out.flush();
        out.close();
        System.out.println("写入成功");
    }

}