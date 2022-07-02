package com.jap.twstockinformation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockUtilTest {

    //    public native String getresult(String CACERT);
    //    public native ArrayList<String> getGerritChanges(String CACERT);
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val st = StockUtilV2()
        runBlocking {st.getPrice()}
        Thread.sleep(5000)
//        println(st.information.size)
//        st.information.forEach {
//            println(it.toString())
//        }
    }

    @Test
    fun cpp_test() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        //        StockUtil st = new StockUtil(appContext);
        val st = StockUtilV2()
        val aa = runBlocking {
            st.Get_HashMap_Num_MapTotalInformation()
        }
//        val aa = runBlocking { st.Get_HashMap_Num_MapFundamental() }

        println("aa size = ${aa.size}")
        for (key in aa.keys) {
            if (key.toString() != "2308") continue
            println(key + aa[key].toString())
        }
    }
}