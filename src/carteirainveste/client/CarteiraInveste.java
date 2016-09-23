package carteirainveste.client;

// XML
// http://code.google.com/webtoolkit/doc/latest/DevGuideCodingBasicsXML.html

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
//import com.google.gwt.user.client.ui.SuggestionEvent;
//import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
//import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
//import com.google.gwt.i18n.shared.GwtLocale;
//import com.google.gwt.i18n.shared.GwtLocaleFactory;
//import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Date;
import java.util.Iterator;

class DateUtil {
	static NumberFormat yearFormat  = NumberFormat.getFormat("0000");
	static NumberFormat monthFormat = NumberFormat.getFormat("00");

	static final long MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

	static Date addDays(Date date, long days) {
		 return new Date(date.getTime() + days * MILLISECONDS_IN_DAY);
	}

	static long daysBetween(Date d1, Date d2) {
		long m1 = d1.getTime();
		long m2 = d2.getTime();
		return (m2 - m1) / MILLISECONDS_IN_DAY;
	}

	static int year(Date d) {
		String yy = DateTimeFormat.getFormat("y").format(d);
		int year = 0;
		try { year = Integer.parseInt(yy); } catch (Exception e) { }
		return year;
	}

	static int linearMonth(Date d) {
		int year = year(d);

		String mm = DateTimeFormat.getFormat("MM").format(d);
		int month = 0;
		try { month = Integer.parseInt(mm); } catch (Exception e) { }

		return year * 12 + month - 1;
	}

	static int monthToYear(int linearMonth) {
		return linearMonth / 12;
	}

	static int circularMonth(int linearMonth) {
		return linearMonth % 12 + 1;
	}

	static String monthStr(int linearMonth) {
		int year = monthToYear(linearMonth);
		int month = circularMonth(linearMonth);
		return yearFormat.format(year) + "-" + monthFormat.format(month);
	}
}

class Preference {
	static DateTimeFormat dateFormat             = DateTimeFormat.getFormat("yyyy-MM-dd");
	static NumberFormat   amountFormat           = NumberFormat.getFormat("0.000000");
	static NumberFormat   quoteFormat            = NumberFormat.getFormat("0.000000");
	static NumberFormat   unitaryQuoteFormat     = NumberFormat.getFormat("0.000000");
	static double         zeroPositionThreshold  = .0001;
	
	static double         sellSpread             = .02;     // 2%
	static long           brokerFeeCents         = 2000;    // R$ 20.00
	static long           stockTaxExemptionCents = 2000000; // R$ 20,000.00
	static double         stockTaxFee            = .15;     // 15%
	static double         stockDayTradeTaxFee    = .2;      // 20%

	static CheckBox stockDayTradeAffectExemptionLimit = new CheckBox("Day-trade consome limite de isencao");
	static CheckBox stockExemptGainsReduceCarriedLoss = new CheckBox("Ganho isento reduz prejuizo acumulado");
	static CheckBox stockTaxRatioOverPretaxEarnings   = new CheckBox("Aliquota de IR incide sobre Lucro Antes do Imposto na Fonte (LAIF)");

	static TextBox startTaxCarryLoss = new TextBox();
	static TextBox startTaxDayTradeCarryLoss = new TextBox();

	static void stockTaxResetDefaultsOptions() {
		stockDayTradeAffectExemptionLimit.setValue(true);
		stockExemptGainsReduceCarriedLoss.setValue(false);
		stockTaxRatioOverPretaxEarnings.setValue(true);
	}

	static {
		stockTaxResetDefaultsOptions();
		startTaxCarryLoss.setText(CurrencyUtil.format(0));
		startTaxDayTradeCarryLoss.setText(CurrencyUtil.format(0));
	}

	static boolean amountIsZero(double amount) {
		return amount <= zeroPositionThreshold;
	}
	static boolean amountIsNonZero(double amount) {
		return amount > zeroPositionThreshold;
	}
	
	static Element appendChildNode(Document doc, Node parent) {
		Element elem = doc.createElement(Constant.XML_TAG_PREFERENCES);
		parent.appendChild(elem);

		elem.setAttribute("stock_sell_spread",         String.valueOf(sellSpread));
		elem.setAttribute("stock_broker_fee",          String.valueOf(brokerFeeCents));
		elem.setAttribute("stock_tax_exemption_limit", String.valueOf(stockTaxExemptionCents));
		elem.setAttribute("stock_tax_fee",             String.valueOf(stockTaxFee));
		elem.setAttribute("stock_daytrade_tax_fee",    String.valueOf(stockDayTradeTaxFee));
		elem.setAttribute("stock_daytrade_affect_exemption_limit", String.valueOf(stockDayTradeAffectExemptionLimit.getValue()));
		elem.setAttribute("stock_exempt_gains_reduce_carried_loss", String.valueOf(stockExemptGainsReduceCarriedLoss.getValue()));
		elem.setAttribute("stock_tax_ratio_over_pretax_earnings", String.valueOf(stockTaxRatioOverPretaxEarnings.getValue()));

		/*
		Carteira curr = CarteiraInveste.carteiraAtual;
		String strCarryLoss = curr.startTaxCarryLoss.getText();
		if (strCarryLoss == null)
			strCarryLoss = CurrencyUtil.format(0);
		String strDayTradeCarryLoss = curr.startTaxDayTradeCarryLoss.getText();
		if (strDayTradeCarryLoss == null)
			strDayTradeCarryLoss = CurrencyUtil.format(0);
		*/

		elem.setAttribute("stock_tax_start_carry_loss", startTaxCarryLoss.getText());
		elem.setAttribute("stock_tax_daytrade_start_carry_loss", startTaxDayTradeCarryLoss.getText());

		return elem;
	}
	
	static void loadFromDb(Element elem) throws IllegalArgumentException {
		String strSellSpread             = elem.getAttribute("stock_sell_spread");
		String strBrokerFeeCents         = elem.getAttribute("stock_broker_fee");
		String strStockTaxExemptionCents = elem.getAttribute("stock_tax_exemption_limit");
		String strStockTaxFee            = elem.getAttribute("stock_tax_fee");
		String strStockDayTradeTaxFee    = elem.getAttribute("stock_daytrade_tax_fee");
		String strStockDayTradeAffectExemptionLimit = elem.getAttribute("stock_daytrade_affect_exemption_limit");
		String strStockExemptGainsReduceCarriedLoss = elem.getAttribute("stock_exempt_gains_reduce_carried_loss");
		String strStockTaxRatioOverPretaxEarnings   = elem.getAttribute("stock_tax_ratio_over_pretax_earnings");

		String strStockStartTaxCarryLoss = elem.getAttribute("stock_tax_start_carry_loss");
		String strStockStartTaxDayTradeCarryLoss = elem.getAttribute("stock_tax_daytrade_start_carry_loss");

		if (strSellSpread != null)			sellSpread 			= Double.parseDouble(strSellSpread);
		if (strBrokerFeeCents != null)		brokerFeeCents		= Long.parseLong(strBrokerFeeCents);
		if (strStockTaxExemptionCents != null)	stockTaxExemptionCents	= Long.parseLong(strStockTaxExemptionCents);
		if (strStockTaxFee != null)			stockTaxFee			= Double.parseDouble(strStockTaxFee);
		if (strStockDayTradeTaxFee != null)		stockDayTradeTaxFee	= Double.parseDouble(strStockDayTradeTaxFee);
		if (strStockDayTradeAffectExemptionLimit != null) stockDayTradeAffectExemptionLimit.setValue(Boolean.parseBoolean(strStockDayTradeAffectExemptionLimit));
		if (strStockExemptGainsReduceCarriedLoss != null) stockExemptGainsReduceCarriedLoss.setValue(Boolean.parseBoolean(strStockExemptGainsReduceCarriedLoss));
		if (strStockTaxRatioOverPretaxEarnings != null)   stockTaxRatioOverPretaxEarnings.setValue(Boolean.parseBoolean(strStockTaxRatioOverPretaxEarnings));

		if (strStockStartTaxCarryLoss != null) startTaxCarryLoss.setText(strStockStartTaxCarryLoss);
		if (strStockStartTaxDayTradeCarryLoss != null) startTaxDayTradeCarryLoss.setText(strStockStartTaxDayTradeCarryLoss);
	}
}

class Percent {
	static NumberFormat percent2 = NumberFormat.getFormat("0.00");
	static NumberFormat percent4 = NumberFormat.getFormat("0.0000");
	static NumberFormat percent6 = NumberFormat.getFormat("0.000000");

	static String format2(double value, boolean suffix) {
		if (suffix)
			return percent2.format(100 * value) + "%";

		return percent2.format(100 * value);
	}

	static String format2(double value) {
		return format2(value, true);
	}

	static String format4(double value) {
		return percent4.format(100 * value) + "%";
	}

	static String format6(double value) {
		return percent6.format(100 * value) + "%";
	}
}

class Accent {
	// http://programandosemcafeina.blogspot.com/2007/04/caracteres-especiais-representados-em.html
	static final String AACUTE_L = "\u00e1";
	static final String EACUTE_L = "\u00e9";
	static final String IACUTE_L = "\u00ed";
	static final String OACUTE_L = "\u00f3";
	static final String CCEDIL_L = "\u00e7";
	static final String ATILDE_L = "\u00e3";
	static final String OTILDE_L = "\u00f5";
	static final String ECIRC_L  = "\u00ea";
	static final String CCEDIL_OTILDE_LL = CCEDIL_L + OTILDE_L;
	static final String CCEDIL_ATILDE_LL = CCEDIL_L + ATILDE_L;
}

class Constant {
	static final String AVERAGE_O    = "M" + Accent.EACUTE_L + "dio";
	static final String BOND         = "T" + Accent.IACUTE_L + "tulo";
	static final String OPERATION    = "Opera" + Accent.CCEDIL_ATILDE_LL + "o";
	static final String OPERATIONS   = "Neg" + Accent.OACUTE_L + "cios";
	static final String EVOLUTION    = "Evolu" + Accent.CCEDIL_ATILDE_LL + "o";
	static final String SETTINGS     = "Op" + Accent.CCEDIL_OTILDE_LL + "es";
	static final String PREFERENCES  = "Prefer" + Accent.ECIRC_L + "ncias";
	static final String MONTH        = "M" + Accent.ECIRC_L + "s";
	static final String DEPOSIT      = "Dep" + Accent.OACUTE_L + "sito";
	static final String DEPOSITS     = DEPOSIT + "s";
	static final String NET_A        = "L" + Accent.IACUTE_L + "quida";
	static final String NET_O        = "L" + Accent.IACUTE_L + "quido";
	static final String STOCK        = "A" + Accent.CCEDIL_ATILDE_LL + "o";
	static final String STOCKS       = "A" + Accent.CCEDIL_OTILDE_LL + "es";
	static final String BONUS        = "Bonifica" + Accent.CCEDIL_ATILDE_LL + "o";
	static final String BONUS_IN_STOCK = BONUS + " em " + STOCKS;
	static final String POSITIONS    = "Posi" + Accent.CCEDIL_OTILDE_LL + "es";
	static final String QUOTE        = "Cota" + Accent.CCEDIL_ATILDE_LL + "o";
	static final String QUOTES       = "Cota" + Accent.CCEDIL_OTILDE_LL + "es";
	static final String REALIZATIONS = "Realiza" + Accent.CCEDIL_OTILDE_LL + "es";
	static final String REALIZABLE   = "Realiz" + Accent.AACUTE_L + "vel";
	static final String RATIO        = "Propor" + Accent.CCEDIL_ATILDE_LL + "o";
	static final String TRANSFER     = "Transfer" + Accent.ECIRC_L + "ncia";
	static final String VERSION      = "Vers" + Accent.ATILDE_L + "o";
	static final String V_NUMBER     = "0.16";

	static final String LICENSE = "<a rel=\"license\" href=\"http://creativecommons.org/licenses/by/3.0/deed.pt_BR\"><img alt=\"Licen" + Accent.CCEDIL_L + "a Creative Commons\" style=\"border-width:0\" src=\"http://i.creativecommons.org/l/by/3.0/88x31.png\" /></a><br /><span xmlns:dct=\"http://purl.org/dc/terms/\" property=\"dct:title\">Carteira Investe</span> de <span xmlns:cc=\"http://creativecommons.org/ns#\" property=\"cc:attributionName\">Everton da Silva Marques</span> " + Accent.EACUTE_L + " licenciado sob uma <a rel=\"license\" href=\"http://creativecommons.org/licenses/by/3.0/deed.pt_BR\">Licen" + Accent.CCEDIL_L + "a Creative Commons Atribui" + Accent.CCEDIL_ATILDE_LL + "o 3.0 Unported</a>.";

	static final String XML_HEADER          = "<?xml version=\"1.0\" ?>";
	static final String XML_TAG_OPERATION   = "operation";
	static final String XML_TAG_QUOTE       = "quote";
	static final String XML_TAG_PREFERENCES = "preferences";
}

class Quote {
	Date   quoteDate;
	String quoteAssetName;
	double quoteAssetAmount;
	double quoteValue;
	String key;

	static String makeKey(String assetName, String date) {
		return assetName + " " + date;
	}

	static String makeKey(String assetName, Date date) {
		return makeKey(assetName, Preference.dateFormat.format(date));
	}

	Quote(Date date, String asset, double amount, double value) {
		quoteDate        = date;
		quoteAssetName   = asset;
		quoteAssetAmount = amount;
		quoteValue       = value;
		key              = makeKey(quoteAssetName, quoteDate);
	}

	Quote(Element elem) throws IllegalArgumentException {
		quoteDate        = Preference.dateFormat.parse(elem.getAttribute("date"));
		quoteAssetName   = elem.getAttribute("asset_name");
		quoteAssetAmount = Double.parseDouble(elem.getAttribute("asset_amount"));
		quoteValue       = Double.parseDouble(elem.getAttribute("value"));
		key              = makeKey(quoteAssetName, quoteDate);
	}

	Date getDate() {
		return quoteDate;
	}

	String getKey() {
		return key;
	}

	double getUnitValue() {
		return quoteValue / quoteAssetAmount;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = doc.createElement(Constant.XML_TAG_QUOTE);
		parent.appendChild(elem);

		elem.setAttribute("date",         Preference.dateFormat.format(quoteDate));
		elem.setAttribute("asset_name",   quoteAssetName);
		elem.setAttribute("asset_amount", String.valueOf(quoteAssetAmount));
		elem.setAttribute("value",        String.valueOf(quoteValue));

		return elem;
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(Preference.dateFormat.format(quoteDate));
		case 1: return new Label(quoteAssetName);
		case 2: return new Label(Preference.amountFormat.format(quoteAssetAmount));
		case 3: return new Label(Preference.quoteFormat.format(quoteValue));
		case 4: return new Label(Preference.unitaryQuoteFormat.format(getUnitValue()));
		default: return new Label("Quote.getCol error col=" + col);
		}
	}
}

abstract class Asset {
	static final int ASSET_ACAO   = 0;
	static final int ASSET_CLUBE  = 1;
	static final int ASSET_TITULO = 2;
	static final int ASSET_ETF    = 3;
	static final String[] assetTypeName = {
		Constant.STOCK,
		"Clube",
		Constant.BOND,
		"ETF"
	};
	static final String[] assetTypeLabel = {
		"stock",
		"club",
		"bond",
		"etf"
	};

	static int findAssetType(String label) {
		for (int i = 0; i < assetTypeLabel.length; ++i)
			if (label.equals(assetTypeLabel[i]))
				return i;
		return -1;
	}

	static Asset newAsset(int assetType, String assetName) {
		switch (assetType) {
		case ASSET_ACAO:   return new Stock(assetName);
		case ASSET_CLUBE:  return new Club(assetName);
		case ASSET_TITULO: return new Bond(assetName);
		case ASSET_ETF:    return new ETF(assetName);
		default:
			// FIXME enum?
		}
		return null;
	}

	String assetName;
	double amountBought           = 0;
	double amountSold             = 0;
	double amountBonus            = 0;
	long   bonusTaxCostCents      = 0;
	long   purchaseGrossCostCents = 0;
	long   purchaseExpenseCents   = 0;   // Remessa
	long   salesNetRevenueCents   = 0;
	long   yieldAfterTaxCents     = 0;
	long   dayTradeNetResultCents = 0;

	Asset(String name) {
		assetName = name;
		resetHistory();
	}

	private void resetHistory() {
		amountBought           = 0;
		amountSold             = 0;
		amountBonus            = 0;
		bonusTaxCostCents      = 0;
		purchaseGrossCostCents = 0;
		purchaseExpenseCents   = 0;
		salesNetRevenueCents   = 0;
		yieldAfterTaxCents     = 0;
		dayTradeNetResultCents = 0;
	}

	void add(Buy b) {

		// Reset history if current amount is zero.
		// Possibly due to a previous sale.
		// We could check that any sale has been performed,
		// but it is not strictly needed, since
		// resetting an empty history is harmless (idempotent).
		if (Preference.amountIsZero(getCurrentAmount()))
			resetHistory();

		amountBought           += b.buyAmount;
		purchaseGrossCostCents += b.buyCostCents;
		purchaseExpenseCents   += b.buyExpenseCents;
	}

	void add(Sell s) {
		amountSold           += s.sellAmount;
		salesNetRevenueCents += s.sellNetRevenueCents;
	}

	void add(Yield y) {
		if (y.isBonus()) {
			amountBonus       += y.getAssetAmount();
			bonusTaxCostCents += y.yieldGrossCents;

			return;
		}

		yieldAfterTaxCents += y.yieldNetCents;
	}

	void add(DayTrade dt) {
		dayTradeNetResultCents += dt.getNetResultCents();
	}

	void add(Split s) {
		amountBought *= s.splitTo / s.splitFrom;
		amountBonus  *= s.splitTo / s.splitFrom;
		amountSold   *= s.splitTo / s.splitFrom;
	}

	long getBrokerFeeCents() {
		return 0;
	}

	double getAcquiredAmount() {
		return amountBought + amountBonus;
	}

	double getCurrentAmount() {
		return getAcquiredAmount() - amountSold;
	}

	// For tax calculations
	double getTaxAverageAcquisitionCostCents() {
		return (((double) purchaseGrossCostCents) + ((double) bonusTaxCostCents)) / getAcquiredAmount();
	}

	// For calculating real transaction performance
	double getRealAverageAcquisitionCost() {
		return (((double) purchaseGrossCostCents) + ((double) purchaseExpenseCents)) / getAcquiredAmount();
	}

	long getMarketValueCents(Quote assetQuote) {
		double marketValue = assetQuote.getUnitValue() * getCurrentAmount();
		return Math.round((100 * (1.0 - Preference.sellSpread) * marketValue));
	}

	// Child classes override this method.
	// For instance, see Stock class.
	double getTaxRate(long salesGrossValueCents) {
		return 0.0; // 0% by default
	}

	long quoteProfitBeforeTaxCents(Quote assetQuote) {
		return getMarketValueCents(assetQuote) - getBrokerFeeCents() - purchaseGrossCostCents;
	}

	static long getTaxValueCents(long profitBeforeTaxCents, double taxRate) {
		return (profitBeforeTaxCents <= 0) ? 0 : Math.round(taxRate * (double) profitBeforeTaxCents);
	}

	long quoteTaxValueCents(Quote assetQuote) {
		long salesGrossValueCents = getMarketValueCents(assetQuote);
		double taxRate = getTaxRate(salesGrossValueCents);

		long profitBeforeTaxCents = quoteProfitBeforeTaxCents(assetQuote);
		return getTaxValueCents(profitBeforeTaxCents, taxRate);
	}

	long getRealizableCents(Quote assetQuote) {

		if (Preference.amountIsZero(getCurrentAmount())) {
			return 0;
		}

		// mkt_sale (-) broker_fee (-) tax
		return getMarketValueCents(assetQuote) - getBrokerFeeCents() - quoteTaxValueCents(assetQuote);
	}

	long getResultCents(Quote assetQuote) {
		return	getRealizableCents(assetQuote) /* realizable */
			+ salesNetRevenueCents         /* plus realized */
			+ yieldAfterTaxCents           /* plus yields */
			- purchaseGrossCostCents       /* minus costs */
			- purchaseExpenseCents         /* minus expenses */
                  + dayTradeNetResultCents;      /* plus day trades */
	}
}

class Stock extends Asset {
	Stock(String name) {
		super(name);
	}

	long getBrokerFeeCents() {
		return Preference.brokerFeeCents;
	}

	static boolean stockSaleIsTaxExempt(long salesGrossValueCents) {
		return salesGrossValueCents < Preference.stockTaxExemptionCents;
	}

	static double stockTaxRate(long salesGrossValueCents) {
		return stockSaleIsTaxExempt(salesGrossValueCents) ? 0.0 : Preference.stockTaxFee;
	}

	double getTaxRate(long salesGrossValueCents) {
		return stockTaxRate(salesGrossValueCents);
	}
}
class Club extends Asset {
	Club(String name) {
		super(name);
	}

	double getTaxRate(long profitBeforeTaxCents) {
		return 0.15; // 15%
	}
}
class Bond extends Asset {
	Bond(String name) {
		super(name);
	}
}
class ETF extends Asset {
	ETF(String name) {
		super(name);
	}

	long getBrokerFeeCents() {
		return Preference.brokerFeeCents;
	}
}

class Account {
	String acctName;
	long   depositsCents;
	long   yieldsCents;
	long   buysCents;
	long   salesCents;
	long   withdrawalsCents;
	long   brokerTaxesCents;
      long   dayTradeCents;
	long   transferFromCents;
	long	 transferToCents;

	Account(String name) {
		acctName          = name;
		depositsCents     = 0; // depositos
		withdrawalsCents  = 0; // saques
		buysCents         = 0; // compras
		salesCents        = 0; // vendas
		yieldsCents       = 0; // proventos
		brokerTaxesCents  = 0; // corretora (custodia, etc)
		dayTradeCents     = 0; // daytrading
		transferFromCents = 0;
		transferToCents   = 0;
	}

	void add(Yield y) {
		if (y.isBonus()) {
			// Bonus does not affect account balance
			return;
		}

		yieldsCents += y.yieldNetCents;
	}

	void add(Buy b) {
		buysCents += b.buyCostCents;
	}

	void add(Sell s) {
		salesCents += s.sellNetRevenueCents;
	}

	void add(Deposit d) {
		depositsCents += d.depValueCents;
	}

	void add(Withdraw w) {
		withdrawalsCents += w.wdrawValueCents;
	}

	void add(Fee f) {
		brokerTaxesCents += f.feeValueCents;
	}

	void add(DayTrade dt) {
		dayTradeCents += dt.getNetResultCents();
	}

	void addFrom(Transfer t) {
		transferFromCents += t.transferValueCents;
	}

	void addTo(Transfer t) {
		transferToCents += t.transferValueCents;
	}

	long getBalanceCents() {
		return   depositsCents    + salesCents + yieldsCents
		       - withdrawalsCents - buysCents  - brokerTaxesCents
                   + dayTradeCents + transferToCents - transferFromCents;
	}
}

/*
// Format
Locale locale = Locale.GERMANY;
String string = NumberFormat.getCurrencyInstance(locale).format(123.45);
// 123,45 DM

locale = Locale.CANADA;
string = NumberFormat.getCurrencyInstance(locale).format(123.45);
// $123.45

// Parse
try {
    Number number = NumberFormat.getCurrencyInstance(locale).parse("$123.45");
    // 123.45
    if (number instanceof Long) {
        // Long value
    } else {
        // Double value
    }
} catch (ParseException e) {
}
*/

class CurrencyUtil {
	static NumberFormat fmt = NumberFormat.getFormat("0.00");

	//static NumberFormat localeFmt = NumberFormat.getCurrencyFormat();
	//static Locale locale = Locale.BRAZIL;
	//static NumberFormat localeFmt = NumberFormat.getCurrencyInstance(locale);
	//static NumberFormat fmt_BR = NumberFormat.getFormat("0.00");
	//static GwtLocaleFactory factory = new GwtLocaleFactoryImpl();
	//static GwtLocale locale_pt_BR = factory.fromString("pt_BR");
	//static NumberFormat fmt_pt_BR = NumberFormat.getFormat("#.##0,00");

	static String localeFormat(long cents) {
		return fmt.format(((float) cents) / 100).replace('.',',');
	}

	static String format(long cents) {
		return fmt.format(((float) cents) / 100);
	}

	static String formatDouble(double value) {
		return fmt.format(value);
	}

	static long roundCents(double value) {
		return  Math.round(100 * value);
	}

	static long parseToCents(String input) {
		double value = 0;

		try {
			value = fmt.parse(input);
			//value = Double.parseDouble(input);
		}
		catch (NumberFormatException e) {
		}

		return roundCents(value);
	}

	// CAUTION: will truncate decimals to cents 0.1234 --> 0.12
	static double refreshCents(TextBox input) {
		double value = 0;

		try {
			value = fmt.parse(input.getText());
			//value = Double.parseDouble(input.getText());
		}
		catch (NumberFormatException e) {
		}

		input.setText(fmt.format(value));

		return value;
	}

	// CAUTION: will truncate decimals to cents 0.1234 --> 0.12
	static long refreshCentsLong(TextBox input) {
		double value = 0;

		try {
			value = fmt.parse(input.getText());
			//value = Double.parseDouble(input.getText());
		}
		catch (NumberFormatException e) {
		}

		input.setText(fmt.format(value));

		return roundCents(value);
	}

	static double refresh(TextBox input) {
		double value = 0;

		try {
			value = Double.parseDouble(input.getText());
		}
		catch (NumberFormatException e) {
		}

		input.setText(Preference.unitaryQuoteFormat.format(value));

		return value;
	}
}

abstract class Operation {
	static final int TRADE_BUY      = 0;
	static final int TRADE_SELL     = 1;
	static final int TRADE_DAYTRADE = 2;
	static final String[] operationName = {
		"Compra",
		"Venda",
		"Day-trade"
	};
	static final int ACC_DEPOSIT  = 0;
	static final int ACC_WITHDRAW = 1;
	static final int ACC_FEE      = 2;
	static final int ACC_TRANSFER = 3;
	static final String[] accountAction = {
		Constant.DEPOSIT,
		"Saque",
		"Taxa",
		Constant.TRANSFER
	};

	static int nextId = 0;

	int id;

	private void getNextId() {
		id = nextId++;
	}

	Operation () {
		getNextId();
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = doc.createElement(Constant.XML_TAG_OPERATION);
		parent.appendChild(elem);
		return elem;
        }

	abstract Widget getCol(int col);
	abstract Date   getDate();
	abstract String getAccount();
}

interface HasAsset {
	String getAsset();
}

// Buy and Sell
interface Trade extends HasAsset {
} 

class Yield extends Operation implements HasAsset {
	static final int YIELD_JSCP     = 0;
	static final int YIELD_DIVIDEND = 1;
	static final int YIELD_BONUS    = 2;
	static final int YIELD_RENTAL   = 3;
	static final int YIELD_RENTAL2  = 4;
	static final int YIELD_COUPON   = 5;

	static final String[] yieldTypeName = {
		Constant.STOCK + " - JSCP",
		Constant.STOCK + " - Dividendos",
		Constant.STOCK + " - " + Constant.BONUS_IN_STOCK,
		Constant.STOCK + " - Aluguel",
		Constant.STOCK + " - Aluguel-Repasse-BTC",
		Constant.BOND  + " - Cupom"
	};

	static final String[] yieldTypeLabel = {
		"jscp",
		"dividend",
		"bonus",
            "rental",
            "rental2",
		"coupon"
	};

	static int findYieldType(String label) {
		for (int i = 0; i < yieldTypeLabel.length; ++i)
			if (label.equals(yieldTypeLabel[i]))
				return i;
		return -1;
	}

	int    yieldType;
	Date   yieldDate;
	//int    yieldTaxYear;
	String yieldAsset;
	double yieldAssetAmount;
	long   yieldGrossCents;
	long   yieldNetCents;
	String yieldAccount;

	Yield(int type, Date date, /*int taxYear,*/ String asset, double assetAmount, long grossValueCents, long netValueCents, String account) {
		super();
		yieldType        = type;
		yieldDate        = date;
		//yieldTaxYear     = taxYear;
		yieldAsset       = asset;
		yieldAssetAmount = assetAmount;
		yieldGrossCents  = grossValueCents;
		yieldNetCents    = netValueCents;
		yieldAccount     = account;
	}

	Yield(Element elem) throws IllegalArgumentException {
		super();
		yieldType        = findYieldType(elem.getAttribute("yield_type"));
		yieldDate        = Preference.dateFormat.parse(elem.getAttribute("date"));
		//yieldTaxYear     = Integer.parseInt(elem.getAttribute("tax_year"));
		yieldAsset       = elem.getAttribute("asset_name");
		yieldAssetAmount = Double.parseDouble(elem.getAttribute("asset_amount"));
		yieldGrossCents  = Long.parseLong(elem.getAttribute("gross_value"));
		yieldNetCents    = Long.parseLong(elem.getAttribute("net_value"));
		yieldAccount     = elem.getAttribute("account");
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(yieldTypeName[yieldType]);
		case 2: return new Label(Preference.dateFormat.format(yieldDate));
		//case 3: return new Label(String.valueOf(yieldTaxYear));
		case 3: return new Label(yieldAsset);
		case 4: return new Label(String.valueOf(yieldAssetAmount));
		case 5: return new Label(CurrencyUtil.format(yieldGrossCents));
		case 6: return new Label(CurrencyUtil.format(yieldNetCents));
		case 7: return new Label(yieldAccount == null ? "Nenhuma" : yieldAccount);
		default: return new Label("Yield.getCol error col=" + col);
		}
	}

	boolean isBonus() {
		return yieldType == YIELD_BONUS;
	}

	double getAssetAmount() {
		return yieldAssetAmount;
	}

	Date getDate() {
		return yieldDate;
	}

	String getAccount() {
		return yieldAccount;
	}

	public String getAsset() {
		return yieldAsset;
	}

	int getAssetType() {
		switch (yieldType) {
			case YIELD_JSCP:
			case YIELD_DIVIDEND:
			case YIELD_BONUS:
			case YIELD_RENTAL:
			case YIELD_RENTAL2:
				return Asset.ASSET_ACAO;
			case YIELD_COUPON:
				return Asset.ASSET_TITULO;
		}
		return -1;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",         "yield");
		elem.setAttribute("yield_type",   yieldTypeLabel[yieldType]);
		elem.setAttribute("date",         Preference.dateFormat.format(yieldDate));
		//elem.setAttribute("tax_year",     String.valueOf(yieldTaxYear));
		elem.setAttribute("asset_name",   yieldAsset);
		elem.setAttribute("asset_amount", String.valueOf(yieldAssetAmount));
		elem.setAttribute("gross_value",  String.valueOf(yieldGrossCents));
		elem.setAttribute("net_value",    String.valueOf(yieldNetCents));
		elem.setAttribute("account",      yieldAccount);

		return elem;
	}
}

class Split extends Operation implements HasAsset {

	Date   splitDate;
	String splitAsset;
	double splitFrom;
	double splitTo;

	Split(Date date, String asset, double from, double to) {
		splitDate  = date;
		splitAsset = asset;
		splitFrom  = from;
		splitTo    = to;
	}

	Split(Element elem) throws IllegalArgumentException {
		super();

		splitDate  = Preference.dateFormat.parse(elem.getAttribute("date"));
		splitAsset = elem.getAttribute("asset_name");
		splitFrom  = Double.parseDouble(elem.getAttribute("from"));
		splitTo    = Double.parseDouble(elem.getAttribute("to"));
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",       "split");
		elem.setAttribute("date",       Preference.dateFormat.format(splitDate));
		elem.setAttribute("asset_name", splitAsset);
		elem.setAttribute("from", 	  String.valueOf(splitFrom));
		elem.setAttribute("to",         String.valueOf(splitTo));

		return elem;
	}

	Date getDate() {
		return splitDate;
	}

	String getAccount() {
		return null;
	}

	public String getAsset() {
		return splitAsset;
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(Preference.dateFormat.format(getDate()));
		case 1: return new Label(getAsset());
		case 2: return new Label(String.valueOf(splitFrom));
		case 3: return new Label(String.valueOf(splitTo));
		default: return new Label("Split.getCol error col=" + col);
		}
	}

	int getAssetType() {
		return Asset.ASSET_ACAO;
	}
}

class Buy extends Operation implements Trade {
	Date   buyDate;
	int    buyAssetType;
	String buyAssetName;
	double buyAmount;
	long   buyCostCents;
	long   buyExpenseCents;
	String buyAccount;

	Buy(Date date, int assetType, String name, double amount, long cost, long expense, String account) {
		super();
		buyDate         = date;
		buyAssetType    = assetType;
		buyAssetName    = name;
		buyAmount       = amount;
		buyCostCents    = cost;
		buyExpenseCents = expense;
		buyAccount      = account;
	}

	Buy(Element elem) throws IllegalArgumentException {
		super();
		//buyDate         = DateFormat.getDateInstance().parse(elem.getAttribute("date"));
		//buyDate         = buyDate.parse(elem.getAttribute("date"));
		buyDate         = Preference.dateFormat.parse(elem.getAttribute("date"));
		buyAssetType    = Asset.findAssetType(elem.getAttribute("asset_type"));
		buyAssetName    = elem.getAttribute("asset_name");
		buyAmount	    = Double.parseDouble(elem.getAttribute("amount"));
		buyCostCents    = Long.parseLong(elem.getAttribute("cost"));
		buyExpenseCents = Long.parseLong(elem.getAttribute("expense"));
		buyAccount      = elem.getAttribute("account");
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(Preference.dateFormat.format(buyDate));
		case 2: return new Label(operationName[Operation.TRADE_BUY]);
		case 3: return new Label(Asset.assetTypeName[buyAssetType]);
		case 4: return new Label(buyAssetName);
		case 5: return new Label(String.valueOf(buyAmount));
		case 6: return new Label("--");
		case 7: return new Label(CurrencyUtil.format(buyCostCents));
		case 8: return new Label(CurrencyUtil.format(buyExpenseCents));
		case 9: return new Label("--");
		case 10: return new Label(buyAccount == null ? "Nenhuma" : buyAccount);
		case 11: return new Label(CurrencyUtil.format((long) ((double) (buyExpenseCents + buyCostCents) / buyAmount)));
		default: return new Label("Buy.getCol error col=" + col);
		}
	}

	Date getDate() {
		return buyDate;
	}

	String getAccount() {
		return buyAccount;
	}

	public String getAsset() {
		return buyAssetName;
	}

	int getAssetType() {
		return buyAssetType;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",       "buy");
		elem.setAttribute("date",       Preference.dateFormat.format(buyDate));
		elem.setAttribute("asset_type", Asset.assetTypeLabel[buyAssetType]);
		elem.setAttribute("asset_name", buyAssetName);
		elem.setAttribute("amount",     String.valueOf(buyAmount));
		elem.setAttribute("cost",       String.valueOf(buyCostCents));
		elem.setAttribute("expense",    String.valueOf(buyExpenseCents));
		if (buyAccount != null)
			elem.setAttribute("account",    buyAccount);

		return elem;
	}
}

class Sell extends Operation implements Trade {
	Date   sellDate;
	int    sellAssetType;
	String sellAssetName;
	double sellAmount;
	long   sellGrossValueCents;
	long   sellNetRevenueCents;
	long   sellRetainedTaxCents;
	String sellAccount;

	Sell(Date date, int assetType, String name, double amount, long grossValue, long netRevenue, long retainedTax, String account) {
		super();
		sellDate             = date;
		sellAssetType        = assetType;
		sellAssetName        = name;
		sellAmount           = amount;
		sellGrossValueCents  = grossValue;
		sellNetRevenueCents  = netRevenue;
		sellRetainedTaxCents = retainedTax;
		sellAccount          = account;
	}

	Sell(Element elem) throws IllegalArgumentException {
		super();
		sellDate            = Preference.dateFormat.parse(elem.getAttribute("date"));
		sellAssetType       = Asset.findAssetType(elem.getAttribute("asset_type"));
		sellAssetName       = elem.getAttribute("asset_name");
		sellAmount          = Double.parseDouble(elem.getAttribute("amount"));

		try {	sellGrossValueCents = Long.parseLong(elem.getAttribute("gross"));	}
		catch (NumberFormatException e) { sellGrossValueCents = 0; }

		try { sellNetRevenueCents = Long.parseLong(elem.getAttribute("revenue")); }
		catch (NumberFormatException e) { sellNetRevenueCents = 0;	}

		try {	sellRetainedTaxCents = Long.parseLong(elem.getAttribute("retained_tax")); }
		catch (NumberFormatException e) { sellRetainedTaxCents = 0;	}

		sellAccount         = elem.getAttribute("account");
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(Preference.dateFormat.format(sellDate));
		case 2: return new Label(operationName[Operation.TRADE_SELL]);
		case 3: return new Label(Asset.assetTypeName[sellAssetType]);
		case 4: return new Label(sellAssetName);
		case 5: return new Label(String.valueOf(sellAmount));
		case 6: return new Label(CurrencyUtil.format(sellGrossValueCents));
		case 7: return new Label(CurrencyUtil.format(sellNetRevenueCents));
		case 8: return new Label("--");
		case 9: return new Label(CurrencyUtil.format(sellRetainedTaxCents));
		case 10: return new Label(sellAccount == null ? "Nenhuma" : sellAccount);
		case 11: return new Label(CurrencyUtil.format((long) ((double) sellNetRevenueCents / sellAmount)));
		default: return new Label("Sell.getCol error col=" + col);
		}
	}

	Date getDate() {
		return sellDate;
	}

	String getAccount() {
		return sellAccount;
	}

	public String getAsset() {
		return sellAssetName;
	}

	int getAssetType() {
		return sellAssetType;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",         "sell");
		elem.setAttribute("date",         Preference.dateFormat.format(sellDate));
		elem.setAttribute("asset_type",   Asset.assetTypeLabel[sellAssetType]);
		elem.setAttribute("asset_name",   sellAssetName);
		elem.setAttribute("amount",       String.valueOf(sellAmount));
		elem.setAttribute("gross",        String.valueOf(sellGrossValueCents)); 
		elem.setAttribute("revenue",      String.valueOf(sellNetRevenueCents));
		elem.setAttribute("retained_tax", String.valueOf(sellRetainedTaxCents));
		if (sellAccount != null)
			elem.setAttribute("account", sellAccount);

		return elem;
	}
}

class DayTrade extends Operation implements Trade {
	Date   dtDate;
	String dtAssetName;
	double dtAmount;
	long   dtBuyGrossValueCents;
	long   dtSellGrossValueCents;
	long   dtSellNetValueCents;
	long   dtRetainedTaxCents;
	String dtAccount;

	DayTrade(Date date, String name, double amount, long buyGross, long sellGross, long sellNet, long retainedTax, String account) {
		super();
		dtDate                = date;
		dtAssetName           = name;
		dtAmount              = amount;
		dtBuyGrossValueCents  = buyGross;
		dtSellGrossValueCents = sellGross;
		dtSellNetValueCents   = sellNet;
		dtRetainedTaxCents    = retainedTax;
		dtAccount             = account;
	}

	DayTrade(Element elem) throws IllegalArgumentException {
		super();
		dtDate            = Preference.dateFormat.parse(elem.getAttribute("date"));
		dtAssetName       = elem.getAttribute("asset_name");
		dtAmount          = Double.parseDouble(elem.getAttribute("amount"));
		dtAccount         = elem.getAttribute("account");

		try {	dtBuyGrossValueCents = Long.parseLong(elem.getAttribute("buy_gross")); }
		catch (NumberFormatException e) { dtBuyGrossValueCents = 0;	}

		try {	dtSellGrossValueCents = Long.parseLong(elem.getAttribute("sell_gross")); }
		catch (NumberFormatException e) { dtSellGrossValueCents = 0;	}

		try {	dtSellNetValueCents = Long.parseLong(elem.getAttribute("sell_net")); }
		catch (NumberFormatException e) { dtSellNetValueCents = 0;	}

		try { dtRetainedTaxCents = Long.parseLong(elem.getAttribute("retained_tax"));	}
		catch (NumberFormatException e) { dtRetainedTaxCents = 0; }
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(Preference.dateFormat.format(dtDate));
		case 2: return new Label(operationName[Operation.TRADE_DAYTRADE]);
		case 3: return new Label(Asset.assetTypeName[getAssetType()]);
		case 4: return new Label(dtAssetName);
		case 5: return new Label(String.valueOf(dtAmount));
		case 6: return new Label(CurrencyUtil.format(dtBuyGrossValueCents));
		case 7: return new Label(CurrencyUtil.format(dtSellNetValueCents));
		case 8: return new Label("--");
		case 9: return new Label(CurrencyUtil.format(dtRetainedTaxCents));
		case 10: return new Label(dtAccount == null ? "Nenhuma" : dtAccount);
		case 11: return new Label(CurrencyUtil.format(dtSellNetValueCents - dtBuyGrossValueCents));
		default: return new Label("Sell.getCol error col=" + col);
		}
	}

	Date getDate() {
		return dtDate;
	}

	String getAccount() {
		return dtAccount;
	}

	public String getAsset() {
		return dtAssetName;
	}

	int getAssetType() {
		return Asset.ASSET_ACAO;
	}

	long getNetResultCents() {
		return dtSellNetValueCents - dtBuyGrossValueCents;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",         "daytrade");
		elem.setAttribute("date",         Preference.dateFormat.format(dtDate));
		elem.setAttribute("asset_name",   getAsset());
		elem.setAttribute("amount",       String.valueOf(dtAmount));
		elem.setAttribute("buy_gross",    String.valueOf(dtBuyGrossValueCents)); 
		elem.setAttribute("sell_gross",   String.valueOf(dtSellGrossValueCents));
		elem.setAttribute("sell_net",     String.valueOf(dtSellNetValueCents));
		elem.setAttribute("retained_tax", String.valueOf(dtRetainedTaxCents));
		if (dtAccount != null)
			elem.setAttribute("account", dtAccount);

		return elem;
	}
}

class Deposit extends Operation {

	Date   depDate;
	String depAccount;
	long   depValueCents;
	long   depExpenseCents;

	Deposit(Date date, String account, long value, long expense) {
		super();
		depDate         = date;
		depAccount      = account;
		depValueCents   = value;
		depExpenseCents = expense;
	}

	Deposit(Element elem) throws IllegalArgumentException {
		super();
		//depDate       = DateFormat.getDateInstance().parse(elem.getAttribute("date"));
		//depDate       = depDate.parse(elem.getAttribute("date"));
		depDate         = Preference.dateFormat.parse(elem.getAttribute("date"));
		depAccount      = elem.getAttribute("account");

		try { depValueCents = Long.parseLong(elem.getAttribute("value"));	}
		catch (NumberFormatException e) { depValueCents = 0; }

		try { depExpenseCents = Long.parseLong(elem.getAttribute("expense"));	}
		catch (NumberFormatException e) { depExpenseCents = 0; }
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(Preference.dateFormat.format(depDate));
		case 2: return new Label(depAccount);
		case 3: return new Label(Operation.accountAction[Operation.ACC_DEPOSIT]);
		case 4: return new Label(CurrencyUtil.format(depValueCents));
		case 5: return new Label(CurrencyUtil.format(depExpenseCents));
		default: return new Label("Deposit.getCol error col=" + col);
		}
	}

	Date getDate() {
		return depDate;
	}

	String getAccount() {
		return depAccount;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",    "deposit");
		elem.setAttribute("date",    Preference.dateFormat.format(depDate));
		elem.setAttribute("account", depAccount);
		elem.setAttribute("value",   String.valueOf(depValueCents));
		elem.setAttribute("expense", String.valueOf(depExpenseCents));

		return elem;
	}
}

class Withdraw extends Operation {

	Date   wdrawDate;
	String wdrawAccount;
	long   wdrawValueCents;

	Withdraw(Date date, String account, long value) {
		super();
		wdrawDate       = date;
		wdrawAccount    = account;
		wdrawValueCents = value;
	}

	Withdraw(Element elem) throws IllegalArgumentException {
		super();
		wdrawDate       = Preference.dateFormat.parse(elem.getAttribute("date"));
		wdrawAccount    = elem.getAttribute("account");
		wdrawValueCents = Long.parseLong(elem.getAttribute("value"));
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(Preference.dateFormat.format(wdrawDate));
		case 2: return new Label(wdrawAccount);
		case 3: return new Label(Operation.accountAction[Operation.ACC_WITHDRAW]);
		case 4: return new Label(CurrencyUtil.format(wdrawValueCents));
		case 5: return new Label("--");
		default: return new Label("Withdraw.getCol error col=" + col);
		}
	}

	Date getDate() {
		return wdrawDate;
	}

	String getAccount() {
		return wdrawAccount;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",    "withdraw");
		elem.setAttribute("date",    Preference.dateFormat.format(wdrawDate));
		elem.setAttribute("account", wdrawAccount);
		elem.setAttribute("value",   String.valueOf(wdrawValueCents));

		return elem;
	}
}

class Transfer extends Operation {

	Date   transferDate;
	String transferAccountFrom;
	String transferAccountTo;
	long   transferValueCents;

	Transfer(Date date, String accountFrom, String accountTo, long value) {
		super();
		transferDate        = date;
		transferAccountFrom = accountFrom;
		transferAccountTo   = accountTo;
		transferValueCents  = value;
	}

	Transfer(Element elem) throws IllegalArgumentException {
		super();
		transferDate        = Preference.dateFormat.parse(elem.getAttribute("date"));
		transferAccountFrom = elem.getAttribute("account_from");
		transferAccountTo   = elem.getAttribute("account_to");
		transferValueCents  = Long.parseLong(elem.getAttribute("value"));
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(Preference.dateFormat.format(transferDate));
		case 2: return new Label(getAccount());
		case 3: return new Label(Operation.accountAction[Operation.ACC_TRANSFER]);
		case 4: return new Label(CurrencyUtil.format(transferValueCents));
		case 5: return new Label("--");
		default: return new Label("Transfer.getCol error col=" + col);
		}
	}

	Date getDate() {
		return transferDate;
	}

	String getAccount() {
		return transferAccountFrom + "/" + transferAccountTo;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",         "transfer");
		elem.setAttribute("date",         Preference.dateFormat.format(transferDate));
		elem.setAttribute("account_from", transferAccountFrom);
		elem.setAttribute("account_to",   transferAccountTo);
		elem.setAttribute("value",        String.valueOf(transferValueCents));

		return elem;
	}
}


class Fee extends Operation {

	Date   feeDate;
	String feeAccount;
	long   feeValueCents;

	Fee(Date date, String account, long value) {
		super();
		feeDate       = date;
		feeAccount    = account;
		feeValueCents = value;
	}

	Fee(Element elem) throws IllegalArgumentException {
		super();
		feeDate       = Preference.dateFormat.parse(elem.getAttribute("date"));
		feeAccount    = elem.getAttribute("account");
		feeValueCents = Long.parseLong(elem.getAttribute("value"));
	}

	Widget getCol(int col) {
		switch (col) {
		case 0: return new Label(String.valueOf(id));
		case 1: return new Label(Preference.dateFormat.format(feeDate));
		case 2: return new Label(feeAccount);
		case 3: return new Label(Operation.accountAction[Operation.ACC_FEE]);
		case 4: return new Label(CurrencyUtil.format(feeValueCents));
		case 5: return new Label("--");
		default: return new Label("Fee.getCol error col=" + col);
		}
	}

	Date getDate() {
		return feeDate;
	}

	String getAccount() {
		return feeAccount;
	}

	Element appendChildNode(Document doc, Node parent) {
		Element elem = super.appendChildNode(doc, parent);

		elem.setAttribute("type",    "fee");
		elem.setAttribute("date",    Preference.dateFormat.format(feeDate));
		elem.setAttribute("account", feeAccount);
		elem.setAttribute("value",   String.valueOf(feeValueCents));

		return elem;
	}
}

class CarrierButton extends Button {
	Object opaqueObj;

	CarrierButton(String html, ClickHandler handler, Object opaque) {
		super(html, handler);
		opaqueObj = opaque;
	}
}

class DelOpButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		CarrierButton delButton = (CarrierButton) event.getSource();
		Operation op = (Operation) delButton.opaqueObj;
		CarteiraInveste.carteiraAtual.delOperation(op);
		CarteiraInveste.carteiraAtual.updateXmlDb();
	}
}

class DelQuoteButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		CarrierButton delButton = (CarrierButton) event.getSource();
		Quote q = (Quote) delButton.opaqueObj;
		CarteiraInveste.carteiraAtual.delQuote(q);
		CarteiraInveste.carteiraAtual.updateXmlDb();
	}
}

class TabEntry {
	Widget widget;
	String txt;

	TabEntry(Widget w, String t) {
		widget = w;
		txt   = t;
	}

	Widget getWidget() {
		return widget;
	}

	String getText() {
		return txt;
	}
}

class Carteira {
	VerticalPanel aboutPanel = new VerticalPanel();
	VerticalPanel mainPanel = new VerticalPanel();
	DecoratedTabPanel tabPanel = new DecoratedTabPanel();
	TabSelectionHandler mainTabSelectionHandler = new TabSelectionHandler();
	VerticalPanel opPanel = new VerticalPanel();
	VerticalPanel portfolioPanel = new VerticalPanel();
	VerticalPanel portfolioDebugPanel = new VerticalPanel();
	VerticalPanel yieldPanel = new VerticalPanel();
	VerticalPanel splitPanel = new VerticalPanel();
	VerticalPanel quotePanel = new VerticalPanel();
	VerticalPanel summaryPanel = new VerticalPanel();
	VerticalPanel evolutionPanel = new VerticalPanel();
	VerticalPanel taxPanel = new VerticalPanel();
	VerticalPanel prefPanel = new VerticalPanel();
	HorizontalPanel portfolioEndDatePanel = new HorizontalPanel();
	HorizontalPanel summaryEndDatePanel = new HorizontalPanel();
	HorizontalPanel taxEndDatePanel = new HorizontalPanel();
	VerticalPanel debugPanel = new VerticalPanel();

	HorizontalPanel tradeFilterPanel = new HorizontalPanel();
	MultiWordSuggestOracle assetNameOracle = new MultiWordSuggestOracle();
	MultiWordSuggestOracle accountNameOracle = new MultiWordSuggestOracle();
	TextBox tradeFilterYear = new TextBox();
	SuggestBox tradeFilterAssetSuggest = new SuggestBox(assetNameOracle);
	SuggestBox tradeFilterAccountSuggest = new SuggestBox(accountNameOracle);
	TradeFilterHandler tradeFilterHandler = new TradeFilterHandler();

	DateBox portfolioEndDate = new DateBox();
	DateBox summaryEndDate = new DateBox();
	DateBox evolutionEndDate = new DateBox();
	DateBox taxEndDate = new DateBox();
	PortfolioEndDateHandler portfolioEndDateHandler = new PortfolioEndDateHandler();
	SummaryEndDateHandler summaryEndDateHandler = new SummaryEndDateHandler();
	EvolutionEndDateHandler evolutionEndDateHandler = new EvolutionEndDateHandler();
	TaxEndDateHandler taxEndDateHandler = new TaxEndDateHandler();
	HorizontalPanel addAssetPanel = new HorizontalPanel();
	HorizontalPanel addAccountPanel = new HorizontalPanel();
	HorizontalPanel addYieldPanel = new HorizontalPanel();
	HorizontalPanel addSplitPanel = new HorizontalPanel();
	HorizontalPanel addQuotePanel = new HorizontalPanel();
	int addAssetPanelCurrType = Asset.ASSET_ACAO;
	int addAssetPanelCurrTrade = Operation.TRADE_BUY;
	int addAssetPanelCurrAccountIndex = 0; // builtin NONE
	ClickHandler addAssetHandler = new AddAssetButtonHandler();
	Button addAssetButton = new Button("Criar", addAssetHandler);
	ChangeHandler assetTypeHandler = new SelectAssetTypeHandler();
	ChangeHandler assetTradeHandler = new SelectTradeOpHandler();
	ChangeHandler assetAccountHandler = new SelectAssetAccountHandler();
	ChangeHandler currencyHandler = new TextBoxCurrencyHandler();
	ChangeHandler assetHandler = new TextBoxAssetHandler();
	ChangeHandler addAssetAmountHandler = new AddAssetAmountHandler();
	ChangeHandler addAssetGrossValueCurrencyHandler = new AddAssetGrossValueCurrencyHandler();
	ChangeHandler addAssetCostCurrencyHandler = new AddAssetCostCurrencyHandler();
	ChangeHandler addAssetExpenseCurrencyHandler = new AddAssetExpenseCurrencyHandler();
	ChangeHandler addAssetRetainedTaxCurrencyHandler = new AddAssetRetainedTaxCurrencyHandler();

	ChangeHandler addAssetDtBuyGrossCurrencyHandler = new AddAssetDtBuyGrossCurrencyHandler();
	ChangeHandler addAssetDtSellGrossCurrencyHandler = new AddAssetDtSellGrossCurrencyHandler();
	ChangeHandler addAssetDtSellNetCurrencyHandler = new AddAssetDtSellNetCurrencyHandler();

	ListBox assetTypeDropBox = new ListBox(false);
	ListBox operationDropBox = new ListBox(false);
	ListBox assetAccountDropBox = new ListBox(false);
	ListBox accountActionDropBox = new ListBox(false);
	ListBox yieldTypeDropBox = new ListBox(false);
	ListBox yieldAccountDropBox = new ListBox(false);
	ListBox quoteAssetDropBox = new ListBox(false);
	DateBox addAssetDate = new DateBox();
	TextBox addAssetName = new TextBox();
	TextBox addAssetAmount = new TextBox();
	TextBox addAssetGrossValue = new TextBox();
	TextBox addAssetCost = new TextBox();
	TextBox addAssetExpense = new TextBox();
	TextBox addAssetRetainedTax = new TextBox();
	Label addAssetAveragePriceLabel = new Label("avgPrice?");

	TextBox addAssetDtBuyGross = new TextBox();
	TextBox addAssetDtSellGross = new TextBox();
	TextBox addAssetDtSellNet = new TextBox();
	Label addAssetDtResultLabel = new Label("result?");

	ArrayList<Operation> operationList = new ArrayList<Operation>();
	int operationListTradeCount = 0;
	int operationListAccountCount = 0;
	int operationListYieldCount = 0;
	int operationListSplitCount = 0;
	Grid opTab = new Grid();
	Grid accTab = new Grid();
	Grid accountBalanceTab = new Grid();
	Grid portfolioTab = new Grid();
	Grid yieldTable = new Grid();
	Grid splitGrid = new Grid();
	Grid quoteGrid = new Grid();
	Grid summaryAccountGrid = new Grid();
	Grid summaryAssetGrid = new Grid();
	Grid summaryGrid = new Grid();
	Grid summaryFlowGrid = new Grid();
	Grid evolutionGrid = new Grid();
	Grid taxGrid = new Grid();
	Grid taxYearlySummaryGrid = new Grid();
	VerticalPanel dbPanel = new VerticalPanel();
	TextArea dbText = new TextArea();
	VerticalPanel summaryLogConsole = new VerticalPanel();
	VerticalPanel dbLogConsole = new VerticalPanel();
	ChangeHandler dbTextHandler = new DbTextChangeHandler();
	String currXmlTextDb = Constant.XML_HEADER;
	TreeSet<String> accountList = new TreeSet<String>();
	VerticalPanel accountPanel = new VerticalPanel();
	DateBox addAccountDate = new DateBox();
	TextBox addAccountName = new TextBox();
	TextBox addAccountFromName = new TextBox();
	TextBox addAccountToName = new TextBox();
	TextBox addAccountValue = new TextBox();
	TextBox addAccountExpense = new TextBox();
	ClickHandler addAccountHandler = new AddAccountButtonHandler();
	AccountActionChangeHandler accountActionHandler = new AccountActionChangeHandler();
	Button addAccountButton = new Button("Criar", addAccountHandler);
	HashMap<String,Asset> assetTable = new HashMap<String,Asset>();
	HashMap<String,Account> accountTable = new HashMap<String,Account>();
	HashMap<String,Quote> quoteTable = new HashMap<String,Quote>();
	DateBox addYieldDate = new DateBox();
	//TextBox addYieldTaxYear = new TextBox();
	TextBox addYieldAsset = new TextBox();
	TextBox addYieldAmount = new TextBox();
	TextBox addYieldGrossValue = new TextBox();
	TextBox addYieldNetValue = new TextBox();
	ClickHandler addYieldHandler = new AddYieldButtonHandler();
	Button addYieldButton = new Button("Criar", addYieldHandler);

	DateBox addSplitDate = new DateBox();
	SuggestBox addSplitAssetSuggest = new SuggestBox(assetNameOracle);
	TextBox addSplitFrom = new TextBox();
	TextBox addSplitTo = new TextBox();
	AddSplitButtonHandler addSplitHandler = new AddSplitButtonHandler();
	Button addSplitButton = new Button("Criar", addSplitHandler);

	DateBox addQuoteDate = new DateBox();
	TextBox addQuoteAmount = new TextBox();
	TextBox addQuoteValue = new TextBox();
	ClickHandler addQuoteHandler = new AddQuoteButtonHandler();
	Button addQuoteButton = new Button("Criar", addQuoteHandler);
	SummaryUpdateButtonHandler summaryUpdateButtonHandler = new SummaryUpdateButtonHandler();
	Button summaryUpdateButton = new Button("Atualizar", summaryUpdateButtonHandler);
	HashMap<String,SummaryAccount> summaryAccountTable = new HashMap<String,SummaryAccount>();
	HashMap<String,SummaryAsset> summaryAssetTable = new HashMap<String,SummaryAsset>();
	SummaryAccountChangeHandler summaryAccountHandler = new SummaryAccountChangeHandler();
	SummaryAssetChangeHandler summaryAssetHandler = new SummaryAssetChangeHandler();

	EvolutionUpdateButtonHandler evolutionUpdateButtonHandler = new EvolutionUpdateButtonHandler();
	Button evolutionUpdateButton = new Button("Atualizar", evolutionUpdateButtonHandler);
	
	TextBox prefSellSpreadText     = new TextBox();
	TextBox prefBrokerFeeText      = new TextBox();
	TextBox prefTaxExemptionText   = new TextBox();
	TextBox prefTaxFeeText         = new TextBox();
	TextBox prefDayTradeTaxFeeText = new TextBox();
	ChangeHandler prefChangeHandler = new PreferenceChangeHandler();

	ClickHandler clearTodayQuoteHandler = new ClearTodayQuoteHandler();
	Button clearTodayQuoteButton = new Button("Excluir Hoje", clearTodayQuoteHandler);
	ClickHandler clearPastQuoteHandler = new ClearPastQuoteHandler();
	Button clearPastQuoteButton = new Button("Excluir Anteriores", clearPastQuoteHandler);

	CheckBox portfolioHideSoldPositionsCheckBox = new CheckBox("Ocultar " + Constant.POSITIONS + " Vendidas");
	PortfolioHideSoldPositionsHandler portfolioHideSoldPositionsHandler = new PortfolioHideSoldPositionsHandler();
	ChangeHandler unitQuoteHandler = new UnitQuoteHandler();
	ChangeHandler sellGrossValueHandler = new SellGrossValueHandler();
	ChangeHandler sellRetainedTaxHandler = new SellRetainedTaxHandler();

	EditDepositHandler editDepositBoxHandler = new EditDepositHandler();

	PreferenceDayTradeAffectExemptionHandler dayTradeAffectExemptionHandler = new PreferenceDayTradeAffectExemptionHandler();
	PreferenceExemptGainReduceLossHandler exemptGainReduceLossHandler = new PreferenceExemptGainReduceLossHandler();
	PreferenceTaxRatioOverPretax taxRatioOverPretaxHandler = new PreferenceTaxRatioOverPretax();

	StartTaxCarryLossChangeHandler startTaxCarryLossHandler = new StartTaxCarryLossChangeHandler();
	StartTaxDayTradeCarryLossChangeHandler startTaxDayTradeCarryLossHandler = new StartTaxDayTradeCarryLossChangeHandler();

        QuoteServiceAsync quoteService = null;

	static final int TAB_PORTFOLIO   = 0;
	static final int TAB_OPERATIONS  = 1;
	static final int TAB_YIELD       = 2;
	static final int TAB_SPLIT       = 3;
	static final int TAB_ACCOUNTS    = 4;
	static final int TAB_QUOTES      = 5;
	static final int TAB_RESULT      = 6;
	static final int TAB_TAX         = 7;
	static final int TAB_PREFERENCES = 8;
	static final int TAB_DB          = 9;
	static final int TAB_ABOUT       = 10;

	TabEntry mainTabList[] = {
		new TabEntry(portfolioPanel,	"Carteira"),
		new TabEntry(opPanel,		Constant.OPERATIONS),
		new TabEntry(yieldPanel,	"Proventos"),
		new TabEntry(splitPanel,	"Desdobramentos"),
		new TabEntry(accountPanel,	"Contas"),
		new TabEntry(quotePanel,	Constant.QUOTES),
		new TabEntry(summaryPanel,	"Resultado"),
		new TabEntry(evolutionPanel,	Constant.EVOLUTION),
		new TabEntry(taxPanel,		"IR"),
		new TabEntry(prefPanel,		Constant.PREFERENCES),
		new TabEntry(dbPanel,		"Banco de Dados"),
		new TabEntry(aboutPanel,	"Sobre")

	};

	Carteira() {
		accountList.add("--"); // built-in account NONE

		aboutPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		aboutPanel.add(new Label("Carteira Investe"));
		aboutPanel.add(new Label(Constant.VERSION + ": " + Constant.V_NUMBER));
		aboutPanel.add(new HTML(Constant.LICENSE));

		portfolioEndDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		portfolioEndDate.setValue(new Date());
		portfolioEndDate.addValueChangeHandler(portfolioEndDateHandler);

		summaryEndDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		summaryEndDate.setValue(new Date());
		summaryEndDate.addValueChangeHandler(summaryEndDateHandler);

		evolutionEndDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		evolutionEndDate.setValue(new Date());
		evolutionEndDate.addValueChangeHandler(evolutionEndDateHandler);

		taxEndDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		taxEndDate.setValue(new Date());
		taxEndDate.addValueChangeHandler(taxEndDateHandler);

		tabPanel.addSelectionHandler(mainTabSelectionHandler);

		addYieldAsset.addChangeHandler(assetHandler);
		addAssetName.addChangeHandler(assetHandler);
		addAssetAmount.addChangeHandler(addAssetAmountHandler);
		addAssetGrossValue.addChangeHandler(addAssetGrossValueCurrencyHandler);
		addAssetCost.addChangeHandler(addAssetCostCurrencyHandler);
		addAssetExpense.addChangeHandler(addAssetExpenseCurrencyHandler);
		addAssetRetainedTax.addChangeHandler(addAssetRetainedTaxCurrencyHandler);
		addAccountValue.addChangeHandler(currencyHandler);
		addAccountExpense.addChangeHandler(currencyHandler);
		addYieldGrossValue.addChangeHandler(currencyHandler);
		addYieldNetValue.addChangeHandler(currencyHandler);

		addAssetDtBuyGross.addChangeHandler(addAssetDtBuyGrossCurrencyHandler);
		addAssetDtSellGross.addChangeHandler(addAssetDtSellGrossCurrencyHandler);
		addAssetDtSellNet.addChangeHandler(addAssetDtSellNetCurrencyHandler);

		assetAccountDropBox.setSelectedIndex(addAssetPanelCurrAccountIndex);
		assetAccountDropBox.addChangeHandler(assetAccountHandler);

		tradeFilterYear.addChangeHandler(tradeFilterHandler);
		tradeFilterAssetSuggest.getValueBox().addChangeHandler(tradeFilterHandler);
		tradeFilterAccountSuggest.getValueBox().addChangeHandler(tradeFilterHandler);
		tradeFilterAssetSuggest.addSelectionHandler(tradeFilterHandler);
		tradeFilterAccountSuggest.addSelectionHandler(tradeFilterHandler);

		portfolioHideSoldPositionsCheckBox.setValue(false);
		portfolioHideSoldPositionsCheckBox.addValueChangeHandler(portfolioHideSoldPositionsHandler);

		prefSellSpreadText.addChangeHandler(prefChangeHandler);
		prefBrokerFeeText.addChangeHandler(prefChangeHandler);
		prefTaxExemptionText.addChangeHandler(prefChangeHandler);
		prefTaxFeeText.addChangeHandler(prefChangeHandler);
		prefDayTradeTaxFeeText.addChangeHandler(prefChangeHandler);

		Preference.stockDayTradeAffectExemptionLimit.addValueChangeHandler(dayTradeAffectExemptionHandler);
		Preference.stockExemptGainsReduceCarriedLoss.addValueChangeHandler(exemptGainReduceLossHandler);
		Preference.stockTaxRatioOverPretaxEarnings.addValueChangeHandler(taxRatioOverPretaxHandler);

		Preference.startTaxCarryLoss.addChangeHandler(startTaxCarryLossHandler);
		Preference.startTaxDayTradeCarryLoss.addChangeHandler(startTaxDayTradeCarryLossHandler);

		taxGrid.addStyleName("taxGrid");
		taxYearlySummaryGrid.addStyleName("taxGrid");
		summaryAccountGrid.addStyleName("boxedTable");
		summaryAssetGrid.addStyleName("boxedTable");
		summaryGrid.addStyleName("boxedTable");
		summaryFlowGrid.addStyleName("boxedTable");
		evolutionGrid.addStyleName("boxedTable");

		build();
	}

	void buildAddAssetAccountDropBox() {
		assetAccountDropBox.clear();

		for (Object element : accountList)
			assetAccountDropBox.addItem((String) element);
	}

	void updateAddAssetAveragePriceLabel() {

		double amount = -1;
		try {
			amount = Double.parseDouble(addAssetAmount.getText());
		}
		catch (NumberFormatException e) {
			addAssetAveragePriceLabel.setText("(bad-amount?)");
			return;
		}
		if (Preference.amountIsZero(amount)) {
			addAssetAveragePriceLabel.setText("(zero-amount?)");
			return;
		}

		long costCents = CurrencyUtil.parseToCents(addAssetCost.getText());
		double cost = ((double) costCents) / 100;

		String valueStr;

		switch (addAssetPanelCurrTrade) {
			case Operation.TRADE_BUY: {
				long expenseCents = CurrencyUtil.parseToCents(addAssetExpense.getText());
				double expense = ((double) expenseCents) / 100;

				double value = (cost + expense) / amount;
				valueStr = Preference.unitaryQuoteFormat.format(value);
			}
			break;
			case Operation.TRADE_SELL: {
				double value = cost / amount;
				valueStr = Preference.unitaryQuoteFormat.format(value);
			}
			break;
			default:
				valueStr = "(buy-sell?)";
		}

		addAssetAveragePriceLabel.setText(valueStr);
	}

	void updateAddAssetDtNetResultLabel() {
	}

	void buildAddAssetPanel() {
		// Create addAssetPanel
		addAssetPanel.clear();

		addAssetPanel.add(assetTypeDropBox);
		addAssetPanel.add(operationDropBox);

		// Create a DateBox
		addAssetDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		addAssetDate.setValue(new Date());
		addAssetPanel.add(addAssetDate);

		buildAddAssetAccountDropBox();

		switch (addAssetPanelCurrTrade) {
			case Operation.TRADE_BUY: {
		VerticalPanel namePanel = new VerticalPanel();
		VerticalPanel amountPanel = new VerticalPanel();
		VerticalPanel costPanel = new VerticalPanel();
		VerticalPanel expensePanel = new VerticalPanel();
		VerticalPanel accPanel = new VerticalPanel();
		VerticalPanel avgPricePanel = new VerticalPanel();

		namePanel.add(new Label("Ativo"));
		amountPanel.add(new Label("Quant."));
		costPanel.add(new Label("Custo Bruto"));
		expensePanel.add(new Label("Remessa"));
		accPanel.add(new Label("Conta"));
		avgPricePanel.add(new Label("C." + Constant.AVERAGE_O));

		namePanel.add(addAssetName);
		amountPanel.add(addAssetAmount);
		costPanel.add(addAssetCost);
		expensePanel.add(addAssetExpense);
		accPanel.add(assetAccountDropBox);
		avgPricePanel.add(addAssetAveragePriceLabel);

		addAssetPanel.add(namePanel);
		addAssetPanel.add(amountPanel);
		addAssetPanel.add(costPanel);
		addAssetPanel.add(expensePanel);
		addAssetPanel.add(accPanel);
		addAssetPanel.add(avgPricePanel);
			}
			break;

			case Operation.TRADE_SELL: {
		VerticalPanel namePanel = new VerticalPanel();
		VerticalPanel amountPanel = new VerticalPanel();
		VerticalPanel grossPanel = new VerticalPanel();
		VerticalPanel costPanel = new VerticalPanel();
		VerticalPanel retainedTaxPanel = new VerticalPanel();
		VerticalPanel accPanel = new VerticalPanel();
		VerticalPanel avgPricePanel = new VerticalPanel();

		namePanel.add(new Label("Ativo"));
		amountPanel.add(new Label("Quant."));
		grossPanel.add(new Label("Valor Bruto"));
		costPanel.add(new Label("Receita " + Constant.NET_A));
		retainedTaxPanel.add(new Label("IRRF"));
		accPanel.add(new Label("Conta"));
		avgPricePanel.add(new Label("P." + Constant.AVERAGE_O));

		namePanel.add(addAssetName);
		amountPanel.add(addAssetAmount);
		grossPanel.add(addAssetGrossValue);
		costPanel.add(addAssetCost);
		retainedTaxPanel.add(addAssetRetainedTax);
		accPanel.add(assetAccountDropBox);
		avgPricePanel.add(addAssetAveragePriceLabel);

		addAssetPanel.add(namePanel);
		addAssetPanel.add(amountPanel);
		addAssetPanel.add(grossPanel);
		addAssetPanel.add(costPanel);
		addAssetPanel.add(retainedTaxPanel);
		addAssetPanel.add(accPanel);
		addAssetPanel.add(avgPricePanel);
			}
			break;

			case Operation.TRADE_DAYTRADE: {
				VerticalPanel namePanel = new VerticalPanel();
				VerticalPanel amountPanel = new VerticalPanel();
				VerticalPanel buyGrossPanel = new VerticalPanel();
				VerticalPanel sellGrossPanel = new VerticalPanel();
				VerticalPanel sellNetPanel = new VerticalPanel();
				VerticalPanel retainedTaxPanel = new VerticalPanel();
				VerticalPanel accPanel = new VerticalPanel();
				VerticalPanel netResultPanel = new VerticalPanel();

				namePanel.add(new Label("Ativo"));
				amountPanel.add(new Label("Quant."));
				buyGrossPanel.add(new Label("Compra.Bruta"));
				sellGrossPanel.add(new Label("Venda.Bruta"));
				sellNetPanel.add(new Label("Venda.Liq."));
				retainedTaxPanel.add(new Label("IRRF"));
				accPanel.add(new Label("Conta"));
				netResultPanel.add(new Label("Resultado"));

				namePanel.add(addAssetName);
				amountPanel.add(addAssetAmount);
				buyGrossPanel.add(addAssetDtBuyGross);
				sellGrossPanel.add(addAssetDtSellGross);
				sellNetPanel.add(addAssetDtSellNet);
				retainedTaxPanel.add(addAssetRetainedTax);
				accPanel.add(assetAccountDropBox);
				netResultPanel.add(addAssetDtResultLabel);

				addAssetPanel.add(namePanel);
				addAssetPanel.add(amountPanel);
				addAssetPanel.add(buyGrossPanel);
				addAssetPanel.add(sellGrossPanel);
				addAssetPanel.add(sellNetPanel);
				addAssetPanel.add(retainedTaxPanel);
				addAssetPanel.add(accPanel);
				addAssetPanel.add(netResultPanel);
			}
			break;
			default:
		}

		// Add a normal button
		addAssetPanel.add(addAssetButton);
	}

	void buildTradeFilterPanel() {
		tradeFilterPanel.clear();

		VerticalPanel yearPanel = new VerticalPanel();
		VerticalPanel assetSuggest = new VerticalPanel();
		VerticalPanel accountSuggest = new VerticalPanel();

		yearPanel.add(new Label("Ano"));
		yearPanel.add(tradeFilterYear);
		assetSuggest.add(new Label("Ativo"));
		assetSuggest.add(tradeFilterAssetSuggest);
		accountSuggest.add(new Label("Conta"));
		accountSuggest.add(tradeFilterAccountSuggest);

		tradeFilterPanel.add(new Label("Mostrar somente:"));
		tradeFilterPanel.add(yearPanel);
		tradeFilterPanel.add(assetSuggest);
		tradeFilterPanel.add(accountSuggest);
	}

	void buildOpPanel() {
		// Create operacao panel
		opPanel.clear();

		opPanel.add(addAssetPanel);
		opPanel.add(tradeFilterPanel);
		opPanel.add(new Label("operacoes=" + operationList.size() + " negocios=" + operationListTradeCount + " contas=" + operationListAccountCount));
		opPanel.add(opTab);
	}

	void buildSummaryPanel() {
		HorizontalPanel selectionPanel = new HorizontalPanel();

		summaryPanel.clear();
		summaryEndDatePanel.add(new Label("Data:"));
		summaryEndDatePanel.add(summaryEndDate);

		selectionPanel.add(summaryAccountGrid);
		selectionPanel.add(summaryAssetGrid);

		summaryPanel.add(summaryEndDatePanel);
		summaryPanel.add(summaryUpdateButton);
		summaryPanel.add(selectionPanel);
		summaryPanel.add(summaryGrid);
		summaryPanel.add(summaryFlowGrid);
		summaryPanel.add(summaryLogConsole);
	}

	void summaryLog(String msg) {
		summaryLogConsole.add(new Label(msg));
	}

	void updateSummary() {
		summaryLogConsole.clear();

		//
		// Refresh account selection table
		//
		summaryLog("Re-scanning accounts... " + summaryAccountTable.size() + " to " + accountTable.size());
		{
			HashMap<String,SummaryAccount> newSummaryAccountTable = new HashMap<String,SummaryAccount>();
			for (Account a : accountTable.values()) {
				SummaryAccount sa = (SummaryAccount) summaryAccountTable.get(a.acctName);
				if (sa == null) {
					sa = new SummaryAccount(a.acctName);
				}
				newSummaryAccountTable.put(a.acctName, sa);
			}
			summaryAccountTable = newSummaryAccountTable;
		}
		summaryLog("Re-scanning accounts...done " + summaryAccountTable.size() + " to " + accountTable.size());

		//
		// Refresh asset selection table
		//
		summaryLog("Re-scanning assets... " + summaryAssetTable.size() + " to " + assetTable.size());

		issueAssetTable(summaryEndDate.getValue(), null, null);
		assetTableDropSoldPositions();

		{
			HashMap<String,SummaryAsset> newSummaryAssetTable = new HashMap<String,SummaryAsset>();
			for (Asset a : assetTable.values()) {
				SummaryAsset sa = (SummaryAsset) summaryAssetTable.get(a.assetName);
				if (sa == null) {
					sa = new SummaryAsset(a.assetName);
				}
				newSummaryAssetTable.put(a.assetName, sa);
			}
			summaryAssetTable = newSummaryAssetTable;
		}
		summaryLog("Re-scanning assets...done " + summaryAssetTable.size() + " to " + assetTable.size());

		long sumDeposit    = 0;
		long sumWithdraw   = 0;
		long sumBalance    = 0;
		
		//
		// Display account selections
		//
		summaryAccountGrid.clear();
		int rows = summaryAccountTable.size() + 2;
		summaryAccountGrid.resize(rows, 4);
		summaryAccountGrid.setWidget(0, 0, new Label("Conta"));
		summaryAccountGrid.setWidget(0, 1, new Label("Dep."));
		summaryAccountGrid.setWidget(0, 2, new Label("Saq."));
		summaryAccountGrid.setWidget(0, 3, new Label("Saldo"));
		int row = 1;
		for (SummaryAccount sa : summaryAccountTable.values()) {
			Account a = accountTable.get(sa.accountName);
			String accDepositsStr    = "?";
			String accWithdrawalsStr = "?";
			String accBalanceStr     = "?" ;
			if (a != null) {
				long balanceCents = a.getBalanceCents();
					
				accDepositsStr    = CurrencyUtil.format(a.depositsCents);
				accWithdrawalsStr = CurrencyUtil.format(a.withdrawalsCents);
				accBalanceStr     = CurrencyUtil.format(balanceCents);
				
				if (sa.checkBox.getValue()) {
					sumDeposit  += a.depositsCents;
					sumWithdraw += a.withdrawalsCents;
					sumBalance  += balanceCents;
				}
			}

			summaryAccountGrid.setWidget(row, 0, sa.checkBox);
			summaryAccountGrid.setWidget(row, 1, new Label(accDepositsStr));
			summaryAccountGrid.setWidget(row, 2, new Label(accWithdrawalsStr));
			summaryAccountGrid.setWidget(row, 3, new Label(accBalanceStr));

			++row;
		}
		int lastRow = rows - 1;
		summaryAccountGrid.setWidget(lastRow, 1, new Label(CurrencyUtil.format(sumDeposit)));
		summaryAccountGrid.setWidget(lastRow, 2, new Label(CurrencyUtil.format(sumWithdraw)));
		summaryAccountGrid.setWidget(lastRow, 3, new Label(CurrencyUtil.format(sumBalance)));
		
		long sumRealizable = 0;

		//
		// Display asset selections
		//
		summaryAssetGrid.clear();
		rows = summaryAssetTable.size() + 2;
		summaryAssetGrid.resize(rows, 2);
		summaryAssetGrid.setWidget(0, 0, new Label("Ativo"));
		summaryAssetGrid.setWidget(0, 1, new Label("Realizavel"));
		row = 1;
		for (SummaryAsset sa : summaryAssetTable.values()) {
			String realizableStr = "?";
			Asset a = assetTable.get(sa.assetName);
			if (a != null) {
				Quote assetQuote = findQuote(a.assetName, summaryEndDate.getValue());
				if (assetQuote != null) {
					long realizableCents = a.getRealizableCents(assetQuote);
					realizableStr = CurrencyUtil.format(realizableCents);
					if (sa.checkBox.getValue()) {
						sumRealizable += realizableCents;
					}
				}
			}

			summaryAssetGrid.setWidget(row, 0, sa.checkBox);
			summaryAssetGrid.setWidget(row, 1, new Label(realizableStr));

			++row;
		}
		lastRow = rows - 1;
		summaryAssetGrid.setWidget(lastRow, 1, new Label(CurrencyUtil.format(sumRealizable)));

		long equityCents = sumBalance + sumRealizable;
		long equityPlusWithdrawalsCents = equityCents + sumWithdraw;
		long totalEarningsCents = equityPlusWithdrawalsCents - sumDeposit;
		double totalResult = ((double) totalEarningsCents) / ((double) sumDeposit);

		long equityEarningsCents = equityCents - sumDeposit;
		double equityGrowth = ((double) equityEarningsCents) / ((double) sumDeposit);
		
		summaryGrid.clear();
		summaryGrid.resize(10, 3);

		summaryGrid.setWidget(0, 0, new Label("A"));
		summaryGrid.setWidget(1, 0, new Label("B"));
		summaryGrid.setWidget(2, 0, new Label("C"));
		summaryGrid.setWidget(3, 0, new Label("D"));
		summaryGrid.setWidget(4, 0, new Label("E"));
		summaryGrid.setWidget(5, 0, new Label("F"));
		summaryGrid.setWidget(6, 0, new Label("G"));
		summaryGrid.setWidget(7, 0, new Label("H"));
		summaryGrid.setWidget(8, 0, new Label("I"));
		summaryGrid.setWidget(9, 0, new Label("J"));

		summaryGrid.setWidget(0, 1, new Label("Depositos:"));
		summaryGrid.setWidget(1, 1, new Label("Saques+Saldo+Realizavel:"));
		summaryGrid.setWidget(2, 1, new Label("Ganho interno total (B-A):"));
		summaryGrid.setWidget(3, 1, new Label("Retorno interno total do investimento (C/A):"));
		summaryGrid.setWidget(4, 1, new Label("Patrimonio (Saldo+Realizavel):"));
		summaryGrid.setWidget(5, 1, new Label("Ganho patrimonial (E-A):"));
		summaryGrid.setWidget(6, 1, new Label("Crescimento patrimonial (F/A):"));
		summaryGrid.setWidget(7, 1, new Label("Taxa composta diaria:"));
		summaryGrid.setWidget(8, 1, new Label("Taxa composta mensal:"));
		summaryGrid.setWidget(9, 1, new Label("Taxa composta anual:"));

		summaryGrid.setWidget(0, 2, new Label(CurrencyUtil.format(sumDeposit)));
		summaryGrid.setWidget(1, 2, new Label(CurrencyUtil.format(equityPlusWithdrawalsCents)));
		summaryGrid.setWidget(2, 2, new Label(CurrencyUtil.format(totalEarningsCents)));
		summaryGrid.setWidget(3, 2, new Label(Percent.format2(totalResult)));
		summaryGrid.setWidget(4, 2, new Label(CurrencyUtil.format(equityCents)));
		summaryGrid.setWidget(5, 2, new Label(CurrencyUtil.format(equityEarningsCents)));
		summaryGrid.setWidget(6, 2, new Label(Percent.format2(equityGrowth)));

		//
		// Scan deposits and withdrawals
		//
		summaryLog("Building polynomial...");
		Date endDate = summaryEndDate.getValue();
		//int endMon = DateUtil.linearMonth(endDate);
		ArrayList<Term> poly = new ArrayList<Term>();
		for (Operation op : operationList) {
			Date opDate = op.getDate();

			// Skip newer than endDate operations
			if (opDate.compareTo(endDate) > 0)
				break;

			// Pick only these
			if (!(op instanceof Deposit) && !(op instanceof Withdraw)) {
				continue;
			}

			// Skip non-selected accounts
			String accountName = op.getAccount();
			if (accountName == null)
				continue;
			SummaryAccount sa = summaryAccountTable.get(accountName);
			if (sa == null)
				continue;
			if (!sa.checkBox.getValue())
				continue;

			long valueCents =  0;
			if (op instanceof Deposit) {
				Deposit d = (Deposit) op;
				valueCents = d.depValueCents + d.depExpenseCents;
			}
			else {
				Withdraw w = (Withdraw) op;
				valueCents = -w.wdrawValueCents;
			}

			// Find month
			//int currMon = DateUtil.linearMonth(opDate);
			//int months = endMon - currMon;

			long days = DateUtil.daysBetween(opDate, endDate);

			summaryLog(Preference.dateFormat.format(opDate) + " days=" + days + " value=" + CurrencyUtil.format(valueCents));

			poly.add(new Term(valueCents, days));
		}

                // add constant to polynomial
		summaryLog(Preference.dateFormat.format(endDate) + " days=" + 0 + " value=" + CurrencyUtil.format(equityCents));
		poly.add(new Term(- equityCents, 0));


		summaryLog("Building polynomial...done " + poly.size() + " terms");

		summaryFlowGrid.clear();
		summaryFlowGrid.resize(1 + poly.size(), 3);
		summaryFlowGrid.setWidget(0, 0, new Label("Dias"));
		summaryFlowGrid.setWidget(0, 1, new Label("Fluxo"));
		summaryFlowGrid.setWidget(0, 2, new Label("Data"));
		int polyRow = 0;
		for (Term t : poly) {
			++polyRow;

			long days = (long) t.exponent;
			Date pastDate = DateUtil.addDays(endDate, -days);

			summaryFlowGrid.setWidget(polyRow, 0, new Label(String.valueOf(days)));
			summaryFlowGrid.setWidget(polyRow, 1, new Label(CurrencyUtil.localeFormat(- (long) t.coeficient)));
			summaryFlowGrid.setWidget(polyRow, 2, new Label(Preference.dateFormat.format(pastDate)));
		}

		int maxLoop = 20;
		double maxError = .00001;
		double x0 = 1.0003; // 0,03% ao dia
		summaryLog("Solving... maxLoop=" + maxLoop + " maxError=" + maxError + " x0=" + (100 * (x0 - 1)) + "%");
		double x = x0;
		double err = 1;
		for (int i = 0; i < maxLoop; ++i) {
			double y = Polynomial.eval(poly, x);
			err = Math.abs(y);
			summaryLog("i=" + i + " x=" + x + " y=" + y + " err=" + err);
			if (err < maxError)
				break;
			double dy = Polynomial.evalDerivative(poly, x);
			double newX = x - y / dy;
			summaryLog("i=" + i + " x=" + x + " y=" + y + " err=" + err + " dy=" + dy + " newX=" + newX);
			x = newX;
		}
		
		if (err < maxError) {
			summaryLog("Solving...success");
			summaryGrid.setWidget(7, 2, new Label(Percent.format6(x - 1)));
			summaryGrid.setWidget(8, 2, new Label(Percent.format4(Math.pow(x, 30) - 1)));
			summaryGrid.setWidget(9, 2, new Label(Percent.format2(Math.pow(x, 365) - 1)));
		}
		else {
			summaryLog("Solving...failure to converge");
			summaryGrid.setWidget(7, 2, new Label("failure to converge"));
			summaryGrid.setWidget(8, 2, new Label("failure to converge"));
			summaryGrid.setWidget(9, 2, new Label("failure to converge"));
		}
	}

	void buildEvolutionPanel() {
		evolutionPanel.clear();
		evolutionPanel.add(evolutionEndDate);
		evolutionPanel.add(evolutionUpdateButton);
		evolutionPanel.add(evolutionGrid);
	}

	void displayEvolutionMonth(final int COLS, int row, int month,
						long deposits, long totalDeposits,
						long withdrawals, long totalWithdrawals) {

		int rows = 1 + row;
		if (rows > evolutionGrid.getRowCount()) {
			// Grow grid
			evolutionGrid.resize(rows, COLS);
		}

		String monStr = DateUtil.monthStr(month);
		long investmentBalance = totalDeposits - totalWithdrawals;

		evolutionGrid.setWidget(row, 0, new Label(monStr));
		evolutionGrid.setWidget(row, 1, new Label(CurrencyUtil.format(deposits)));
		evolutionGrid.setWidget(row, 2, new Label(CurrencyUtil.format(totalDeposits)));
		evolutionGrid.setWidget(row, 3, new Label(CurrencyUtil.format(withdrawals)));
		evolutionGrid.setWidget(row, 4, new Label(CurrencyUtil.format(totalWithdrawals)));
		evolutionGrid.setWidget(row, 5, new Label(CurrencyUtil.format(investmentBalance)));
	}

	void updateEvolution() {
		final int COLS = 9;
		int rows = 0;

		evolutionGrid.clear();
		evolutionGrid.resize(rows + 1, COLS); // header
		evolutionGrid.getRowFormatter().addStyleName(0, "tableHeader");

		//
		// Header
		//
		evolutionGrid.setWidget(0, 0, new Label(Constant.MONTH));
		evolutionGrid.setWidget(0, 1, new Label(Constant.DEPOSITS));
		evolutionGrid.setWidget(0, 2, new Label("Total de " + Constant.DEPOSITS));
		evolutionGrid.setWidget(0, 3, new Label("Saques"));
		evolutionGrid.setWidget(0, 4, new Label("Total de Saques"));
		evolutionGrid.setWidget(0, 5, new Label("Saldo Investido"));

		evolutionGrid.setWidget(0, 6, new Label("Caixa"));
		evolutionGrid.setWidget(0, 7, new Label("Realizavel"));
		evolutionGrid.setWidget(0, 8, new Label("Patrimonio"));

		long deposits         = 0;
		long totalDeposits    = 0;
		long withdrawals      = 0;
		long totalWithdrawals = 0;
		int firstMon = -1;
		int prevMon = -1;
		int currMon = 0;
		Date endDate = evolutionEndDate.getValue();
		for (Operation op : operationList) {
			Date opDate = op.getDate();

			// Skip newer than endDate operations
			if (opDate.compareTo(endDate) > 0)
				break;

			// Pick only these
			if (!(op instanceof Deposit) && !(op instanceof Withdraw)) {
				continue;
			}

			// Find month
			currMon = DateUtil.linearMonth(opDate);
			if (firstMon < 0) {
				firstMon = currMon;
				prevMon  = currMon;
			}
			if (currMon != prevMon) {
				int row = 1 + prevMon - firstMon;

				displayEvolutionMonth(COLS, row, prevMon, deposits, totalDeposits, withdrawals, totalWithdrawals);
								
				deposits = 0;
				withdrawals = 0;

				prevMon = currMon;
			}

			if (op instanceof Deposit) {
				Deposit d = (Deposit) op;
				deposits += d.depValueCents;
				totalDeposits += d.depValueCents;
			}
			else {
				Withdraw w = (Withdraw) op;
				withdrawals += w.wdrawValueCents;
				totalWithdrawals += w.wdrawValueCents;
			}

		}

		if (deposits + withdrawals > 0) {

			// Last month
			int row = 1 + prevMon - firstMon;
			displayEvolutionMonth(COLS, row, currMon, deposits, totalDeposits, withdrawals, totalWithdrawals);

			// Averages

			long investmentBalance = totalDeposits - totalWithdrawals;

			// Grow grid
			int avgRow = evolutionGrid.getRowCount();
			long months = avgRow - 1;

			evolutionGrid.resize(1 + avgRow, COLS);

			evolutionGrid.setWidget(avgRow, 0, new Label("Media"));
			evolutionGrid.setWidget(avgRow, 2, new Label(CurrencyUtil.format(totalDeposits / months)));
			evolutionGrid.setWidget(avgRow, 4, new Label(CurrencyUtil.format(totalWithdrawals / months)));
			evolutionGrid.setWidget(avgRow, 5, new Label(CurrencyUtil.format(investmentBalance / months)));
		}
	}

	void buildTaxPanel() {
		HorizontalPanel headPanel = new HorizontalPanel();
		VerticalPanel prefPanel = new VerticalPanel();
		VerticalPanel startLossPanel = new VerticalPanel();
		HorizontalPanel lossPanel = new HorizontalPanel();
		HorizontalPanel dayTradeLossPanel = new HorizontalPanel();

		taxPanel.clear();
		taxEndDatePanel.add(new Label("Data:"));
		taxEndDatePanel.add(taxEndDate);
		taxPanel.add(taxEndDatePanel);

		StockTaxPrefResetButtonHandler stockTaxPrefButtonHandler = new StockTaxPrefResetButtonHandler();
		Button stockTaxPrefButton = new Button("Restaurar Recomendacoes", stockTaxPrefButtonHandler);

		prefPanel.add(stockTaxPrefButton);
		prefPanel.add(Preference.stockDayTradeAffectExemptionLimit);
		prefPanel.add(Preference.stockExemptGainsReduceCarriedLoss);
		prefPanel.add(Preference.stockTaxRatioOverPretaxEarnings);
		prefPanel.addStyleName("boxedTable");

		lossPanel.add(new Label("Perda acumulada inicial:"));
		lossPanel.add(Preference.startTaxCarryLoss);
		dayTradeLossPanel.add(new Label("Perda acumulada inicial em Day-trade:"));
		dayTradeLossPanel.add(Preference.startTaxDayTradeCarryLoss);

		startLossPanel.add(lossPanel);
		startLossPanel.add(dayTradeLossPanel);

		headPanel.add(prefPanel);
		headPanel.add(startLossPanel);
		taxPanel.add(headPanel);

		taxPanel.add(new Label("Apuracao Mensal:"));
		taxPanel.add(taxGrid);
		taxPanel.add(new Label("Legenda:"));
		taxPanel.add(new Label("IR.Devido: Codigo Receita DARF: Pessoa Fisica = 6015, Pessoa Juridica = 3317"));
		taxPanel.add(new Label("DT.IR.Devido: Codigo Receita DARF: Pessoa Fisica = 6015, Pessoa Juridica = 3317"));
		taxPanel.add(new Label("Resumo Anual:"));
		taxPanel.add(taxYearlySummaryGrid);
		taxPanel.add(new Label("Legenda:"));
		taxPanel.add(new Label("Repasse BTC: Rendimentos Tributaveis"));
		taxPanel.add(new Label("Lucro Liquido na Venda de Titulos: Rendimentos Sujeitos a Tributacao Exclusiva"));
		taxPanel.add(new Label("Cupons de Titulos: Rendimentos Sujeitos a Tributacao Exclusiva"));
		taxPanel.add(new Label("Aluguel de Acoes: Rendimentos Sujeitos a Tributacao Exclusiva"));
		taxPanel.add(new Label("Juros Sobre Capital Proprio: Rendimentos Sujeitos a Tributacao Exclusiva"));
		taxPanel.add(new Label("Dividendos: Rendimentos Isentos e nao Tributaveis"));
		taxPanel.add(new Label("Ganho Liquido Isento: Rendimentos Isentos e nao Tributaveis"));
		taxPanel.add(debugPanel);
	}

	static YearSummary lookupYear(ArrayList<YearSummary> yearTable, int yearIndex) {
		// Grow table with null slots, if needed
		for (int j = yearTable.size(); j < yearIndex; ++j) {
			yearTable.add(j, null);
		}

		// Lookup year table
		YearSummary ySum = null;
            try { ySum = yearTable.get(yearIndex); } catch (Exception e) { }
		if (ySum == null) {
			ySum = new YearSummary();
			yearTable.add(yearIndex, ySum);
		}

		return ySum;
	}

	void updateTaxGrid() {

		debugPanel.clear();

		//
		// Scan operations
		//

		Date endDate = taxEndDate.getValue();
		ArrayList<Sales> monthlyTaxTable = new ArrayList<Sales>();
		ArrayList<YearSummary> yearTable = new ArrayList<YearSummary>();
		int firstSaleMonth = -1;
		int firstYear = -1;
		int rows = 0;

		for (int i = 0; i < operationList.size(); ++i) {
			Operation op = operationList.get(i);
			Date opDate = op.getDate();

			// Skip newer than endDate operations
			if (opDate.compareTo(endDate) > 0)
				break;

			// Compute yearly yields
			if (op instanceof Yield) {
				Yield y = (Yield) op;
				if ((y.yieldType != Yield.YIELD_JSCP) &&
					(y.yieldType != Yield.YIELD_DIVIDEND) &&
					(y.yieldType != Yield.YIELD_RENTAL) &&
					(y.yieldType != Yield.YIELD_RENTAL2) &&
					(y.yieldType != Yield.YIELD_COUPON))
					continue;

				int year = DateUtil.year(opDate);
				if (firstYear < 0) {
					firstYear = year;
				}
				int yearIndex = year - firstYear;

				YearSummary ySum = lookupYear(yearTable, yearIndex);

				if (y.yieldType == Yield.YIELD_JSCP) {
					ySum.ySumJscpCents      += y.yieldNetCents;
				}
				if (y.yieldType == Yield.YIELD_DIVIDEND) {
					ySum.ySumDividendsCents += y.yieldNetCents;
				}
				if (y.yieldType == Yield.YIELD_RENTAL) {
					ySum.ySumRentalCents += y.yieldNetCents;
				}
				if (y.yieldType == Yield.YIELD_RENTAL2) {
					ySum.ySumRental2Cents += y.yieldNetCents;
				}
				if (y.yieldType == Yield.YIELD_COUPON) {
					ySum.ySumCouponCents += y.yieldNetCents;
				}

				continue;
			}

			// Pick only sales and daytrades
			if (!(op instanceof Sell) && !(op instanceof DayTrade)) {
				continue;
			}

			// For sales, consider only stocks
			Sell s = null;
			if (op instanceof Sell) {
				s = (Sell) op;
				if (s.getAssetType() != Asset.ASSET_ACAO) {
					continue;
				}
			}

			// Find month
			int currMon = DateUtil.linearMonth(opDate);
			if (firstSaleMonth < 0) {
				firstSaleMonth = currMon;
			}
			int monIndex = currMon - firstSaleMonth;

			// Grow monthly table with null slots, if needed
			for (int j = monthlyTaxTable.size(); j <= monIndex; ++j) {
				monthlyTaxTable.add(j, null);
			}

			// Lookup month table
			Sales monthSales = monthlyTaxTable.get(monIndex);
			if (monthSales == null) {
				monthSales = new Sales();
				monthlyTaxTable.add(monIndex, monthSales);
				++rows;
			}

			if (op instanceof DayTrade) {
				DayTrade dt = (DayTrade) op;
				monthSales.dayTradeMonthlySalesGrossCents += dt.dtSellGrossValueCents;
				monthSales.dayTradeRetainedTaxCents += dt.dtRetainedTaxCents;
				continue;
			}

			monthSales.salesGrossValueCents += s.sellGrossValueCents;
			monthSales.retainedTaxCents += s.sellRetainedTaxCents;
		}

		//
		// Accumulate sales
		//

		AccumulateSales accumulateSalesCallback = new AccumulateSales(monthlyTaxTable, firstSaleMonth, debugPanel);

		// Re-scan operations list accumulating sales
		issueAssetTable(endDate, accumulateSalesCallback, accumulateSalesCallback);

		//
		// Show tax grid result
		//

		final int COLS = 17;
		taxGrid.clear();
		taxGrid.resize(rows + 1, COLS); // header

		taxGrid.getRowFormatter().addStyleName(0, "taxGridHeader");

		//
		// Header
		//
		taxGrid.setWidget(0, 0, new Label(Constant.MONTH));

		// Normal
		taxGrid.setWidget(0, 1, new Label("V.Brut."));
		taxGrid.setWidget(0, 2, new Label("Ganho/Perda"));
		taxGrid.setWidget(0, 3, new Label("Perda.Acum."));
		taxGrid.setWidget(0, 4, new Label("G/P.Liq."));
		taxGrid.setWidget(0, 5, new Label("LAIF"));
		taxGrid.setWidget(0, 6, new Label("IR.Total"));
		taxGrid.setWidget(0, 7, new Label("IRRF"));
		taxGrid.setWidget(0, 8, new Label("IR.Devido"));

		// Daytrade
		taxGrid.setWidget(0, 9, new Label("DT.V.Brut."));
		taxGrid.setWidget(0, 10, new Label("DT.Ganho/Perda"));
		taxGrid.setWidget(0, 11, new Label("DT.Perda.Acum."));
		taxGrid.setWidget(0, 12, new Label("DT.G/P.Liq."));
		taxGrid.setWidget(0, 13, new Label("DT.LAIF"));
		taxGrid.setWidget(0, 14, new Label("DT.IR.Total"));
		taxGrid.setWidget(0, 15, new Label("DT.IRRF"));
		taxGrid.setWidget(0, 16, new Label("DT.IR.Devido"));

		//
		// Scan months
		//

		int row = 0;
		long carryLossCents = CurrencyUtil.parseToCents(Preference.startTaxCarryLoss.getText());
		long dayTradeCarryLossCents = CurrencyUtil.parseToCents(Preference.startTaxDayTradeCarryLoss.getText());
		for (int i = 0; i < monthlyTaxTable.size(); ++i) {
			Sales monthSales = (Sales) monthlyTaxTable.get(i);
			if (monthSales == null)
				continue;

			++row;

			int mon = i + firstSaleMonth;
			String monStr = DateUtil.monthStr(mon);

			int year = DateUtil.monthToYear(mon);
			if (firstYear < 0) {
				firstYear = year;
			}
			int yearIndex = year - firstYear;
			YearSummary ySum = lookupYear(yearTable, yearIndex);

			//
			// First: Normal operations
			//

			long taxMonthGrossSalesCents = monthSales.salesGrossValueCents; // normal sales
			if (Preference.stockDayTradeAffectExemptionLimit.getValue()) {
				// add day-trade sales (only for tax exemption evaluation)
				taxMonthGrossSalesCents += monthSales.dayTradeMonthlySalesGrossCents;
			}

			long netProfitCents = monthSales.monthSalesNetProfitsCents;

			if (monthSales.monthSalesNetProfitsCents <= 0) {
				// There is loss

				// Just accumulate loss
				carryLossCents += monthSales.monthSalesNetProfitsCents;
			}
			else {
				// There is profit

				if (!Stock.stockSaleIsTaxExempt(taxMonthGrossSalesCents) ||
					Preference.stockExemptGainsReduceCarriedLoss.getValue()) {

					// There is NO tax exemption
					//  or
					// Exempt gains reduce accumulated loss

					// Subtract accumulated loss from profit
					netProfitCents += carryLossCents;
					// Compensate accumulated loss with current gains
					carryLossCents += monthSales.monthSalesNetProfitsCents;
					if (carryLossCents > 0)
						carryLossCents = 0;
				}

				if (Stock.stockSaleIsTaxExempt(taxMonthGrossSalesCents)) {
					// There IS tax exemption

					// Accumulate yearly tax-exempt gains
					ySum.ySumExemptProfitCents += monthSales.monthSalesNetProfitsCents;
				}

			}

			//
			// From last month of the year, accumulate yearly loss
			//
			ySum.ySumCarryLossCents = carryLossCents;

			//
			// Monthly due tax
			//
			double taxRate = Stock.stockTaxRate(taxMonthGrossSalesCents);
			long pretaxProfitCents = netProfitCents + monthSales.retainedTaxCents;
			long taxValueCents = -1;
			if (Preference.stockTaxRatioOverPretaxEarnings.getValue()) {
				// consider pretax earnings
				taxValueCents = Stock.getTaxValueCents(pretaxProfitCents, taxRate);
			}
			else {
				// consider after tax earnings
				taxValueCents = Stock.getTaxValueCents(netProfitCents, taxRate);
			}
			long dueTaxCents = taxValueCents - monthSales.retainedTaxCents;
			if (dueTaxCents < 0)
				dueTaxCents = 0;

			//
			// Second: Day-trade operations
			//
			
			long dtMonthlyNetResultCents = monthSales.dayTradeMonthlyNetResultCents;

			if (monthSales.dayTradeMonthlyNetResultCents <= 0) {
				// There is loss

				// Just accumulate loss
				dayTradeCarryLossCents += monthSales.dayTradeMonthlyNetResultCents;
			}
			else {
				// There is profit

				// Subtract accumulated loss from profit
				dtMonthlyNetResultCents += dayTradeCarryLossCents;

				// Compensate accumulated loss with current gains
				dayTradeCarryLossCents += monthSales.dayTradeMonthlyNetResultCents;
				if (dayTradeCarryLossCents > 0)
					dayTradeCarryLossCents = 0;
			}

			//
			// From last month of the year, accumulate yearly loss
			//
			ySum.ySumDayTradeCarryLossCents = dayTradeCarryLossCents;

			//
			// Monthly due tax
			//
			long dtPretaxResultCents = dtMonthlyNetResultCents + monthSales.dayTradeRetainedTaxCents;
			long dtTaxValueCents = -1;
			if (Preference.stockTaxRatioOverPretaxEarnings.getValue()) {
				// consider pretax earnings
				dtTaxValueCents = Math.round(Preference.stockDayTradeTaxFee * (double) dtPretaxResultCents);
			}
			else {
				// consider after tax earnings
				dtTaxValueCents = Math.round(Preference.stockDayTradeTaxFee * (double) dtMonthlyNetResultCents);
			}
			if (dtTaxValueCents < 0)
				dtTaxValueCents = 0;
			long dtDueTaxCents = dtTaxValueCents - monthSales.dayTradeRetainedTaxCents;
			if (dtDueTaxCents < 0)
				dtDueTaxCents = 0;

			//
			// Show
			//

			taxGrid.setWidget(row, 0, new Label(monStr));

			// Normal
			taxGrid.setWidget(row, 1, new Label(CurrencyUtil.format(monthSales.salesGrossValueCents)));
			taxGrid.setWidget(row, 2, new Label(CurrencyUtil.format(monthSales.monthSalesNetProfitsCents)));
			taxGrid.setWidget(row, 3, new Label(CurrencyUtil.format(carryLossCents)));
			taxGrid.setWidget(row, 4, new Label(CurrencyUtil.format(netProfitCents)));
			taxGrid.setWidget(row, 5, new Label(CurrencyUtil.format(pretaxProfitCents)));
			taxGrid.setWidget(row, 6, new Label(CurrencyUtil.format(taxValueCents)));
			taxGrid.setWidget(row, 7, new Label(CurrencyUtil.format(monthSales.retainedTaxCents)));
			taxGrid.setWidget(row, 8, new Label(CurrencyUtil.format(dueTaxCents)));

			// Day-trade
			taxGrid.setWidget(row, 9, new Label(CurrencyUtil.format(monthSales.dayTradeMonthlySalesGrossCents)));
			taxGrid.setWidget(row, 10, new Label(CurrencyUtil.format(monthSales.dayTradeMonthlyNetResultCents)));
			taxGrid.setWidget(row, 11, new Label(CurrencyUtil.format(dayTradeCarryLossCents)));
			taxGrid.setWidget(row, 12, new Label(CurrencyUtil.format(dtMonthlyNetResultCents)));
			taxGrid.setWidget(row, 13, new Label(CurrencyUtil.format(dtPretaxResultCents)));
			taxGrid.setWidget(row, 14, new Label(CurrencyUtil.format(dtTaxValueCents)));
			taxGrid.setWidget(row, 15, new Label(CurrencyUtil.format(monthSales.dayTradeRetainedTaxCents)));
			taxGrid.setWidget(row, 16, new Label(CurrencyUtil.format(dtDueTaxCents)));
		}

		//
		// Show yearly summary
		//

		final int YCOLS = 10;
		taxYearlySummaryGrid.clear();
		taxYearlySummaryGrid.resize(yearTable.size() + 1, YCOLS); // header

		taxYearlySummaryGrid.getRowFormatter().addStyleName(0, "taxGridHeader");

		// Header
		taxYearlySummaryGrid.setWidget(0, 0, new Label("Ano"));
		taxYearlySummaryGrid.setWidget(0, 1, new Label("Repasse.BTC"));
		taxYearlySummaryGrid.setWidget(0, 2, new Label("LLV.Titulos"));
		taxYearlySummaryGrid.setWidget(0, 3, new Label("Cupons"));
		taxYearlySummaryGrid.setWidget(0, 4, new Label("Aluguel"));
		taxYearlySummaryGrid.setWidget(0, 5, new Label("JSCP"));
		taxYearlySummaryGrid.setWidget(0, 6, new Label("Dividendos"));
		taxYearlySummaryGrid.setWidget(0, 7, new Label("Ganho.Liq.Isento"));
		taxYearlySummaryGrid.setWidget(0, 8, new Label("Perda.Acum."));
		taxYearlySummaryGrid.setWidget(0, 9, new Label("DT.Perda.Acum."));

		int yearRow = 0;
		for (int i = 0; i < yearTable.size(); ++i) {

			++yearRow;
			int year = i + firstYear;
			taxYearlySummaryGrid.setWidget(yearRow, 0, new Label(String.valueOf(year)));

			YearSummary yearSum = yearTable.get(i);
			if (yearSum == null)
				continue;

			taxYearlySummaryGrid.setWidget(yearRow, 1, new Label(CurrencyUtil.format(yearSum.ySumRental2Cents)));
			taxYearlySummaryGrid.setWidget(yearRow, 2, new Label(CurrencyUtil.format(yearSum.ySumBondSaleNetEarningsCents)));
			taxYearlySummaryGrid.setWidget(yearRow, 3, new Label(CurrencyUtil.format(yearSum.ySumCouponCents)));
			taxYearlySummaryGrid.setWidget(yearRow, 4, new Label(CurrencyUtil.format(yearSum.ySumRentalCents)));
			taxYearlySummaryGrid.setWidget(yearRow, 5, new Label(CurrencyUtil.format(yearSum.ySumJscpCents)));
			taxYearlySummaryGrid.setWidget(yearRow, 6, new Label(CurrencyUtil.format(yearSum.ySumDividendsCents)));
			taxYearlySummaryGrid.setWidget(yearRow, 7, new Label(CurrencyUtil.format(yearSum.ySumExemptProfitCents)));
			taxYearlySummaryGrid.setWidget(yearRow, 8, new Label(CurrencyUtil.format(yearSum.ySumCarryLossCents)));
			taxYearlySummaryGrid.setWidget(yearRow, 9, new Label(CurrencyUtil.format(yearSum.ySumDayTradeCarryLossCents)));
		}

		//debugPanel.add(new Label("yearTable.size(): " + yearTable.size()));
		//debugPanel.add(new Label("yearRow: " + yearRow));
	}

	void buildPortfolioPanel() {
		portfolioEndDatePanel.add(new Label("Data:"));
		portfolioEndDatePanel.add(portfolioEndDate);

		HorizontalPanel controlPanel = new HorizontalPanel();
		FetchQuotesButtonHandler fetchQuotesButtonHandler = new FetchQuotesButtonHandler();
		Button fetchQuotesButton = new Button("Buscar Cotacoes", fetchQuotesButtonHandler);
		controlPanel.add(portfolioHideSoldPositionsCheckBox);
		controlPanel.add(fetchQuotesButton);

		portfolioPanel.clear();
		portfolioPanel.add(portfolioEndDatePanel);
		portfolioPanel.add(controlPanel);
		portfolioPanel.add(portfolioTab);
		portfolioPanel.add(accountBalanceTab);
		portfolioPanel.add(portfolioDebugPanel);

		portfolioDebugPanel.clear();
	}

	void portfolioDebug(String msg) {
		portfolioDebugPanel.add(new Label(msg));
	}

	void feedAccountOracle(String accountName) {
		if (accountName == null)
			return;
		accountNameOracle.add(accountName);
	}

	Account findAccount(String accName) {
		Account acc = accountTable.get(accName);
		if (acc == null) {
			acc = new Account(accName);
			accountTable.put(accName, acc);
			feedAccountOracle(accName);
		}
		return acc;
	}

	Asset findAsset(String assetName, int assetType) {
		Asset a = assetTable.get(assetName);
		if (a == null) {
			a = Asset.newAsset(assetType, assetName);
			assetTable.put(assetName, a);
		}
		// FIXME: check assetType matches class of a
		return a;
	}

	void accountTableAdd(Buy b) {
		String accName = b.getAccount();
		if (accName == null)
			return;
		Account acc = findAccount(accName);
		acc.add(b);
	}

	void accountTableAdd(Sell s) {
		String accName = s.getAccount();
		if (accName == null)
			return;
		Account acc = findAccount(accName);
		acc.add(s);
	}

	void accountTableAdd(DayTrade dt) {
		String accName = dt.getAccount();
		if (accName == null)
			return;
		Account acc = findAccount(accName);
		acc.add(dt);
	}

	void accountTableAdd(Yield y) {
		String accName = y.getAccount();
		if (accName == null)
			return;
		Account acc = findAccount(accName);
		acc.add(y);
	}

	void accountTableAdd(Deposit d) {
		String accName = d.getAccount();
		Account acc = findAccount(accName);
		acc.add(d);
	}

	void accountTableAdd(Withdraw w) {
		String accName = w.getAccount();
		Account acc = findAccount(accName);
		acc.add(w);
	}

	void accountTableAdd(Fee f) {
		String accName = f.getAccount();
		Account acc = findAccount(accName);
		acc.add(f);
	}

	void accountTableAdd(Transfer t) {
		String accFromName = t.transferAccountFrom;
		Account accFrom = findAccount(accFromName);
		accFrom.addFrom(t);

		String accToName = t.transferAccountTo;
		Account accTo = findAccount(accToName);
		accTo.addTo(t);
	}

	void assetTableAdd(Buy b) {
		Asset a = findAsset(b.getAsset(), b.getAssetType());
		a.add(b);
	}

	Asset assetTableAdd(Sell s) {
		Asset a = findAsset(s.getAsset(), s.getAssetType());
		a.add(s);
		return a;
	}

	Asset assetTableAdd(DayTrade dt) {
		Asset a = findAsset(dt.getAsset(), dt.getAssetType());
		a.add(dt);
		return a;
	}

	void assetTableAdd(Yield y) {
		Asset a = findAsset(y.getAsset(), y.getAssetType());
		a.add(y);
	}

	void assetTableAdd(Split s) {
		Asset a = findAsset(s.getAsset(), s.getAssetType());
		a.add(s);
	}

	void issueAssetTable(Date endDate, SellAssetCallback onSellCall, DayTradeCallback onDayTradeCall) {

		// Reset asset/account tables
		assetTable = new HashMap<String,Asset>();
		accountTable = new HashMap<String,Account>();

		for (int i = 0; i < operationList.size(); ++i) {
			Operation op = operationList.get(i);
			if (op.getDate().compareTo(endDate) > 0)
				break;

			if (op instanceof Yield) {
				Yield y = (Yield) op;
				assetTableAdd(y);   // update Asset table
				accountTableAdd(y); // update Account table
				continue;
			}
			if (op instanceof Deposit) {
				Deposit d = (Deposit) op;
				accountTableAdd(d); 				// update Account table
				continue;
			}
			if (op instanceof Withdraw) {
				Withdraw w = (Withdraw) op;
				accountTableAdd(w); 				// update Account table
				continue;
			}
			if (op instanceof Fee) {
				Fee f = (Fee) op;
				accountTableAdd(f);				// update Account table
				continue;
			}
			if (op instanceof Transfer) {
				Transfer t = (Transfer) op;
				accountTableAdd(t);				// update Account table
				continue;
			}
			if (op instanceof Buy) {
				Buy b = (Buy) op;
				assetTableAdd(b);   // update Asset table
				accountTableAdd(b); // update Account table
				continue;
			}
			if (op instanceof Sell) {
				Sell s = (Sell) op;
				Asset a = assetTableAdd(s); // update Asset table
				accountTableAdd(s);         // update Account table
				if (onSellCall != null) {
					onSellCall.execute(a, s);
				}
				continue;
			}
			if (op instanceof DayTrade) {
				DayTrade dt = (DayTrade) op;
				Asset a = assetTableAdd(dt); // update Asset table
				accountTableAdd(dt);         // update Account table
				if (onDayTradeCall != null) {
					onDayTradeCall.execute(a, dt);	
				}
				continue;
			}
			if (op instanceof Split) {
				Split s = (Split) op;
				assetTableAdd(s);   // update Asset table
				continue;
			}
		}

	}

	void assetTableDropSoldPositions() {

		// Create temporary table
		HashMap<String,Asset> newAssetTable = new HashMap<String,Asset>();		

		for (Iterator<Asset> i = assetTable.values().iterator(); i.hasNext(); ) {
			Asset a = i.next();
			double currentAmount = a.getCurrentAmount();
			if (Preference.amountIsNonZero(currentAmount)) {
				newAssetTable.put(a.assetName, a);
			}
		}

		// Replace table
		assetTable = newAssetTable;
	}

	void updatePortfolioTable() {

		issueAssetTable(portfolioEndDate.getValue(), null, null); // Re-scan operations list

		if (portfolioHideSoldPositionsCheckBox.getValue()) {
			assetTableDropSoldPositions();
		}

		// update portfolioTab

		final int COL_BUY        = 5;
		final int COL_EXPENSE    = 6;
		final int COL_SALE       = 9;
		final int COL_YIELD      = 10;
		final int COL_REALIZABLE = 13;
		final int COL_RATIO      = 14;
		final int COLS           = 17;

		final int LAST_ROW         = assetTable.size() + 1;
		final int portfolioTabRows = assetTable.size() + 2;
		portfolioTab.resize(portfolioTabRows, COLS);
		portfolioTab.getRowFormatter().addStyleName(0, "tableHeader");

		// Header
		portfolioTab.setWidget(0, 0, new Label("Ativo"));
		portfolioTab.setWidget(0, 1, new Label("Compras"));
		portfolioTab.setWidget(0, 2, new Label("Bonus"));
		portfolioTab.setWidget(0, 3, new Label("Vendas"));
		portfolioTab.setWidget(0, 4, new Label("Posicao"));
		portfolioTab.setWidget(0, COL_BUY, new Label("Custos"));
		portfolioTab.setWidget(0, COL_EXPENSE, new Label("Remessas"));
		portfolioTab.setWidget(0, 7, new Label("C." + Constant.AVERAGE_O + " IR"));
		portfolioTab.setWidget(0, 8, new Label("C." + Constant.AVERAGE_O + " Real"));
		portfolioTab.setWidget(0, COL_SALE, new Label(Constant.REALIZATIONS));
		portfolioTab.setWidget(0, COL_YIELD, new Label("Proventos"));
		portfolioTab.setWidget(0, 11, new Label("Day-Trade"));
		portfolioTab.setWidget(0, 12, new Label(Constant.QUOTE));
		portfolioTab.setWidget(0, COL_REALIZABLE, new Label(Constant.REALIZABLE));
		portfolioTab.setWidget(0, COL_RATIO, new Label(Constant.RATIO));
		portfolioTab.setWidget(0, 15, new Label("Resultado"));
		portfolioTab.setWidget(0, 16, new Label("Retorno"));

		long sumBuyCents        = 0;
		long sumExpenseCents    = 0;
		long sumSaleCents       = 0;
		long sumYieldCents      = 0;
		long sumRealizableCents = 0;
		
		int row = 1;
		for (Iterator<Asset> i = assetTable.values().iterator(); i.hasNext(); ++row) {
			Asset a = i.next();

			sumBuyCents     += a.purchaseGrossCostCents;
			sumExpenseCents += a.purchaseExpenseCents;
			sumSaleCents    += a.salesNetRevenueCents;
			sumYieldCents   += a.yieldAfterTaxCents;

			double currentAmount = a.getCurrentAmount();
			//double acquiredAmount = a.getAcquiredAmount();
			long taxAverageBuyCost = Math.round(a.getTaxAverageAcquisitionCostCents());
			long realAverageBuyCost = Math.round(a.getRealAverageAcquisitionCost());

			String unitValue  = "?";
			String realizable = "?";
			String result     = "?";
			String roeStr     = "?";
			String style      = "negative";

			Quote assetQuote = findQuote(a.assetName, portfolioEndDate.getValue());
			if ((assetQuote != null) || (Preference.amountIsZero(currentAmount))) {

				// When amountIsZero, getRealizableCents yields 0
				// regardless of assetQuote

				long realizableCents = a.getRealizableCents(assetQuote);
				if (Preference.amountIsNonZero(currentAmount)) {
					sumRealizableCents += realizableCents;
				}

				// When amountIsZero, getResultCents ignores assetQuote

				long resultCents = a.getResultCents(assetQuote);
				if (resultCents > 0)
					style = "positive";
				long costCents = a.purchaseGrossCostCents + a.purchaseExpenseCents;
				if (costCents != 0) {
					double roe = (double) resultCents / (double) costCents;
					roeStr = Percent.format2(roe);
				}

				if (assetQuote != null) {
					unitValue  = Preference.unitaryQuoteFormat.format(assetQuote.getUnitValue());
				}
				realizable = CurrencyUtil.format(realizableCents);
				result     = CurrencyUtil.format(resultCents);
			}

			AssetTextBox unitQuoteBox = new AssetTextBox(a.assetName);
			unitQuoteBox.setText(unitValue);
			unitQuoteBox.addChangeHandler(unitQuoteHandler);

			Label resultLabel = new Label(result);
			Label roeLabel    = new Label(roeStr);
			resultLabel.setStyleName(style);
			roeLabel.setStyleName(style);

			portfolioTab.setWidget(row, 0, new Label(a.assetName));
			portfolioTab.setWidget(row, 1, new Label(Preference.amountFormat.format(a.amountBought)));
			portfolioTab.setWidget(row, 2, new Label(Preference.amountFormat.format(a.amountBonus)));
			portfolioTab.setWidget(row, 3, new Label(Preference.amountFormat.format(a.amountSold)));
			portfolioTab.setWidget(row, 4, new Label(Preference.amountFormat.format(currentAmount)));
			portfolioTab.setWidget(row, COL_BUY, new Label(CurrencyUtil.format(a.purchaseGrossCostCents)));
			portfolioTab.setWidget(row, COL_EXPENSE, new Label(CurrencyUtil.format(a.purchaseExpenseCents)));
			portfolioTab.setWidget(row, 7, new Label(CurrencyUtil.format(taxAverageBuyCost)));
			portfolioTab.setWidget(row, 8, new Label(CurrencyUtil.format(realAverageBuyCost)));
			portfolioTab.setWidget(row, COL_SALE, new Label(CurrencyUtil.format(a.salesNetRevenueCents)));
			portfolioTab.setWidget(row, COL_YIELD, new Label(CurrencyUtil.format(a.yieldAfterTaxCents)));
			portfolioTab.setWidget(row, 11, new Label(CurrencyUtil.format(a.dayTradeNetResultCents)));
			portfolioTab.setWidget(row, 12, unitQuoteBox);
			portfolioTab.setWidget(row, COL_REALIZABLE, new Label(realizable));
			// room COL_RATIO=14 is assigned below with asset %
			portfolioTab.setWidget(row, 15, resultLabel);
			portfolioTab.setWidget(row, 16, roeLabel);

			for (int c = 1; c < COLS; ++c) {
				portfolioTab.getCellFormatter().addStyleName(row, c, "portfolioNumericColumn");
			}
		}

		//
		// Fill in last row
		//

		for (int c = 0; c < COLS; ++c) {
			portfolioTab.clearCell(LAST_ROW, c);
			portfolioTab.getCellFormatter().addStyleName(LAST_ROW, c, "portfolioNumericColumn");
		}
		portfolioTab.setWidget(LAST_ROW, COL_BUY, new Label(CurrencyUtil.format(sumBuyCents)));
		portfolioTab.setWidget(LAST_ROW, COL_EXPENSE, new Label(CurrencyUtil.format(sumExpenseCents)));
		portfolioTab.setWidget(LAST_ROW, COL_SALE, new Label(CurrencyUtil.format(sumSaleCents)));
		portfolioTab.setWidget(LAST_ROW, COL_YIELD, new Label(CurrencyUtil.format(sumYieldCents)));
		portfolioTab.setWidget(LAST_ROW, COL_REALIZABLE, new Label(CurrencyUtil.format(sumRealizableCents)));

		//
		// Fill in ratio column
		//
		
		row = 1;
		for (Iterator<Asset> i = assetTable.values().iterator(); i.hasNext(); ++row) {
			Asset a = i.next();

			double currentAmount = a.getCurrentAmount();
			if (Preference.amountIsZero(currentAmount)) {
				portfolioTab.clearCell(row, COL_RATIO);
				continue;
			}

			Quote assetQuote = findQuote(a.assetName, portfolioEndDate.getValue());
			if (assetQuote == null) {
				portfolioTab.clearCell(row, COL_RATIO);
				continue;
			}

			long realizableCents = a.getRealizableCents(assetQuote);

			double assetFraction = ((double) realizableCents) / sumRealizableCents;
			
			portfolioTab.setWidget(row, COL_RATIO, new Label(Percent.format2(assetFraction)));
		}

		//
		// Build account balance summary
		//

		final int COLUMNS = 11;
		accountBalanceTab.resize(accountTable.size() + 1, COLUMNS);
		accountBalanceTab.getRowFormatter().addStyleName(0, "tableHeader");

		// Header
		accountBalanceTab.setWidget(0, 0, new Label("Conta"));
		accountBalanceTab.setWidget(0, 1, new Label(Constant.DEPOSITS));
		accountBalanceTab.setWidget(0, 2, new Label("Proventos"));
		accountBalanceTab.setWidget(0, 3, new Label("Vendas"));
		accountBalanceTab.setWidget(0, 4, new Label("Saques"));
		accountBalanceTab.setWidget(0, 5, new Label("T.Origem"));
		accountBalanceTab.setWidget(0, 6, new Label("T.Destino"));
		accountBalanceTab.setWidget(0, 7, new Label("Taxas"));
		accountBalanceTab.setWidget(0, 8, new Label("Compras"));
		accountBalanceTab.setWidget(0, 9, new Label("Day-Trade"));
		accountBalanceTab.setWidget(0, 10, new Label("Saldo"));

		row = 1;
		for (Account a: accountTable.values()) {

			for (int c = 1; c < COLUMNS; ++c) {
				accountBalanceTab.getCellFormatter().addStyleName(row, c, "portfolioNumericColumn");
			}

			accountBalanceTab.setWidget(row, 0, new Label(a.acctName));
			accountBalanceTab.setWidget(row, 1, new Label(CurrencyUtil.format(a.depositsCents)));
			accountBalanceTab.setWidget(row, 2, new Label(CurrencyUtil.format(a.yieldsCents)));
			accountBalanceTab.setWidget(row, 3, new Label(CurrencyUtil.format(a.salesCents)));
			accountBalanceTab.setWidget(row, 4, new Label(CurrencyUtil.format(a.withdrawalsCents)));
			accountBalanceTab.setWidget(row, 5, new Label(CurrencyUtil.format(a.transferFromCents)));
			accountBalanceTab.setWidget(row, 6, new Label(CurrencyUtil.format(a.transferToCents)));
			accountBalanceTab.setWidget(row, 7, new Label(CurrencyUtil.format(a.brokerTaxesCents)));
			accountBalanceTab.setWidget(row, 8, new Label(CurrencyUtil.format(a.buysCents)));
			accountBalanceTab.setWidget(row, 9, new Label(CurrencyUtil.format(a.dayTradeCents)));
			accountBalanceTab.setWidget(row, 10, new Label(CurrencyUtil.format(a.getBalanceCents())));

			++row;
		}
	}

	boolean tradeFilterMiss(Operation op, String year, String asset, String account) {
		if (!(op instanceof Trade))
			return true;
		
		if (!year.isEmpty()) {
			if (year.compareTo(DateTimeFormat.getFormat("y").format(op.getDate())) != 0)
				return true;
		}

		if (!asset.isEmpty()) {
			Trade tr = (Trade) op;
			if (asset.compareTo(tr.getAsset()) != 0)
				return true;
		}

		if (!account.isEmpty()) {
			if (account.compareTo(op.getAccount()) != 0)
				return true;
		}

		return false;
	}

	int countTradeListDisplaySize() {
		int size = 0;

		String year = tradeFilterYear.getText();
		String asset = tradeFilterAssetSuggest.getText();
		String account = tradeFilterAccountSuggest.getText();

		for (int i = 0; i < operationList.size(); ++i) {
			Operation op = operationList.get(i);

			if (tradeFilterMiss(op, year, asset, account))
				continue;

			++size;
		}

		return size;
	}

	void buildOpTab() {
		int cols = 12;
		int tradeCount = countTradeListDisplaySize();
		opTab.resize(tradeCount + 1, cols + 1);
		opTab.getRowFormatter().addStyleName(0, "tableHeader");

		final int COL_SELL_GROSS   = 6;
		final int COL_RETAINED_TAX = 9;

		// Header
		opTab.setWidget(0, 0, new Label("Id"));
		opTab.setWidget(0, 1, new Label("Data"));
		opTab.setWidget(0, 2, new Label("Op."));
		opTab.setWidget(0, 3, new Label("Tipo"));
		opTab.setWidget(0, 4, new Label("Ativo"));
		opTab.setWidget(0, 5, new Label("Quant."));
		opTab.setWidget(0, COL_SELL_GROSS, new Label("G.Bruto"));
		opTab.setWidget(0, 7, new Label("C.Bruto/G.Liq."));
		opTab.setWidget(0, 8, new Label("Remessa"));
		opTab.setWidget(0, COL_RETAINED_TAX, new Label("IRRF"));
		opTab.setWidget(0, 10, new Label("Conta"));
		opTab.setWidget(0, 11, new Label(Constant.AVERAGE_O));
		opTab.setWidget(0, 12, new Label("Excluir"));

		String year = tradeFilterYear.getText();
		String asset = tradeFilterAssetSuggest.getText();
		String account = tradeFilterAccountSuggest.getText();
		
		for (int i = 0, row = tradeCount + 1; i < operationList.size(); ++i) {
			Operation op = operationList.get(i);

			if (tradeFilterMiss(op, year, asset, account))
				continue;

			// Build a text box for sellGrossValue ?
			SellGrossValueBox grossBox = null;
			SellRetainedTaxBox retainedTaxBox = null;
			if (op instanceof Sell) {
				Sell s = (Sell) op;
				if (s.getAssetType() == Asset.ASSET_ACAO) {
					// Build a text box for sellGrossValue 
					grossBox = new SellGrossValueBox(s);
					grossBox.setText(CurrencyUtil.format(s.sellGrossValueCents));
					grossBox.addChangeHandler(sellGrossValueHandler);

					// Build a text box for sellRetainedTax 
					retainedTaxBox = new SellRetainedTaxBox(s);
					retainedTaxBox.setText(CurrencyUtil.format(s.sellRetainedTaxCents));
					retainedTaxBox.addChangeHandler(sellRetainedTaxHandler);
				}
			}

			--row;
			for (int j = 0; j < cols; ++j) {
				if ((grossBox != null) && (j == COL_SELL_GROSS)) {
					opTab.setWidget(row, j, grossBox);
					continue;
				}
				if ((retainedTaxBox != null) && (j == COL_RETAINED_TAX)) {
					opTab.setWidget(row, j, retainedTaxBox);
					continue;
				}

				// default: get from operation
				opTab.setWidget(row, j, op.getCol(j));
			}
			ClickHandler del = new DelOpButtonHandler();
			CarrierButton delButton = new CarrierButton("X", del, op);
			opTab.setWidget(row, cols, delButton);
		}
	}
	
	void buildAddAccountPanel() {
	
		addAccountPanel.clear();

		addAccountPanel.add(accountActionDropBox);

		// Create a DateBox
		addAccountDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		addAccountDate.setValue(new Date());
		addAccountPanel.add(addAccountDate);

		int accountAction = accountActionDropBox.getSelectedIndex();
		switch (accountAction) {		
		case Operation.ACC_TRANSFER:
			VerticalPanel nameFromPanel = new VerticalPanel();
			nameFromPanel.add(new Label("Conta Origem"));
			nameFromPanel.add(addAccountFromName);
			addAccountPanel.add(nameFromPanel);

			VerticalPanel nameToPanel = new VerticalPanel();
			nameToPanel.add(new Label("Conta Destino"));
			nameToPanel.add(addAccountToName);
			addAccountPanel.add(nameToPanel);
		break;
		default:
			VerticalPanel namePanel = new VerticalPanel();
			namePanel.add(new Label("Conta"));
			namePanel.add(addAccountName);
			addAccountPanel.add(namePanel);
		}

		VerticalPanel valuePanel = new VerticalPanel();
		valuePanel.add(new Label("Valor"));
		valuePanel.add(addAccountValue);
		addAccountPanel.add(valuePanel);

		switch (accountAction) {		
		case Operation.ACC_DEPOSIT:
			VerticalPanel expensePanel = new VerticalPanel();
			expensePanel.add(new Label("Remessa"));
			expensePanel.add(addAccountExpense);
			addAccountPanel.add(expensePanel);
		break;
		default:
		}
	
		addAccountPanel.add(addAccountButton);
	}
	
	void buildAccPanel() {
		accountPanel.clear();

		accountPanel.add(addAccountPanel);
		accountPanel.add(new Label("filtros conta ano"));
		accountPanel.add(new Label("operacoes=" + operationList.size() + " negocios=" + operationListTradeCount + " contas=" + operationListAccountCount));
		accountPanel.add(accTab);
	}
	
	void buildAccTab() {
		int cols = 6;
		accTab.resize(operationListAccountCount + 1, cols + 1);
		accTab.getRowFormatter().addStyleName(0, "tableHeader");

		final int COL_VALUE   = 4;
		final int COL_EXPENSE = 5;

		// Header
		accTab.setWidget(0, 0, new Label("Id"));
		accTab.setWidget(0, 1, new Label("Data"));
		accTab.setWidget(0, 2, new Label("Conta"));
		accTab.setWidget(0, 3, new Label("Op."));
		accTab.setWidget(0, COL_VALUE, new Label("Valor"));
		accTab.setWidget(0, COL_EXPENSE, new Label("Remessa"));
		accTab.setWidget(0, 6, new Label("Excluir"));
		
		for (int i = 0, row = operationListAccountCount + 1; i < operationList.size(); ++i) {
			Operation op = operationList.get(i);
			if (!((op instanceof Deposit) || (op instanceof Withdraw) || (op instanceof Fee) || (op instanceof Transfer)))
				continue;
			--row;

			// Create edit boxes for Deposit ?
			EditDepositValueBox editDepositValueBox = null;
			EditDepositExpenseBox editDepositExpenseBox = null;
			if (op instanceof Deposit) {
				Deposit dep = (Deposit) op;
				editDepositValueBox = new EditDepositValueBox(dep, editDepositBoxHandler);
				editDepositExpenseBox = new EditDepositExpenseBox(dep, editDepositBoxHandler);
			}

			for (int j = 0; j < cols; ++j) {

				if ((editDepositValueBox != null) && (j == COL_VALUE)) {
					accTab.setWidget(row, j, editDepositValueBox);
					continue;
				}
				if ((editDepositExpenseBox != null) && (j == COL_EXPENSE)) {
					accTab.setWidget(row, j, editDepositExpenseBox);
					continue;
				}

				// default: get from operation
				accTab.setWidget(row, j, op.getCol(j));
			}
			ClickHandler del = new DelOpButtonHandler();
			CarrierButton delButton = new CarrierButton("X", del, op);
			accTab.setWidget(row, cols, delButton);
		}
	}

	void buildAddYieldAccountDropBox() {
		yieldAccountDropBox.clear();

		for (Object element : accountList)
			yieldAccountDropBox.addItem((String) element);
	}

	void buildAddYieldPanel() {
		addYieldPanel.clear();

		// Create a DateBox
		Date today = new Date();
		addYieldDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		addYieldDate.setValue(today);

		// Create tax year text box with today's year
		//VerticalPanel addYieldTaxYearPanel = new VerticalPanel();
		//addYieldTaxYearPanel.add(new Label("Ano IR"));
		//addYieldTaxYearPanel.add(addYieldTaxYear);
		//addYieldTaxYear.setText(DateTimeFormat.getFormat("y").format(today));

		VerticalPanel addYieldAssetPanel = new VerticalPanel();
		addYieldAssetPanel.add(new Label("Ativo"));
		addYieldAssetPanel.add(addYieldAsset);

		VerticalPanel addYieldAmountPanel = new VerticalPanel();
		addYieldAmountPanel.add(new Label("Quant."));
		addYieldAmountPanel.add(addYieldAmount);

		VerticalPanel addYieldGrossValuePanel = new VerticalPanel();
		addYieldGrossValuePanel.add(new Label("Valor Bruto"));
		addYieldGrossValuePanel.add(addYieldGrossValue);

		VerticalPanel addYieldNetValuePanel = new VerticalPanel();
		addYieldNetValuePanel.add(new Label("Valor " + Constant.NET_O));
		addYieldNetValuePanel.add(addYieldNetValue);

		buildAddYieldAccountDropBox();
		VerticalPanel addYieldAccountPanel = new VerticalPanel();
		addYieldAccountPanel.add(new Label("Conta"));
		addYieldAccountPanel.add(yieldAccountDropBox);

		addYieldPanel.add(yieldTypeDropBox);
		addYieldPanel.add(addYieldDate);
		//addYieldPanel.add(addYieldTaxYearPanel);
		addYieldPanel.add(addYieldAssetPanel);
		addYieldPanel.add(addYieldAmountPanel);
		addYieldPanel.add(addYieldGrossValuePanel);
		addYieldPanel.add(addYieldNetValuePanel);
		addYieldPanel.add(addYieldAccountPanel);
		addYieldPanel.add(addYieldButton);
	}

	void buildYieldTable() {
		int cols = 8;
		yieldTable.resize(operationListYieldCount + 1, cols + 1);
		yieldTable.getRowFormatter().addStyleName(0, "tableHeader"); 

		// Header
		yieldTable.setWidget(0, 0, new Label("Id"));
		yieldTable.setWidget(0, 1, new Label("Provento"));
		yieldTable.setWidget(0, 2, new Label("Data"));
		//yieldTable.setWidget(0, 3, new Label("Ano IR"));
		yieldTable.setWidget(0, 3, new Label("Ativo"));
		yieldTable.setWidget(0, 4, new Label("Quant."));
		yieldTable.setWidget(0, 5, new Label("Valor Bruto"));
		yieldTable.setWidget(0, 6, new Label("Valor " + Constant.NET_O));
		yieldTable.setWidget(0, 7, new Label("Conta"));
		yieldTable.setWidget(0, 8, new Label("Excluir"));
		
		for (int i = 0, row = operationListYieldCount + 1; i < operationList.size(); ++i) {
			Operation op = operationList.get(i);
			if (!(op instanceof Yield))
				continue;
			--row;
			for (int j = 0; j < cols; ++j) {
				yieldTable.setWidget(row, j, op.getCol(j));
			}
			ClickHandler del = new DelOpButtonHandler();
			CarrierButton delButton = new CarrierButton("X", del, op);
			yieldTable.setWidget(row, cols, delButton);
		}
	}

	void buildYieldPanel() {
		yieldPanel.clear();
		yieldPanel.add(addYieldPanel);
		yieldPanel.add(yieldTable);
	}

	void buildAddSplitPanel() {
		addSplitPanel.clear();

		// Create a DateBox
		Date today = new Date();
		addSplitDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		addSplitDate.setValue(today);

		VerticalPanel addSplitAssetPanel = new VerticalPanel();
		addSplitAssetPanel.add(new Label("Ativo"));
		addSplitAssetPanel.add(addSplitAssetSuggest);

		VerticalPanel addSplitFromPanel = new VerticalPanel();
		addSplitFromPanel.add(new Label("De"));
		addSplitFromPanel.add(addSplitFrom);

		VerticalPanel addSplitToPanel = new VerticalPanel();
		addSplitToPanel.add(new Label("Para"));
		addSplitToPanel.add(addSplitTo);

		addSplitPanel.add(addSplitDate);
		addSplitPanel.add(addSplitAssetPanel);
		addSplitPanel.add(addSplitFromPanel);
		addSplitPanel.add(addSplitToPanel);
		addSplitPanel.add(addSplitButton);
	}

	void buildSplitGrid() {
		int cols = 4;
		splitGrid.resize(operationListSplitCount + 1, cols + 1);
		splitGrid.getRowFormatter().addStyleName(0, "tableHeader");

		// Header
		splitGrid.setWidget(0, 0, new Label("Data"));
		splitGrid.setWidget(0, 1, new Label("Ativo"));
		splitGrid.setWidget(0, 2, new Label("De"));
		splitGrid.setWidget(0, 3, new Label("Para"));
		splitGrid.setWidget(0, 4, new Label("Excluir"));

		for (int i = 0, row = operationListSplitCount + 1; i < operationList.size(); ++i) {
			Operation op = operationList.get(i);
			if (!(op instanceof Split))
				continue;
			--row;
			for (int j = 0; j < cols; ++j) {
				splitGrid.setWidget(row, j, op.getCol(j));
			}
			ClickHandler del = new DelOpButtonHandler();
			CarrierButton delButton = new CarrierButton("X", del, op);
			splitGrid.setWidget(row, cols, delButton);
		}
	}

	void buildSplitPanel() {
		splitPanel.clear();
		splitPanel.add(addSplitPanel);
		splitPanel.add(splitGrid);
	}

	void buildAddQuoteAssetDropBox() {
		quoteAssetDropBox.clear();

		HashMap<String,String> assetList = new HashMap<String,String>();

		// De-duplicate asset names
		for (Object obj : operationList) {
			if (obj instanceof HasAsset) {
				HasAsset ha = (HasAsset) obj;
				assetList.put(ha.getAsset(), ha.getAsset());
			}
		}

		for (Object obj : assetList.values()) {
			String asset = (String) obj;
			quoteAssetDropBox.addItem(asset);
		}
	}

	void buildAddQuotePanel() {
		addQuotePanel.clear();

		// Create a DateBox
		Date today = new Date();
		addQuoteDate.setFormat(new DateBox.DefaultFormat(Preference.dateFormat));
		addQuoteDate.setValue(today);

		buildAddQuoteAssetDropBox();
		VerticalPanel addQuoteAssetPanel = new VerticalPanel();
		addQuoteAssetPanel.add(new Label("Ativo"));
		addQuoteAssetPanel.add(quoteAssetDropBox);

		VerticalPanel addQuoteAmountPanel = new VerticalPanel();
		addQuoteAmountPanel.add(new Label("Quant."));
		addQuoteAmountPanel.add(addQuoteAmount);

		VerticalPanel addQuoteValuePanel = new VerticalPanel();
		addQuoteValuePanel.add(new Label(Constant.QUOTE));
		addQuoteValuePanel.add(addQuoteValue);

		addQuotePanel.add(addQuoteDate);
		addQuotePanel.add(addQuoteAssetPanel);
		addQuotePanel.add(addQuoteAmountPanel);
		addQuotePanel.add(addQuoteValuePanel);
		addQuotePanel.add(addQuoteButton);
	}

	void buildQuoteGrid() {
		int cols = 5;
		quoteGrid.resize(quoteTable.size() + 1, cols + 1);
		quoteGrid.getRowFormatter().addStyleName(0, "tableHeader");

		// Header
		quoteGrid.setWidget(0, 0, new Label("Data"));
		quoteGrid.setWidget(0, 1, new Label("Ativo"));
		quoteGrid.setWidget(0, 2, new Label("Quant."));
		quoteGrid.setWidget(0, 3, new Label(Constant.QUOTE));
		quoteGrid.setWidget(0, 4, new Label("Unidade"));
		quoteGrid.setWidget(0, 5, new Label("Excluir"));

		int row = quoteTable.size() + 1;		
		for (Object obj : quoteTable.values()) {
			Quote q = (Quote) obj;
			--row;
			for (int j = 0; j < cols; ++j) {
				quoteGrid.setWidget(row, j, q.getCol(j));
			}
			ClickHandler del = new DelQuoteButtonHandler();
			CarrierButton delButton = new CarrierButton("X", del, q);
			quoteGrid.setWidget(row, cols, delButton);

		}
	}

	void buildQuotePanel() {
		HorizontalPanel quoteClearPanel = new HorizontalPanel();
		quoteClearPanel.add(clearTodayQuoteButton);
		quoteClearPanel.add(clearPastQuoteButton);

		quotePanel.clear();
		quotePanel.add(addQuotePanel);
		quotePanel.add(quoteClearPanel);
		quotePanel.add(quoteGrid);
	}
	
	void updatePrefPanel() {		
		prefSellSpreadText.setText(Percent.format2(Preference.sellSpread, false));
		prefBrokerFeeText.setText(CurrencyUtil.format(Preference.brokerFeeCents));
		prefTaxExemptionText.setText(CurrencyUtil.format(Preference.stockTaxExemptionCents));
		prefTaxFeeText.setText(Percent.format2(Preference.stockTaxFee, false));
		prefDayTradeTaxFeeText.setText(Percent.format2(Preference.stockDayTradeTaxFee, false));
	}
	
	void buildPrefPanel() {
		updatePrefPanel();
		
		HorizontalPanel prefSellSpreadPanel     = new HorizontalPanel();
		HorizontalPanel prefBrokerFeePanel      = new HorizontalPanel();
		HorizontalPanel prefTaxExemptionPanel   = new HorizontalPanel();
		HorizontalPanel prefTaxFeePanel         = new HorizontalPanel();
		HorizontalPanel prefDayTradeTaxFeePanel = new HorizontalPanel();
		
		prefSellSpreadPanel.add(new Label("Spread de venda"));
		prefBrokerFeePanel.add(new Label("Corretagem fixa"));
		prefTaxExemptionPanel.add(new Label("Limite de isencao"));
		prefTaxFeePanel.add(new Label("Taxa de IR sobre lucro"));
		prefDayTradeTaxFeePanel.add(new Label("Taxa de IR sobre lucro day-trade"));
		
		prefSellSpreadPanel.add(prefSellSpreadText);
		prefBrokerFeePanel.add(prefBrokerFeeText);
		prefTaxExemptionPanel.add(prefTaxExemptionText);
		prefTaxFeePanel.add(prefTaxFeeText);
		prefDayTradeTaxFeePanel.add(prefDayTradeTaxFeeText);
		
		prefSellSpreadPanel.add(new Label("%"));
		prefTaxFeePanel.add(new Label("%"));
		prefDayTradeTaxFeePanel.add(new Label("%"));

		//prefPanel.add(new Label("Estimativa de Valor Realizavel para Venda de Acoes no Mercado a Vista"));
		prefPanel.add(prefSellSpreadPanel);
		prefPanel.add(prefBrokerFeePanel);
		prefPanel.add(prefTaxExemptionPanel);
		prefPanel.add(prefTaxFeePanel);
		prefPanel.add(prefDayTradeTaxFeePanel);
	}

	void build() {

		for (int i = 0; i < Asset.assetTypeName.length; ++i)
			assetTypeDropBox.addItem(Asset.assetTypeName[i]);
		assetTypeDropBox.setSelectedIndex(addAssetPanelCurrType);
		assetTypeDropBox.addChangeHandler(assetTypeHandler);

		for (int i = 0; i < Operation.operationName.length; ++i)
			operationDropBox.addItem(Operation.operationName[i]);
		operationDropBox.setSelectedIndex(addAssetPanelCurrTrade);
		operationDropBox.addChangeHandler(assetTradeHandler);
		
		for (int i = 0; i < Operation.accountAction.length; ++i)
			accountActionDropBox.addItem(Operation.accountAction[i]);
		accountActionDropBox.addChangeHandler(accountActionHandler);

		for (int i = 0; i < Yield.yieldTypeName.length; ++i)
			yieldTypeDropBox.addItem(Yield.yieldTypeName[i]);

		buildAddAssetPanel();

		buildTradeFilterPanel();
		buildOpTab();
		buildOpPanel();	

		buildPortfolioPanel();
		
		buildAddAccountPanel();
		buildAccTab();
		buildAccPanel();

		buildAddYieldPanel();
		buildYieldTable();
		buildYieldPanel();

		buildAddSplitPanel();
		buildSplitGrid();
		buildSplitPanel();

		buildAddQuotePanel();
		buildQuoteGrid();
		buildQuotePanel();
		buildSummaryPanel();
		buildEvolutionPanel();
		buildTaxPanel();
		buildPrefPanel();

		// Build DB panel
		dbText.setCharacterWidth(60);
		dbText.setVisibleLines(20);
		dbText.addChangeHandler(dbTextHandler);
		dbPanel.add(new Label("Copie o texto XML abaixo e guarde para salvar os dados. Cole abaixo um texto XML previamente salvo para restaurar os dados."));
		dbPanel.add(dbText);
		dbPanel.add(dbLogConsole);
		updateXmlDb();

		// Create a tab panel
		//tabPanel.setWidth("400px");
		//tabPanel.setAnimationEnabled(true);

		for (int i = 0; i < mainTabList.length; ++i)
			tabPanel.add(mainTabList[i].getWidget(), mainTabList[i].getText());

		// Assemble Main panel
		mainPanel.add(tabPanel);
	}

	int findOlderThan(Operation op) {
		Date opDate = op.getDate();
		for (int i = 0; i < operationList.size(); ++i) {
			Operation curr = (Operation) operationList.get(i);
			if (opDate.compareTo(curr.getDate()) < 0)
				return i;
		}
		return -1;
	}

	void updateXmlDb() {
		Document doc = XMLParser.createDocument();
		Element parent = doc.getDocumentElement();

		if (parent == null) {
			parent = doc.createElement("database");
			doc.appendChild(parent);
		}

		// Add preferences to DOM
		Preference.appendChildNode(doc, parent);

		// Add quotes to DOM
		for (Object element : quoteTable.values()) {
			Quote quote = (Quote) element;
			quote.appendChildNode(doc, parent);
		}

		// Add operations to DOM
		for (int i = 0; i < operationList.size(); ++i) {
			Operation op = (Operation) operationList.get(i);
			op.appendChildNode(doc, parent);
		}

		// Convert DOM to XML
		currXmlTextDb = Constant.XML_HEADER + doc.toString();

		// Display XML
		dbText.setText(currXmlTextDb);
	}

	void clearOperationList() {
		operationList.clear();
		operationListTradeCount = 0;
		operationListAccountCount = 0;
		operationListYieldCount = 0;
		operationListSplitCount = 0;
	}

	void addOperation(Operation op) {
		String accountName = op.getAccount();

		if (op instanceof HasAsset) {
			HasAsset ha = (HasAsset) op;
			assetNameOracle.add(ha.getAsset());
		}
		if (op instanceof Trade) {
			feedAccountOracle(accountName);
			++operationListTradeCount;
		}
		if ((op instanceof Deposit) || (op instanceof Withdraw) || (op instanceof Fee)) {
			feedAccountOracle(accountName);
			accountList.add(accountName);
			++operationListAccountCount;
		}
		if (op instanceof Transfer) {
			Transfer t = (Transfer) op;
			feedAccountOracle(t.transferAccountFrom);
			feedAccountOracle(t.transferAccountTo);
			accountList.add(t.transferAccountFrom);
			accountList.add(t.transferAccountTo);
			++operationListAccountCount;
		}
		if (op instanceof Yield) {
			feedAccountOracle(accountName);
			++operationListYieldCount;
		}
		if (op instanceof Split) {
			++operationListSplitCount;
		}
		
		int olderIndex = findOlderThan(op);
		if (olderIndex < 0) {
			// append at end
			operationList.add(op);
		}
		else {
			// ordered insertion
			operationList.add(olderIndex, op);
		}
	}
	
	void delOperation(Operation op) {
		int index = operationList.indexOf(op);
		operationList.remove(index);

		if (op instanceof Trade) {		
			--operationListTradeCount;
			buildOpTab();
			buildOpPanel();
			buildAddQuoteAssetDropBox();

			if (op instanceof Sell) {
				updateTaxGrid();
			}
		}
		if ((op instanceof Deposit) || (op instanceof Withdraw) || (op instanceof Fee) || (op instanceof Transfer)) {
			--operationListAccountCount;
			buildAccTab();
			buildAccPanel();
			buildAddAssetAccountDropBox();
			buildAddYieldAccountDropBox();
		}
		if (op instanceof Yield) {
			--operationListYieldCount;
			buildYieldTable();
			buildAddQuoteAssetDropBox();
			updateTaxGrid();
		}
		if (op instanceof Split) {
			--operationListSplitCount;
			buildSplitGrid();
			//updateTaxGrid();
		}
	}

	void delQuote(Quote q) {
		quoteTable.remove(q.getKey());
		buildQuoteGrid();
	}

	void delQuoteFor(Date day) {
		HashMap<String,Quote> tmpQuoteTable = new HashMap<String,Quote>();

		for (Object obj : quoteTable.values()) {
			Quote q = (Quote) obj;
			if (day.compareTo(q.getDate()) != 0) {
				tmpQuoteTable.put(q.getKey(), q);
			}
		}

		quoteTable = tmpQuoteTable; // replace quote table
		updateXmlDb();
		buildQuoteGrid();
	}

	void delQuoteBefore(Date day) {
		HashMap<String,Quote> tmpQuoteTable = new HashMap<String,Quote>();

		for (Object obj : quoteTable.values()) {
			Quote q = (Quote) obj;
			if (day.compareTo(q.getDate()) <= 0) {
				tmpQuoteTable.put(q.getKey(), q);
			}
		}

		quoteTable = tmpQuoteTable; // replace quote table
		updateXmlDb();
		buildQuoteGrid();
	}

	void clearQuoteList() {
		quoteTable = new HashMap<String,Quote>(); // replace quote table
	}

	void delAllQuotes() {
		clearQuoteList();
		updateXmlDb();
		buildQuoteGrid();
	}

	Quote findQuote(String assetName, Date date) {
		return quoteTable.get(Quote.makeKey(assetName, date));
	}

	void addQuote(Quote newQuote) {
		quoteTable.put(newQuote.getKey(), newQuote);
	}

	void addQuote(Element elem) {
		Quote newQuote = new Quote(elem);
		addQuote(newQuote);
	}

	void dbLog(String msg) {
		dbLogConsole.add(new Label(msg));
	}

	void loadDb(NodeList nodeList) {

		dbLogConsole.clear();

		dbLog("Clearing quote list");
		clearQuoteList();
		
		dbLog("Clearing operation list");
		clearOperationList();		

		dbLog("Parsing XML DB...");

		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (!(node instanceof Element))
				continue;
			Element elem = (Element) node;
			String tag = elem.getTagName();

			if (tag.equals(Constant.XML_TAG_PREFERENCES)) {
				Preference.loadFromDb(elem);
				continue;
			}
			
			if (tag.equals(Constant.XML_TAG_QUOTE)) {
				addQuote(elem);
				continue;
			}

			if (!tag.equals(Constant.XML_TAG_OPERATION))
				continue;
			String opType = elem.getAttribute("type");

			if (opType.equals("buy")) {
				try {
					Buy op = new Buy(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Buy operation: " + e);
				}
				continue;
			}
			if (opType.equals("sell")) {
				try {
					Sell op = new Sell(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Sell operation: " + e);
				}
				continue;
			}
			if (opType.equals("daytrade")) {
				try {
					DayTrade op = new DayTrade(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse DayTrade operation: " + e);
				}
				continue;
			}
			if (opType.equals("deposit")) {
				try {
					Deposit op = new Deposit(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Deposit operation: " + e);
				}
				continue;
			}
			if (opType.equals("withdraw")) {
				try {
					Withdraw op = new Withdraw(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Withdraw operation: " + e);
				}
				continue;
			}
			if (opType.equals("fee")) {
				try {
					Fee op = new Fee(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Fee operation: " + e);
				}
				continue;
			}
			if (opType.equals("transfer")) {
				try {
					Transfer op = new Transfer(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Transfer operation: " + e);
				}
				continue;
			}
			if (opType.equals("yield")) {
				try {
					Yield op = new Yield(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Yield operation: " + e);
				}
				continue;
			}
			if (opType.equals("split")) {
				try {
					Split op = new Split(elem);
					addOperation(op);
				}
				catch (IllegalArgumentException e) {
					dbLog("Could not parse Split operation: " + e);
				}
				continue;
			}
		} // for (nodeList)

		dbLog("Parsing XML DB... done");

		dbLog("Updating data structures...");

		buildOpTab();
		buildOpPanel();
		buildAccTab();
		buildAccPanel();
		buildAddQuoteAssetDropBox();
		buildQuoteGrid();
		buildYieldTable();
		buildSplitGrid();
		buildAddAssetAccountDropBox();
		buildAddYieldAccountDropBox();
		updateTaxGrid();
		updatePrefPanel();

		dbLog("Updating data structures... done");
	}
 }

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CarteiraInveste implements EntryPoint {

	// Carteira() builds the client GUI
	static Carteira carteiraAtual = new Carteira();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		// Associate the Main panel with the HTML host page.
		RootPanel.get("carteira").add(carteiraAtual.mainPanel);

		// Focus on first tab
		carteiraAtual.tabPanel.selectTab(0);
	}
}

class StockTaxPrefResetButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Preference.stockTaxResetDefaultsOptions();
		Carteira curr = CarteiraInveste.carteiraAtual;
		curr.updateTaxGrid();
		curr.updateXmlDb();
	}
}

class QuoteServiceGetQuoteCallback implements AsyncCallback<String> {

	int portfolioTabRow;

    QuoteServiceGetQuoteCallback(int row) {
		portfolioTabRow = row;
    }

    public void onSuccess(String result) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		
		curr.portfolioDebug("Callback QuoteService.getQuote OK: " + result);
		
		String resultList[] = result.split("\\s");
		String dateStr = resultList[0];
		String asset = resultList[1];
		String quote = resultList[2];
		
		if (!Character.isDigit(quote.charAt(0))) {
			return;
		}
		
		Date qDate = Preference.dateFormat.parse(dateStr);
		String q = quote.replace(',','.');
		double qValue = Double.parseDouble(q);
			
		Quote newQuote = new Quote(
			qDate,
			asset,
			1,
			qValue);
				
		curr.addQuote(newQuote);
		curr.updateXmlDb();
		curr.buildQuoteGrid();
		curr.updatePortfolioTable();
	}

    public void onFailure(Throwable caught) {
		CarteiraInveste.carteiraAtual.portfolioDebug("Callback QuoteService.getQuote FAIL: " + caught);
    }
}

class FetchQuotesButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Carteira curr = CarteiraInveste.carteiraAtual;

		if (curr.quoteService == null)
			curr.quoteService = (QuoteServiceAsync) GWT.create(QuoteService.class);

		Date quoteDate = curr.portfolioEndDate.getValue();

		int rowCount = curr.portfolioTab.getRowCount() - 1;
		for (int row = 1; row < rowCount; ++row) {

			QuoteServiceGetQuoteCallback callback = new QuoteServiceGetQuoteCallback(row);

			Label asset = (Label) curr.portfolioTab.getWidget(row, 0);
			String assetName = asset.getText();

			curr.portfolioDebug("QuoteService.getQuote: " + Preference.dateFormat.format(quoteDate) + " " + assetName);

			curr.quoteService.getQuote(quoteDate, assetName, callback);
		}

	}
}

class AddAssetButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {

		Carteira curr = CarteiraInveste.carteiraAtual;

		Object list[] = curr.accountList.toArray();
		String account = (curr.addAssetPanelCurrAccountIndex > 0) ?
			(String) list[curr.addAssetPanelCurrAccountIndex] : null;

		Operation newTrade = null;
		String assetName = curr.addAssetName.getText();
		if (assetName != null)
			assetName.trim();

		switch (curr.addAssetPanelCurrTrade) {
		case Operation.TRADE_BUY:
			newTrade = new Buy(curr.addAssetDate.getValue(),
				curr.addAssetPanelCurrType,
				assetName,
				Double.parseDouble(curr.addAssetAmount.getText()),
				CurrencyUtil.parseToCents(curr.addAssetCost.getText()),
				CurrencyUtil.parseToCents(curr.addAssetExpense.getText()),
				account);
			break;
		case Operation.TRADE_SELL:
			newTrade = new Sell(curr.addAssetDate.getValue(),
				curr.addAssetPanelCurrType,
				assetName,
				Double.parseDouble(curr.addAssetAmount.getText()),
				CurrencyUtil.parseToCents(curr.addAssetGrossValue.getText()),
				CurrencyUtil.parseToCents(curr.addAssetCost.getText()),
				CurrencyUtil.parseToCents(curr.addAssetRetainedTax.getText()),
				account);
			break;
		case Operation.TRADE_DAYTRADE:
			newTrade = new DayTrade(curr.addAssetDate.getValue(),
				assetName,
				Double.parseDouble(curr.addAssetAmount.getText()),
				CurrencyUtil.parseToCents(curr.addAssetDtBuyGross.getText()),
				CurrencyUtil.parseToCents(curr.addAssetDtSellGross.getText()),
				CurrencyUtil.parseToCents(curr.addAssetDtSellNet.getText()),
				CurrencyUtil.parseToCents(curr.addAssetRetainedTax.getText()),
				account);

			break;

		}

		curr.addOperation(newTrade);
		curr.updateXmlDb();
		curr.buildOpTab();
		curr.buildOpPanel();
		curr.buildAddQuoteAssetDropBox();
		curr.updateTaxGrid();
	}
}

class AccountActionChangeHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		CarteiraInveste.carteiraAtual.buildAddAccountPanel();
	}
}

class AddAccountButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Carteira curr = CarteiraInveste.carteiraAtual;

		//String accountName = curr.addAccountName.getText();

		int accountAction = curr.accountActionDropBox.getSelectedIndex();

		switch (accountAction) {		
		case Operation.ACC_DEPOSIT:
			Deposit d = new Deposit(curr.addAccountDate.getValue(),
				curr.addAccountName.getText(),
				CurrencyUtil.parseToCents(curr.addAccountValue.getText()),
				CurrencyUtil.parseToCents(curr.addAccountExpense.getText()));
			curr.addOperation(d);
			curr.accountTableAdd(d);
			break;
		case Operation.ACC_WITHDRAW:
			Withdraw w = new Withdraw(curr.addAccountDate.getValue(),
				curr.addAccountName.getText(),
				CurrencyUtil.parseToCents(curr.addAccountValue.getText()));
			curr.addOperation(w);
			curr.accountTableAdd(w);
			break;
		case Operation.ACC_FEE:
			Fee f = new Fee(curr.addAccountDate.getValue(),
				curr.addAccountName.getText(),
				CurrencyUtil.parseToCents(curr.addAccountValue.getText()));
			curr.addOperation(f);
			curr.accountTableAdd(f);
			break;
		case Operation.ACC_TRANSFER:
			Transfer t = new Transfer(curr.addAccountDate.getValue(),
				curr.addAccountFromName.getText(),
				curr.addAccountToName.getText(),
				CurrencyUtil.parseToCents(curr.addAccountValue.getText()));
			curr.addOperation(t);
			curr.accountTableAdd(t);
			break;
		default:
			return; // do nothing
		}

		curr.updateXmlDb();
		curr.buildAccTab();
		curr.buildAccPanel();
		curr.buildAddAssetAccountDropBox();
		curr.buildAddYieldAccountDropBox();
	}
}

class AddYieldButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Carteira curr = CarteiraInveste.carteiraAtual;

		int accountIndex = curr.yieldAccountDropBox.getSelectedIndex();
		Object list[] = curr.accountList.toArray();
		String account = (accountIndex > 0) ? (String) list[accountIndex] : null;

		curr.addOperation(new Yield(curr.yieldTypeDropBox.getSelectedIndex(),
						curr.addYieldDate.getValue(),
						//Integer.parseInt(curr.addYieldTaxYear.getText()),
						curr.addYieldAsset.getText(),
						Double.parseDouble(curr.addYieldAmount.getText()),
						CurrencyUtil.parseToCents(curr.addYieldGrossValue.getText()),
						CurrencyUtil.parseToCents(curr.addYieldNetValue.getText()),
						account));

		curr.updateXmlDb();
		curr.buildYieldTable();
		curr.buildAddQuoteAssetDropBox();
		curr.updateTaxGrid();
	}
}

class AddSplitButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Carteira curr = CarteiraInveste.carteiraAtual;

		curr.addOperation(new Split(curr.addSplitDate.getValue(),
						curr.addSplitAssetSuggest.getValue(),
						Double.parseDouble(curr.addSplitFrom.getText()),
						Double.parseDouble(curr.addSplitTo.getText())));

		curr.buildSplitGrid();
		curr.updateXmlDb();
		//curr.updateTaxGrid();
	}
}


class AddQuoteButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Carteira curr = CarteiraInveste.carteiraAtual;

		int assetIndex = curr.quoteAssetDropBox.getSelectedIndex();
		String assetName = curr.quoteAssetDropBox.getValue(assetIndex);

		Quote newQuote = new Quote(
					curr.addQuoteDate.getValue(),
					assetName,
					Double.parseDouble(curr.addQuoteAmount.getText()),
					Double.parseDouble(curr.addQuoteValue.getText())
					);

		curr.addQuote(newQuote);

		curr.updateXmlDb();
		curr.buildQuoteGrid();
	}
}

class ClearTodayQuoteHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		Date today = curr.addQuoteDate.getValue();
		curr.delQuoteFor(today);
	}
}

class ClearPastQuoteHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		Date today = curr.addQuoteDate.getValue();
		curr.delQuoteBefore(today);
	}
}

class SelectAssetTypeHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		CarteiraInveste.carteiraAtual.addAssetPanelCurrType =
			CarteiraInveste.carteiraAtual.assetTypeDropBox.getSelectedIndex();
		//CarteiraInveste.carteiraAtual.buildAddAssetPanel();
	}
}

class SelectTradeOpHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		CarteiraInveste.carteiraAtual.addAssetPanelCurrTrade =
			CarteiraInveste.carteiraAtual.operationDropBox.getSelectedIndex();
		CarteiraInveste.carteiraAtual.buildAddAssetPanel();
	}
}

class SelectAssetAccountHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		CarteiraInveste.carteiraAtual.addAssetPanelCurrAccountIndex =
			CarteiraInveste.carteiraAtual.assetAccountDropBox.getSelectedIndex();
		//CarteiraInveste.carteiraAtual.buildAddAssetPanel();
	}
}

class TradeFilterHandler implements ChangeHandler, SelectionHandler<SuggestOracle.Suggestion> {

	private void update() {
		Carteira curr = CarteiraInveste.carteiraAtual;

		String asset = curr.tradeFilterAssetSuggest.getText();
		curr.tradeFilterAssetSuggest.setText(asset.trim().toUpperCase());

		curr.buildOpTab();
	}

	public void onChange(ChangeEvent event) {
		update();
	}

	//public void onSuggestionSelected(SuggestionEvent event) {
	//	update();
	//}

	public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
		update();
	}
}

class DbTextChangeHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		String text = CarteiraInveste.carteiraAtual.dbText.getText();
		Document messageDom;

		Carteira curr = CarteiraInveste.carteiraAtual;

		// parse the XML document into a DOM
		try {
			messageDom = XMLParser.parse(text);
		} catch (DOMException e) {
			curr.dbLog("Could not parse XML document: " + text);

			// Revert change
			curr.dbText.setText(CarteiraInveste.carteiraAtual.currXmlTextDb);
			return;
		}

		Element parent = messageDom.getDocumentElement();
		curr.loadDb(parent.getChildNodes());

		// Save change
		curr.currXmlTextDb = text;
	}
}

class AddAssetAmountHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		//TextBox input = (TextBox) event.getSource();
		CarteiraInveste.carteiraAtual.updateAddAssetAveragePriceLabel();
	}
}

class TextBoxCurrencyHandler implements ChangeHandler {

	public static void refreshCentsText(TextBox input) {
		CurrencyUtil.refreshCents(input);
	}

	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		refreshCentsText(input);
	}
}

class AddAssetGrossValueCurrencyHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		TextBoxCurrencyHandler.refreshCentsText(input);
	}
}

class AddAssetCostCurrencyHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		TextBoxCurrencyHandler.refreshCentsText(input);
		CarteiraInveste.carteiraAtual.updateAddAssetAveragePriceLabel();
	}
}

class AddAssetExpenseCurrencyHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		TextBoxCurrencyHandler.refreshCentsText(input);
		CarteiraInveste.carteiraAtual.updateAddAssetAveragePriceLabel();
	}
}

class AddAssetRetainedTaxCurrencyHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		TextBoxCurrencyHandler.refreshCentsText(input);
	}
}

class AddAssetDtBuyGrossCurrencyHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		TextBoxCurrencyHandler.refreshCentsText(input);
		CarteiraInveste.carteiraAtual.updateAddAssetDtNetResultLabel();
	}
}

class AddAssetDtSellGrossCurrencyHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		TextBoxCurrencyHandler.refreshCentsText(input);
	}
}

class AddAssetDtSellNetCurrencyHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		TextBoxCurrencyHandler.refreshCentsText(input);
		CarteiraInveste.carteiraAtual.updateAddAssetDtNetResultLabel();
	}
}

class TextBoxAssetHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();
		input.setText(input.getText().toUpperCase());
	}
}

class UnitQuoteHandler implements ChangeHandler {

	public void onChange(ChangeEvent event) {
		AssetTextBox input = (AssetTextBox) event.getSource();

		CurrencyUtil.refresh(input);

		Carteira curr = CarteiraInveste.carteiraAtual;

		Quote newQuote = new Quote(
					curr.portfolioEndDate.getValue(),
					input.assetName,
					1,
					Double.parseDouble(input.getText())
					);
		curr.addQuote(newQuote);

		curr.updateXmlDb();
		curr.buildQuoteGrid();
		curr.updatePortfolioTable();
	}
}

class SellGrossValueHandler implements ChangeHandler {

	public void onChange(ChangeEvent event) {
		SellGrossValueBox input = (SellGrossValueBox) event.getSource();

		CurrencyUtil.refreshCents(input); // Update interface

		input.sellOp.sellGrossValueCents = CurrencyUtil.parseToCents(input.getText()); // Update operation

		Carteira curr = CarteiraInveste.carteiraAtual;

		curr.updateTaxGrid(); // Update tax calculations
		curr.updateXmlDb();   // Export to XML db
	}
}

class SellRetainedTaxHandler implements ChangeHandler {

	public void onChange(ChangeEvent event) {
		SellRetainedTaxBox input = (SellRetainedTaxBox) event.getSource();

		CurrencyUtil.refreshCents(input); // Update interface

		input.sellOp.sellRetainedTaxCents = CurrencyUtil.parseToCents(input.getText()); // Update operation

		Carteira curr = CarteiraInveste.carteiraAtual;

		curr.updateTaxGrid(); // Update tax calculations
		curr.updateXmlDb();   // Export to XML db
	}
}

class EditDepositHandler implements ChangeHandler {

	public void onChange(ChangeEvent event) {
		EditDepositBox input = (EditDepositBox) event.getSource();

		CurrencyUtil.refreshCents(input); // Update interface

		input.setValueCents(CurrencyUtil.parseToCents(input.getText())); // Update Deposit

		Carteira curr = CarteiraInveste.carteiraAtual;

		curr.buildAccTab();
		curr.buildAccPanel();
		curr.updateXmlDb();   // Export to XML db
	}
}

class TabSelectionHandler implements SelectionHandler<Integer> {
	public void onSelection(SelectionEvent<Integer> event) {
		Integer selected = event.getSelectedItem();
		int tab = selected.intValue();
		switch (tab) {
		case Carteira.TAB_PORTFOLIO:
			CarteiraInveste.carteiraAtual.updatePortfolioTable();
			break;
		}
	}
}

class PortfolioEndDateHandler implements ValueChangeHandler<Date> {
	public void onValueChange(ValueChangeEvent<Date> event) {
		CarteiraInveste.carteiraAtual.updatePortfolioTable();
	}
}

class PortfolioHideSoldPositionsHandler implements ValueChangeHandler<Boolean> {
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		CarteiraInveste.carteiraAtual.updatePortfolioTable();
	}
}

class SummaryEndDateHandler implements ValueChangeHandler<Date> {
	public void onValueChange(ValueChangeEvent<Date> event) {
		CarteiraInveste.carteiraAtual.updateSummary();
	}
}

class SummaryUpdateButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		CarteiraInveste.carteiraAtual.updateSummary();
	}
}

class EvolutionEndDateHandler implements ValueChangeHandler<Date> {
	public void onValueChange(ValueChangeEvent<Date> event) {
		CarteiraInveste.carteiraAtual.updateEvolution();
	}
}

class EvolutionUpdateButtonHandler implements ClickHandler {
	public void onClick(ClickEvent event) {
		CarteiraInveste.carteiraAtual.updateEvolution();
	}
}

class TaxEndDateHandler implements ValueChangeHandler<Date> {
	public void onValueChange(ValueChangeEvent<Date> event) {
		CarteiraInveste.carteiraAtual.updateTaxGrid();
	}
}

class AssetTextBox extends TextBox {
	String assetName;
	AssetTextBox(String asset) {
		super();
		assetName = asset;
	}
}

class SellGrossValueBox extends TextBox {
	Sell sellOp;
	SellGrossValueBox(Sell op) {
		super();
		sellOp = op;
	}
}

class SellRetainedTaxBox extends TextBox {
	Sell sellOp;
	SellRetainedTaxBox(Sell op) {
		super();
		sellOp = op;
	}
}

abstract class EditDepositBox extends TextBox {
	Deposit deposit;

	EditDepositBox(Deposit dep, EditDepositHandler depositBoxHandler) {
		super();
		deposit = dep;
		addChangeHandler(depositBoxHandler);
	}

	abstract void setValueCents(long cents);
}

class EditDepositValueBox extends EditDepositBox {

	EditDepositValueBox(Deposit dep, EditDepositHandler depositBoxHandler) {
		super(dep, depositBoxHandler);
		setText(CurrencyUtil.format(dep.depValueCents));
	}

	void setValueCents(long cents) {
		deposit.depValueCents = cents;
	}
}

class EditDepositExpenseBox extends EditDepositBox {

	EditDepositExpenseBox(Deposit dep, EditDepositHandler depositBoxHandler) {
		super(dep, depositBoxHandler);
		setText(CurrencyUtil.format(dep.depExpenseCents));
	}

	void setValueCents(long cents) {
		deposit.depExpenseCents = cents;
	}
}

class PreferenceChangeHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		TextBox input = (TextBox) event.getSource();

		Carteira curr = CarteiraInveste.carteiraAtual;

		if (input == curr.prefSellSpreadText) {
			Preference.sellSpread = Double.parseDouble(curr.prefSellSpreadText.getText()) / 100;
		}

		if (input == curr.prefBrokerFeeText) {
			Preference.brokerFeeCents = CurrencyUtil.parseToCents(curr.prefBrokerFeeText.getText());
		}

		if (input == curr.prefTaxExemptionText) {
			Preference.stockTaxExemptionCents = CurrencyUtil.parseToCents(curr.prefTaxExemptionText.getText());
		}

		if (input == curr.prefTaxFeeText) {
			Preference.stockTaxFee = Double.parseDouble(curr.prefTaxFeeText.getText()) / 100;
		}

		if (input == curr.prefDayTradeTaxFeeText) {
			Preference.stockDayTradeTaxFee = Double.parseDouble(curr.prefDayTradeTaxFeeText.getText()) / 100;
		}
		
		curr.updatePrefPanel();
		curr.updatePortfolioTable();
		curr.updateTaxGrid();
		curr.updateXmlDb();
	}
}

class PreferenceDayTradeAffectExemptionHandler implements ValueChangeHandler<Boolean> {
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		curr.updateTaxGrid();
		curr.updateXmlDb();
	}
}

class PreferenceExemptGainReduceLossHandler implements ValueChangeHandler<Boolean> {
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		curr.updateTaxGrid();
		curr.updateXmlDb();
	}
}

class PreferenceTaxRatioOverPretax implements ValueChangeHandler<Boolean> {
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		curr.updateTaxGrid();
		curr.updateXmlDb();
	}
}

class Sales {
	long salesGrossValueCents;
	long retainedTaxCents;
	long monthSalesNetProfitsCents;

	long dayTradeMonthlySalesGrossCents;
	long dayTradeMonthlyNetResultCents;
	long dayTradeRetainedTaxCents;

	Sales() {
		salesGrossValueCents      = 0;
		retainedTaxCents          = 0;
		monthSalesNetProfitsCents = 0;

		dayTradeMonthlySalesGrossCents = 0;
		dayTradeMonthlyNetResultCents  = 0;
		dayTradeRetainedTaxCents       = 0;
	}
}

class YearSummary {
	long ySumJscpCents;
	long ySumDividendsCents;
	long ySumRentalCents;
	long ySumRental2Cents;
	long ySumExemptProfitCents;
	long ySumCarryLossCents;
	long ySumCouponCents;
      long ySumBondSaleNetEarningsCents;
	long ySumDayTradeCarryLossCents;

	YearSummary () {
		ySumJscpCents                = 0;
		ySumDividendsCents           = 0;
		ySumRentalCents              = 0;
		ySumRental2Cents              = 0;
		ySumExemptProfitCents        = 0;
		ySumCarryLossCents           = 0;
		ySumCouponCents              = 0;
            ySumBondSaleNetEarningsCents = 0;
		ySumDayTradeCarryLossCents   = 0;
	}
}

interface SellAssetCallback {
	void execute(Asset a, Sell s);
}

interface DayTradeCallback {
	void execute(Asset a, DayTrade dt);
}

class AccumulateSales implements SellAssetCallback,DayTradeCallback {
	ArrayList<Sales> monthlyTaxTable = null;
	int firstSaleMonth               = -1;
	VerticalPanel debugPanel         = null;

	AccumulateSales(ArrayList<Sales> monthTable, int firstMon, VerticalPanel debug) {
		monthlyTaxTable = monthTable;
		firstSaleMonth  = firstMon;
		debugPanel      = debug;
	}

	public void execute(Asset a, Sell s) {
		//Carteira curr = CarteiraInveste.carteiraAtual;
		int assetType = s.getAssetType();

		//
		// Calculate per-asset sales profit
		//

		if (assetType != Asset.ASSET_ACAO)
			return;

		// Average buy cost for stock
		double taxAvgBuyCostCents = a.getTaxAverageAcquisitionCostCents();
		long taxCostCents = Math.round(s.sellAmount * taxAvgBuyCostCents);
		long sellProfitCents = s.sellNetRevenueCents - taxCostCents;

		//
		// Find monthly accumulation -- only for stocks
		//

		Date opDate = s.sellDate;
		int sellMon = DateUtil.linearMonth(opDate);
		int monIndex = sellMon - firstSaleMonth;
		Sales monthSales = monthlyTaxTable.get(monIndex);

		debugPanel.add(new Label("DBG AccumulateSales: " +
			DateTimeFormat.getFormat("yyyy-MM-dd").format(opDate) +
			" " + a.assetName +
			" amtAcq=" + a.getAcquiredAmount() +
                  " amtSold=" + s.sellAmount +
                  " amtCurr=" + a.getCurrentAmount() +
			" buyAvgCost=" + CurrencyUtil.formatDouble(taxAvgBuyCostCents / 100) +
			" buyGrossCost=" + CurrencyUtil.format(taxCostCents) +
			" saleNetGain=" + CurrencyUtil.format(s.sellNetRevenueCents) +
			" netResult=" + CurrencyUtil.format(sellProfitCents)
		));

		// Update monthly profits
		if (monthSales == null)
			debugPanel.add(new Label("ugh AccumulateSales.execute: " +
			DateTimeFormat.getFormat("yyyy-MM-dd").format(opDate) +
			a.assetName +
			" sellMon=" + sellMon +
			" monIndex=" + monIndex +
			" monthSales=" + monthSales
			));
		else
			monthSales.monthSalesNetProfitsCents += sellProfitCents;
	}

	public void execute(Asset a, DayTrade dt) {
		//
		// Calculate per-asset sales profit
		//

		//
		// Find monthly accumulation
		//

		Date opDate = dt.dtDate;
		int sellMon = DateUtil.linearMonth(opDate);
		int monIndex = sellMon - firstSaleMonth;
		Sales monthSales = monthlyTaxTable.get(monIndex);

		debugPanel.add(new Label("DBG AccumulateDT: " +
			DateTimeFormat.getFormat("yyyy-MM-dd").format(opDate) +
			" " + a.assetName +
			" amt=" + dt.dtAmount +
			" buyGross=" + CurrencyUtil.format(dt.dtBuyGrossValueCents) +
			" sellGross=" + CurrencyUtil.format(dt.dtSellGrossValueCents) +
			" sellNet=" + CurrencyUtil.format(dt.dtSellNetValueCents) +
			" result=" + CurrencyUtil.format(dt.getNetResultCents())
		));

		// Update monthly profits
		if (monthSales == null)
			debugPanel.add(new Label("ugh AccumulateDT.execute: " +
			DateTimeFormat.getFormat("yyyy-MM-dd").format(opDate) +
			a.assetName +
			" sellMon=" + sellMon +
			" monIndex=" + monIndex +
			" monthSales=" + monthSales
			));
		else
			monthSales.dayTradeMonthlyNetResultCents += dt.getNetResultCents();

	}
}

class SummaryAccountChangeHandler implements ValueChangeHandler<Boolean> {
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		curr.updateSummary();
	}
}

class SummaryAccount {
	String   accountName;
	CheckBox checkBox;

	SummaryAccount(String accName) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		accountName = accName;
		checkBox = new CheckBox(accName);
		checkBox.setValue(true);
		checkBox.addValueChangeHandler(curr.summaryAccountHandler);
	}
}

class SummaryAssetChangeHandler implements ValueChangeHandler<Boolean> {
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		curr.updateSummary();
	}
}

class SummaryAsset {
	String   assetName;
	CheckBox checkBox;

	SummaryAsset(String aName) {
		Carteira curr = CarteiraInveste.carteiraAtual;
		assetName = aName;
		checkBox = new CheckBox(aName);
		checkBox.setValue(true);
		checkBox.addValueChangeHandler(curr.summaryAssetHandler);
	}
}

class StartTaxCarryLossChangeHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		//TextBox input = (TextBox) event.getSource();
		Carteira curr = CarteiraInveste.carteiraAtual;

		long lossCents = CurrencyUtil.parseToCents(Preference.startTaxCarryLoss.getText());
		if (lossCents > 0)
			lossCents = -lossCents;
		Preference.startTaxCarryLoss.setText(CurrencyUtil.format(lossCents));

		curr.updateTaxGrid();
		curr.updateXmlDb();
	}
}

class StartTaxDayTradeCarryLossChangeHandler implements ChangeHandler {
	public void onChange(ChangeEvent event) {
		//TextBox input = (TextBox) event.getSource();
		Carteira curr = CarteiraInveste.carteiraAtual;

		long lossCents = CurrencyUtil.parseToCents(Preference.startTaxDayTradeCarryLoss.getText());
		if (lossCents > 0)
			lossCents = -lossCents;
		Preference.startTaxDayTradeCarryLoss.setText(CurrencyUtil.format(lossCents));

		curr.updateTaxGrid();
		curr.updateXmlDb();
	}
}

class Term {
	double coeficient;
	double exponent;
	
	Term(double coef, double exp) {
		coeficient = coef;
		exponent   = exp;
	}
}

class Polynomial {
	static double eval(ArrayList<Term> poly, double x) {
		double y = 0;
		for (Term t : poly)
			y += t.coeficient * Math.pow(x, t.exponent);
		return y;
	}

	static double evalDerivative(ArrayList<Term> poly, double x) {
		double y = 0;
		for (Term t : poly) {
			// derive constant to zero
			if (t.exponent < 1)
				continue;
			y += t.exponent * t.coeficient * Math.pow(x, t.exponent - 1);
		}
		return y;
	}
}
