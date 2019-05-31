package com.google.android.gms.samples.vision.barcodereader;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MyTable implements Serializable {

    private HashMap<String, MyTableData> tabData = new HashMap<>();

    public MyTable() {}

    public HashMap<String, MyTableData> getTableData() { return tabData;}

    public MyTableData addItem(String key, String value) {
        MyTableData item = tabData.get(key);
        if (item == null) return tabData.put(key, new MyTableData(value));
        else {
            item.setCount(item.getCount() + 1);
            return item;
        }
    }

    public void removeItem(String key) {
        tabData.remove(key);
    }

    public void clearItems() {
        tabData.clear();
    }
    public String genHtmlTable() {

        String head = "<!DOCTYPE html><html><head></head><body>";
        String tail = "</body></html>";

        String body = "<style type=\"text/css\">\n" +
                ".tg  {border-collapse:collapse;border-spacing:0;border-color:#93a1a1;}\n" +
                ".tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#93a1a1;color:#002b36;background-color:#fdf6e3;}\n" +
                ".tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#93a1a1;color:#fdf6e3;background-color:#657b83;}\n" +
                ".tg .tg-s6z2{text-align:center}\n" +
                ".tg .tg-xmgo{background-color:#eee8d5;text-align:center}\n" +
                ".tg .tg-b4k6{background-color:#eee8d5;text-align:left}\n" +
                ".tg .tg-s268{text-align:left}\n" +
                "</style>";
        body += "<table class=\"tg\">\n" +
                "  <tr>\n" +
                "    <th class=\"tg-s6z2\">Code</th>\n" +
                "    <th class=\"tg-s6z2\">Nom</th>\n" +
                "    <th class=\"tg-s6z2\">Quantié</th>\n" +
                "  </tr>";
        for (Map.Entry<String, MyTableData> entry: tabData.entrySet()) {
            MyTableData entryData = entry.getValue();
            String htmlRow = "  <tr>\n" +
                    "    <td class=\"tg-b4k6\">" + entry.getKey() + "</td>\n" +
                    "    <td class=\"tg-xmgo\">" + entryData.getValue() + "</td>\n" +
                    "    <td class=\"tg-xmgo\">" + entryData.getCount() + "</td>\n" +
                    "  </tr>";
            body += htmlRow;
        }
        body += "</table>";

        return head + body + tail;
    }

    class MyTableData implements Serializable{
        private String value;
        private int count;

        public MyTableData(String value) {
            this.value = value;
            this.count = 1;
        }

        public String getValue() {
            return value;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int newValue) {
            count = newValue;
        }
    }

}
