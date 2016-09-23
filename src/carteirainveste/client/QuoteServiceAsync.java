package carteirainveste.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;

/**
 * The async counterpart of <code>QuoteService</code>.
 */
public interface QuoteServiceAsync {
  void getQuote(Date quoteDate, String assetName, AsyncCallback<String> callback)
      throws IllegalArgumentException;
}
