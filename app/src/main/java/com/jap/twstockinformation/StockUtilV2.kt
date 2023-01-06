package com.jap.twstockinformation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*
import kotlin.coroutines.coroutineContext


class StockUtilV2 {
    val information = HashMap<String, HashMap<String, String>>()

    private fun String.replaceWithSpace(list: List<String>): String {
        var result = this
        list.forEach { result = result.replace(it, "") }
        return result
    }

    suspend fun getPrice(): Boolean = withContext(coroutineContext) {
        val request = Request.Builder()
            .url("https://histock.tw/stock/rank.aspx?p=all")
            .method("GET", null)
            .build()
        try {
            val result = OkhttpWrapper.get(request)?.body?.string()
            val doc = Jsoup.parse(result)
            val e = doc.getElementsByTag("tr")
            e.removeAt(0) //代號 ▼ 名稱 ▼ 價格 ▼ 漲跌 ▼ 漲跌幅 ▼ 周漲跌 ▼ 振幅 ▼ 開盤 ▼ 最高 ▼ 最低 ▼ 昨收 ▼ 成交量 ▼ 成交值(億) ▼
            for (temp_e in e) {
                val inside_value = HashMap<String, String>()
                var temp_text = temp_e.text()
                if (temp_e.text().contains("元大MSCI A股")) {
                    temp_text = temp_text.replace("元大MSCI A股", "元大MSCI_A股")
                }
                val value_list = temp_text.split(" ".toRegex()).toTypedArray()

                if (information.containsKey(value_list[0])) {
                    information[value_list[0]]?.set("Name", value_list[1])
                    information[value_list[0]]?.set("Price", value_list[2])
                    information[value_list[0]]?.set("UpAndDown", value_list[3])
                    information[value_list[0]]?.set("UpAndDownPercent", value_list[4])
                    information[value_list[0]]?.set("WeekUpAndDownPercent", value_list[5])
                    information[value_list[0]]?.set("HighestAndLowestPercent", value_list[6])
                    information[value_list[0]]?.set("Open", value_list[7])
                    information[value_list[0]]?.set("High", value_list[8])
                    information[value_list[0]]?.set("Low", value_list[9])
                    information[value_list[0]]?.set("DealVolume", value_list[11])
                    information[value_list[0]]?.set("DealTotalValue", value_list[12])
                } else {
                    inside_value["Name"] = value_list[1]
                    inside_value["Price"] = value_list[2]
                    inside_value["UpAndDown"] = value_list[3]
                    inside_value["UpAndDownPercent"] = value_list[4]
                    inside_value["WeekUpAndDownPercent"] = value_list[5]
                    inside_value["HighestAndLowestPercent"] = value_list[6]
                    inside_value["Open"] = value_list[7]
                    inside_value["High"] = value_list[8]
                    inside_value["Low"] = value_list[9]
                    inside_value["DealVolume"] = value_list[11]
                    inside_value["DealTotalValue"] = value_list[12]
                    information[value_list[0]] = inside_value
                }
            }
        } catch (e: Exception) {
            false
        }
        true
    }


    suspend fun getFundamental(): Boolean = withContext(coroutineContext) {
        val request = Request.Builder()
            .url("https://www.twse.com.tw/exchangeReport/BWIBBU_d?response=json&date=&selectType=&_=${System.currentTimeMillis()}")
            .method("GET", null)
            .build()
        try {
            val result = OkhttpWrapper.get(request)?.body?.string() ?: return@withContext false
            val j = JSONObject(result)
            val jsonOb = j.getJSONArray("data")
            for (i in 0 until jsonOb.length()) {
                val inside_value = HashMap<String, String>()
                val temp_object = jsonOb[i]
                val temp_list = temp_object.toString().replaceWithSpace(listOf("\"", "[", "]")).split(",".toRegex()).toTypedArray()
                if (information.containsKey(temp_list[0])) {
                    information[temp_list[0]]?.set("DividendYield", temp_list[2])
                    information[temp_list[0]]?.set("PriceToEarningRatio", temp_list[4])
                    information[temp_list[0]]?.set("PriceBookRatio", temp_list[5])
                } else {
                    inside_value["DividendYield"] = temp_list[2]
                    inside_value["PriceToEarningRatio"] = temp_list[4]
                    inside_value["PriceBookRatio"] = temp_list[5]
                    information[temp_list[0]] = inside_value
                }
            }
        } catch (e: Exception) {
            false
            e.printStackTrace()
        }
        true
    }

    suspend fun getIncome() = withContext(coroutineContext) {
        val request = Request.Builder()
            .url("https://stock.wespai.com/income")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (K HTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36"
            )
            .addHeader("Accept-Language", Locale.US.language)
            .method("GET", null)
            .build()
        val result = OkhttpWrapper.get(request) ?: return@withContext
        val body = result.body ?: return@withContext
        try {
            val doc = Jsoup.parse(body.string())
            val e = doc.getElementsByTag("tr")
            e.removeAt(0) //title 拿掉 代號 公司 營業收入(千元) 營收月增率(%) 去年同期營收 營收年增率(%) 累計營收(千元) 去年同期累計營收(千元) 累計營收年增率(%)
//            println(e.toArray().size.toString())
            for (temp_e in e) {
                val insideValue = HashMap<String, String>()
                val tempList = temp_e.text().split(" ".toRegex()).toTypedArray()
                if (information.containsKey(tempList[0])) {
                    information[tempList[0]]!!["OperatingRevenue"] = tempList[2] + "000"
                    information[tempList[0]]!!["MoM"] = tempList[3]
                    information[tempList[0]]!!["YoY"] = tempList[5]
                } else {
                    insideValue["OperatingRevenue"] = tempList[2] + "000"
                    insideValue["MoM"] = tempList[3]
                    insideValue["YoY"] = tempList[5]
                    information[tempList[0]] = insideValue
                }
            }
        } catch (e: Exception) {

        }
    }


    suspend fun getInvestorsRatio(): Boolean = withContext(coroutineContext) {
        val request = Request.Builder()
            .url("https://stock.wespai.com/p/60546")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (K HTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36"
            )
            .method("GET", null)
            .build()
        val result = OkhttpWrapper.get(request)

        try {
            val doc = Jsoup.parse(result?.body?.string())
            val e = doc.getElementsByTag("tr")
            e.removeAt(0)
            val final_result = HashMap<String, HashMap<String, String>>()
            for (temp_e in e) {
                val inside_value = HashMap<String, String>()
                //            System.out.println(temp_e.text());
                val temp_list = temp_e.text().split(" ".toRegex()).toTypedArray()
                val threeBigRation = (temp_list[3].toFloat() + temp_list[4].toFloat() + temp_list[5].toFloat()).toString()
                inside_value["ThreeBigRation"] = threeBigRation
                final_result[temp_list[0]] = inside_value
                if (information.containsKey(temp_list[0])) {
                    information[temp_list[0]]?.set("DirectorsSupervisorsRatio", temp_list[2])
                    information[temp_list[0]]?.set("ForeignInvestmentRatio", temp_list[3])
                    information[temp_list[0]]?.set("InvestmentRation", temp_list[4])
                    information[temp_list[0]]?.set("SelfEmployedRation", temp_list[5])
                    information[temp_list[0]]?.set("ThreeBigRation", threeBigRation)
                } else {

                    inside_value["DirectorsSupervisorsRatio"] = temp_list[2]
                    inside_value["ForeignInvestmentRatio"] = temp_list[3]
                    inside_value["InvestmentRation"] = temp_list[4]
                    inside_value["SelfEmployedRation"] = temp_list[5]
                    information[temp_list[0]] = inside_value
                }
            }
        } catch (e: Exception) {
            false
        }
        true
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
     * SelfEmployedRation - 自營商持股
     * ThreeBigRation - 三大法人持股比例
     */
    suspend fun Get_HashMap_Num_MapTotalInformation(): HashMap<String, HashMap<String, String>> = withContext(coroutineContext) {
        val a = async { priceJob() }
        val b = async { fundamentalJob() }
        val c = async { incomeJob() }
        val d = async { institutionalInvestorsRatioJob() }
//        println("${a.await()} ${b.await()}${c.await()} ${d.await()}")//${a.await()}
        information
    }

    suspend fun priceJob(): Boolean = withContext(Dispatchers.IO) { getPrice() }
    suspend fun fundamentalJob(): Boolean = withContext(Dispatchers.IO) { getFundamental() }
    suspend fun incomeJob() = withContext(Dispatchers.IO) { getIncome() }
    suspend fun institutionalInvestorsRatioJob(): Boolean = withContext(Dispatchers.IO) { getInvestorsRatio() }

}