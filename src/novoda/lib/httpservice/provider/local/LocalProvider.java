package novoda.lib.httpservice.provider.local;

import static novoda.lib.httpservice.util.HttpServiceLog.Provider.d;
import static novoda.lib.httpservice.util.HttpServiceLog.Provider.debugIsEnable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import novoda.lib.httpservice.exception.ProviderException;
import novoda.lib.httpservice.provider.EventBus;
import novoda.lib.httpservice.provider.Provider;
import novoda.lib.httpservice.request.Request;
import novoda.lib.httpservice.request.Response;
import android.net.Uri;

public class LocalProvider implements Provider {
	
	public HashMap<Uri, String> map = new HashMap<Uri, String>();
	
	private EventBus eventBus;
	
	public LocalProvider(EventBus eventBus) {	
		if(eventBus == null) {
			throw new ProviderException("EventBus is null, can't procede");
		}
		this.eventBus = eventBus;
	}
	
	public LocalProvider(EventBus eventBus, Uri uri, String content) {
		this(eventBus);
		map.put(uri, content);
	}
	
	public void add(Uri uri, String content) {
		map.put(uri, content);
	}
	
	public void add(String url, String content) {
		map.put(Uri.parse(url), content);
	}
	
	public InputStream getContent(Uri uri) {
		if(map.containsKey(uri)) {			
			return new ByteArrayInputStream(map.get(uri).getBytes());
		} else {
			if(debugIsEnable()) {
				d("There is no resource registered for the local provider for url : " + uri);
			}
			return null;
		}
	}
	
	@Override
	public Response execute(Request req) {
		InputStream content = getContent(req.getUri());
		if(content == null) {
			eventBus.fireOnThrowable(req, new Throwable("Content not found"));
			throw new ProviderException("Content not found");
		}
		Response response = new Response();
		response.setRequest(req);
		response.setContent(content);
		eventBus.fireOnContentReceived(response);
		return response;
	}

}
