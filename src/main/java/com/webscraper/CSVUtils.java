package com.webscraper;

import com.opencsv.CSVWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CSVUtils {

    private static String[] rowTxt;
    private static CSVWriter csvWriter;
    private static List<String[]> data;

    public CSVUtils(){
        data = new LinkedList<>();
    }

    public static void tableToCSV(String table) {
        Document doc = Jsoup.parse(table, "UTF-8");
        for (Element rowElmt : doc.getElementsByTag("tr")) {

            if(!rowElmt.attr("class").equals("jqgfirstrow")){
                Elements cols = rowElmt.getElementsByTag("th");
                if (cols.size() == 0 )
                    cols = rowElmt.getElementsByTag("td");

                rowTxt = new String[cols.size()];
                for (int i = 0; i < rowTxt.length; i++)
                    rowTxt[i] = cols.get(i).attr("title");
                data.add(rowTxt);
            }
        }
    }


    public static void writeCSV() throws IOException {
        csvWriter = new CSVWriter(new FileWriter("data.csv"));
        csvWriter.writeAll(data);
        csvWriter.close();
    }


}
