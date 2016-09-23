package carteirainveste.server;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import carteirainveste.client.QuoteService;
import java.text.SimpleDateFormat;

public class QuoteServiceImpl extends RemoteServiceServlet implements QuoteService {

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
  private String returnQuote(String date, String asset, String value) {
	return date + " " + asset + " " + value;
  }
  
  private String fetchQuote(String date, String asset) {
  
    String urlStr = "http://www.guiainvest.com.br/raiox/" + asset + ".aspx";
	URL url = null;
	try {
		url = new URL(urlStr);
	}
	catch (MalformedURLException e) {
		return "?bad URL=" + urlStr + " " + e;
	}
	
	BufferedReader in;
	try {
		in = new BufferedReader(new InputStreamReader(url.openStream()));
	}
	catch (IOException e) {
		return "?opening URL " + e;
	}
	
	String quote = "?";
	
	// ????-??-?? <span id="lbUltimaCotacao" class="Cotacao">R$ 41,43
	// 2012-09-19 <span id="lbUltimaCotacao">24,04</span>
	Pattern quoteLinePattern = Pattern.compile(".*lbUltimaCotacao\\D+(\\d+,\\d+)\\D*");
	
	for (;;) {
		String inputLine = null;
		try {
			inputLine = in.readLine();
		}
		catch (IOException e) {
		}
		if (inputLine == null)
			break;
							
		Matcher m = quoteLinePattern.matcher(inputLine);
		if (m.matches()) {
			String quoteMatch = m.group(1);	 
			quote = quoteMatch;
			break;
		}
    }

	try {
		in.close();
	}
	catch (IOException e) {
		return "?closing URL " + e;
	}
	
	return quote;
  }

  public String getQuote(Date quoteDate, String assetName) throws IllegalArgumentException {
  	String dateStr = null;
	try {
		dateStr = dateFormat.format(quoteDate);
	}
	catch (Exception e) {
		returnQuote("????-??-??", assetName, "getQuote,fail1");
	}
	
	if (dateStr == null) 
      return returnQuote("????-??-??", assetName, "getQuote,fail2");
	  
	return returnQuote(dateStr, assetName, fetchQuote(dateStr, assetName));
  }
}