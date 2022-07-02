package com.jap.twstockinformation;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import kotlinx.coroutines.GlobalScope;

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
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        StockUtil st = new StockUtil(appContext);
//        StockUtilV2 st = new StockUtilV2(appContext);
//
//        HashMap<String, HashMap<String, String>> aa = st.Get_HashMap_Num_MapTotalInformation();
//        for (String key : aa.keySet()) {
//            System.out.println(key + aa.get(key).toString());
//        }
    }

}