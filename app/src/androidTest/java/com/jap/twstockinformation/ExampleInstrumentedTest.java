package com.jap.twstockinformation;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    static {
        System.loadLibrary("app");
    }
//    public native String getresult(String CACERT);
//    public native ArrayList<String> getGerritChanges(String CACERT);
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.jap.twstockinformation", appContext.getPackageName());
    }

    @Test
    public void cpp_test() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.jap.twstockinformation", appContext.getPackageName());
//        String CACERT_PATH = appContext.getFilesDir() + File.separator + "cacert.pem";
//        Log.e("CACERT_PATH",CACERT_PATH);
//        System.out.println(getGerritChanges(CACERT_PATH));
        StockUtil st = new StockUtil(appContext);
        st.aa();

    }

}