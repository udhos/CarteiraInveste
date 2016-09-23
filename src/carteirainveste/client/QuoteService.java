package carteirainveste.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.Date;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("quote")
public interface QuoteService extends RemoteService {
  String getQuote(Date quoteDate, String assetName) throws IllegalArgumentException;
}
