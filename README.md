# TwStockInformation

資料來源:

* [台灣證券交易所](https://www.twse.com.tw/)
* [HiStock 嗨!投資](https://histock.tw/)
* [撿股讚](https://stock.wespai.com/)
</br>

## Install
 Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.jap963852741:TwStockInformation:v1.0.10'
	}
  
  ## Usage

     StockUtil st = new StockUtil(appContext);
     HashMap<String,HashMap<String,String>> example = st.Get_HashMap_Num_MapInstitutionalInvestorsRatio;
 
 ## Example
    example.get("2330").get("Name")
    >> return "台積電"
    
  ## Table Schema
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
