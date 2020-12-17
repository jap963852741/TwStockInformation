package com.jap.twstockinformation;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StockUtil {
    private Context context;
    private String CACERT_PATH;
    private HashMap<String,HashMap<String,String>> information = new HashMap<String,HashMap<String,String>>();
    public StockUtil(Context context){
        this.context=context;
        this.CACERT_PATH =  context.getFilesDir()  + File.separator + "cacert.pem";
        try {
            InputStream in = context.getAssets().open("cacert.pem");
            File outFile = new File(CACERT_PATH);
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024*5];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            out.flush();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        System.loadLibrary("app");
    }
    public native HashMap<String,String> getMapNumName(String CACERT_PATH);
    public native String getHtmlStringPrice(String CACERT_PATH);
    public native String getJsonStringFundamental(String CACERT_PATH);
    public native String getHtmlStringIncome(String CACERT_PATH);
    public native String getHtmlStringInstitutionalInvestorsRatio(String CACERT_PATH);

    public HashMap<String,String> Get_HashMap_Num_Name(){
        HashMap<String,String> result = getMapNumName(CACERT_PATH);
        HashMap<String,String> final_result = new HashMap<String, String>();
        for (String key : result.keySet()) {
            try {
                String a_key = URLEncoder. encode(key, "UTF-8").replace("%0D%0D","");
                final_result.put(a_key,result.get(key));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return final_result;
    }
    public HashMap<String,HashMap<String,String>> Get_HashMap_Num_MapPrice(){
        String html_string = getHtmlStringPrice(CACERT_PATH);
        Document doc = Jsoup.parse(html_string);
        Elements e = doc.getElementsByTag("tr");
        e.remove(0);//代號 ▼ 名稱 ▼ 價格 ▼ 漲跌 ▼ 漲跌幅 ▼ 周漲跌 ▼ 振幅 ▼ 開盤 ▼ 最高 ▼ 最低 ▼ 昨收 ▼ 成交量 ▼ 成交值(億) ▼
//        System.out.println(e.text());
        HashMap<String,HashMap<String,String>> final_result = new HashMap<String, HashMap<String,String>>();
        for (Element temp_e :e){
            HashMap<String,String> inside_value = new HashMap<String,String>();
//            System.out.println(temp_e.text());
            String temp_text = temp_e.text();
            if (temp_e.text().contains("元大MSCI A股")){
                temp_text = temp_text.replace("元大MSCI A股","元大MSCI_A股");
            }
            String[] value_list = temp_text.split(" ");
            inside_value.put("Name",value_list[1]);
            inside_value.put("Price",value_list[2]);
            inside_value.put("UpAndDown",value_list[3]);
            inside_value.put("UpAndDownPercent",value_list[4]);
            inside_value.put("WeekUpAndDownPercent",value_list[5]);
            inside_value.put("HighestAndLowestPercent",value_list[6]);
            inside_value.put("Open",value_list[7]);
            inside_value.put("High",value_list[8]);
            inside_value.put("Low",value_list[9]);
//            inside_value.put("YesterdayPrice",value_list[10]);
            inside_value.put("DealVolume",value_list[11]);
            inside_value.put("DealTotalValue",value_list[12]);
            final_result.put(value_list[0],inside_value);
        }
        information = final_result;
        return final_result;
    }
    public HashMap<String,HashMap<String,String>> Get_HashMap_Num_MapFundamental(){
        String Json_String = getJsonStringFundamental(CACERT_PATH);
        HashMap<String,HashMap<String,String>> final_result = new HashMap<String, HashMap<String,String>>();
        try {
            JSONObject j = new JSONObject(Json_String);
            JSONArray jsonOb = j.getJSONArray("data");
            for (int i =0;i<jsonOb.length();i++){
                HashMap<String,String> inside_value = new HashMap<String,String>();
                Object temp_object = jsonOb.get(i);
//                System.out.println(temp_object.toString());
                String[] temp_list = temp_object.toString().replace("\"","").replace("[","").replace("]","").split(",");
                inside_value.put("DividendYield",temp_list[2]);
                inside_value.put("PriceToEarningRatio",temp_list[4]);
                inside_value.put("PriceBookRatio",temp_list[5]);
                final_result.put(temp_list[0],inside_value);
                if(information.containsKey(temp_list[0])){
                    information.get(temp_list[0]).put("DividendYield",temp_list[2]);
                    information.get(temp_list[0]).put("PriceToEarningRatio",temp_list[4]);
                    information.get(temp_list[0]).put("PriceBookRatio",temp_list[5]);


                }else {
                    information.put(temp_list[0],inside_value);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return final_result;
    }
    public HashMap<String,HashMap<String,String>> Get_HashMap_Num_MapIncome(){
        String html_string = getHtmlStringIncome(CACERT_PATH);
        Document doc = Jsoup.parse(html_string);
        Elements e = doc.getElementsByTag("tr");
        e.remove(0);//title 拿掉 代號 公司 營業收入(千元) 營收月增率(%) 去年同期營收 營收年增率(%) 累計營收(千元) 去年同期累計營收(千元) 累計營收年增率(%)
        HashMap<String,HashMap<String,String>> final_result = new HashMap<String, HashMap<String,String>>();
        for (Element temp_e :e){
            HashMap<String,String> inside_value = new HashMap<String,String>();
            String[] temp_list = temp_e.text().split(" ");
            inside_value.put("OperatingRevenue",temp_list[2]+"000");
            inside_value.put("MoM",temp_list[3]);
            inside_value.put("YoY",temp_list[5]);
            final_result.put(temp_list[0],inside_value);
            if(information.containsKey(temp_list[0])){
                information.get(temp_list[0]).put("OperatingRevenue",temp_list[2]+"000");
                information.get(temp_list[0]).put("MoM",temp_list[3]);
                information.get(temp_list[0]).put("YoY",temp_list[5]);
            }else {
                information.put(temp_list[0],inside_value);
            }
        }
        return final_result;
    }
    public HashMap<String,HashMap<String,String>> Get_HashMap_Num_MapInstitutionalInvestorsRatio(){
        String html_string = getHtmlStringInstitutionalInvestorsRatio(CACERT_PATH);
        Document doc = Jsoup.parse(html_string);
        Elements e = doc.getElementsByTag("tr");
        e.remove(0);
        HashMap<String,HashMap<String,String>> final_result = new HashMap<String, HashMap<String,String>>();
        for (Element temp_e :e){
            HashMap<String,String> inside_value = new HashMap<String,String>();
//            System.out.println(temp_e.text());
            String[] temp_list = temp_e.text().split(" ");
            inside_value.put("DirectorsSupervisorsRatio",temp_list[2]);
            inside_value.put("ForeignInvestmentRatio",temp_list[3]);
            inside_value.put("InvestmentRation",temp_list[4]);
            String ThreeBigRation = String.valueOf(Float.parseFloat(temp_list[2])+Float.parseFloat(temp_list[3])+Float.parseFloat(temp_list[4]));
            inside_value.put("ThreeBigRation",ThreeBigRation);
            final_result.put(temp_list[0],inside_value);
            if(information.containsKey(temp_list[0])){
                information.get(temp_list[0]).put("DirectorsSupervisorsRatio",temp_list[2]);
                information.get(temp_list[0]).put("ForeignInvestmentRatio",temp_list[3]);
                information.get(temp_list[0]).put("InvestmentRation",temp_list[4]);
                information.get(temp_list[0]).put("ThreeBigRation",ThreeBigRation);
            }else {
                information.put(temp_list[0],inside_value);
            }
        }
        return final_result;
    }

    /**
     * key : 代號
     * value schema:
     *
     * Name - 公司名稱
     * Price - 股票現價
     * UpAndDown - 漲跌
     * UpAndDownPercent - 漲跌現價比
     * WeekUpAndDownPercent - 周漲跌現價比
     * HighestAndLowestPercent - 最高最低振福
     * Open - 開盤價
     * High - 最高價
     * Low - 最低價
     * DealVolume - 交易量
     * DealTotalValue - 交易總值(億)
     * DividendYield - 殖利率
     * PriceToEarningRatio - 本益比
     * PriceBookRatio - 股價淨值比
     * OperatingRevenue - 營業收入
     * MoM - 「月增率」指的是跟上個月比起來增加了多少
     * YoY - 「年增率」就是當月營收與去年同期相比的年增率
     * DirectorsSupervisorsRatio - 董監持股比例
     * ForeignInvestmentRatio - 外商持股比例
     * InvestmentRation - 投信持股比例
     * ThreeBigRation - 三大法人持股比例
     * */

    public HashMap<String,HashMap<String,String>> Get_HashMap_Num_MapTotalInformation(){
        Get_HashMap_Num_MapPrice();
        System.out.println("Get_HashMap_Num_MapPrice    "+information);
        Get_HashMap_Num_MapFundamental();
        System.out.println("Get_HashMap_Num_MapFundamental    "+information);

        Get_HashMap_Num_MapIncome();
        System.out.println("Get_HashMap_Num_MapIncome    "+information);

        Get_HashMap_Num_MapInstitutionalInvestorsRatio();
        System.out.println("Get_HashMap_Num_MapInstitutionalInvestorsRatio    "+information);

        System.out.println(information.get("2330").get("Name"));

        return information;
    }


}
