package com.jap.twstockinformation;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class StockUtil {
    static {
        System.loadLibrary("app");
    }
    public native HashMap<String,String> getresult(String CACERT_PATH);
    private Context context;
    public StockUtil(Context context){
        this.context=context;
    }
    public void aa(){
        System.out.println("aa");

        String CACERT_PATH =  context.getFilesDir()  + File.separator + "cacert.pem";
//        System.out.println(System.getProperty("user.dir"));
//        File file = new File(System.getProperty("user.dir"));
//        listDir(file);
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
        HashMap<String,String> result = getresult(CACERT_PATH);
        for (String key : result.keySet()) {
            try {
                String a_key = URLEncoder. encode(key, "UTF-8").replace("%0D%0D","");
                System.out.println(a_key);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }




    public static void listDir(File file){
        String newPath = "D:\\newDir";
        if(file.isDirectory()){	 // 是一個目錄
            // 列出目錄中的全部內容
            File results[] = file.listFiles();
            if(results != null){
                for(int i=0;i<results.length;i++){
                    System.out.println(results[i]);	// 繼續一次判斷
                }
            }
        }else{	// 是檔案
            String fileStr = (file.getName()).toString();
            System.out.println(fileStr);	// 繼續一次判斷

        }
        //file.delete(); //刪除!!!!! 根目錄,慎操作
        //獲取完整路徑
        System.out.println(file);
    }
}
