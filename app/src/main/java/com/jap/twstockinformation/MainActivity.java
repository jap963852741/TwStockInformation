package com.jap.twstockinformation;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("app");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
        /**
         * download the cacert.pem from openssl website
         * copy the ssl cacert.pem for libcurl from assets
         * */
        String CACERT_PATH = getFilesDir() + File.separator + "cacert.pem";
        try {
            InputStream in = getApplicationContext().getAssets().open("cacert.pem");
            File outFile = new File(getFilesDir() + File.separator, "cacert.pem");
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
        Log.e("MainActivity",result.toString());
        Log.e("MainActivity","123"+result.get("6703"));
        for (String key : result.keySet()) {
            try {
                String a_key = URLEncoder. encode(key, "UTF-8").replace("%0D%0D","");
                Log.e("MainActivity",a_key+""+ a_key.equals("6703"));
                Log.e("MainActivity","key"+key);
                Log.e("MainActivity","v"+result.get(key));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
    public native HashMap<String,String> getresult(String CACERT_PATH);
}
