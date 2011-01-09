package novoda.lib.httpservice.request;

import static novoda.lib.httpservice.util.HttpServiceLog.Core.d;
import static novoda.lib.httpservice.util.HttpServiceLog.Core.debugIsEnable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import novoda.lib.httpservice.exception.RequestException;

import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.ResultReceiver;

/**
 * The request is just an IntentWrapper.
 * 
 * @author luigi@novoda.com
 *
 */
public class Request {
	
	public static final String SIMPLE_BUNDLE_RESULT = "result";
	
	private static final String ENCODING = "UTF-8";
	
	private static final char AND = '&';
	
	private static final String EMPTY = "";
	
	public static final long DEFAULT_UID = 0l;
	
	public static interface Extra {
		String result_receiver = "novoda.lib.httpservice.extra.RESULT_RECEIVER";
		String method = "novoda.lib.httpservice.extra.METHOD";
		String handler_key = "novoda.lib.httpservice.extra.HANDLER_KEY";
		String params = "novoda.lib.httpservice.extra.PARAMS";
		String uid = "novoda.lib.httpservice.extra.UID";
		String result_consumed_receiver = "novoda.lib.httpservice.extra.RESULT_CONSUMED";		
	}

	public static interface Method {
		int GET = 0;
		int POST = 1;	
	} 
	
	private Intent intent;
	
	public Request(Intent intent) {
		if(intent == null) {
			throw new RequestException("Intent is null! A Intent wrapper need an intent to work properly");
		}
		this.intent = intent;
	}
	
	public Intent getIntent(){
		return intent;
	}

	public Uri getUri() {
		Uri uri = intent.getData();
		if(uri == null) {
			throw new RequestException("Request url and uri are not specified. Need at least one!");
		}
		return uri;
	}

	public ResultReceiver getResultReceiver() {
		return getResultReceiver(Extra.result_receiver);
	}
	
	public ResultReceiver getResultConsumedReceiver() {
		return getResultReceiver(Extra.result_consumed_receiver);
	}
	
	private ResultReceiver getResultReceiver(String type) {
		Object receiverObj = intent.getParcelableExtra(type);
		if (receiverObj == null) {
			if (debugIsEnable()) {
				d("Request receiver " + type + " is null, skipping it");
			}
			return null;
		} 
		if(receiverObj instanceof ResultReceiver) {
			ResultReceiver resultReceiver = (ResultReceiver)receiverObj;
			if (debugIsEnable()) {
				d("Building request for intent with receiver : " + type);
			}
			return resultReceiver;
		} else {
			throw new RequestException("Problem generating reading the result receiver");
		}
	}

	public String getHandlerKey() {
		return intent.getStringExtra(Extra.handler_key);
	}
	
	public boolean isGet() {
		if(Method.GET == getMethod()) {
			return true;
		}
		return false;
	}

	public boolean isPost() {
		if(Method.POST == getMethod()) {
			return true;
		}
		return false;
	}

	public int getMethod() {
		return intent.getIntExtra(Extra.method, Method.GET);
	}

	public List<ParcelableBasicNameValuePair> getParams() {
		return intent.getParcelableArrayListExtra(Extra.params);
	}
	
	public static final URI asURI(Uri uri, List<ParcelableBasicNameValuePair> params) {
		StringBuilder query = new StringBuilder(EMPTY);
		if(params != null) {
			query.append(URLEncodedUtils.format(params, ENCODING));
	        if (uri.getQuery() != null && uri.getQuery().length() > 3) {
	            if (params.size() > 0) {
	                query.append(AND);
	            }
	            query.append(uri.getQuery());
	        }
        }
        return asURI(uri, query.toString());
    }
	
	public static final URI asURI(Uri uri, String query) {
        try {
            return URIUtils.createURI(uri.getScheme(), uri.getHost(), uri.getPort(),
                    uri.getEncodedPath(), query, uri.getFragment());
        } catch (URISyntaxException e) {
            throw new RequestException("Problem generating the URI with " + uri);
        }
    }
	
	public static final URI asURI(Uri uri) {
        return asURI(uri, EMPTY);
    }
	
	public static final URI asURI(Request request) {		
		return asURI(request.getUri(), request.getParams());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Request with URI: ");
		sb.append(getUri()).append(" and ").append(" requestReceiver: ");
		if(getResultReceiver() != null) {
			sb.append(" is not null");
		} else {
			sb.append(" is null");
		}
		sb.append(" and ").append("handlerKey: ").append(getHandlerKey());
		sb.append(" and ").append("method: ").append(getMethod());
		return sb.toString();
	}

	public long getUid() {
		return intent.getLongExtra(Request.Extra.uid, DEFAULT_UID);
	}
	
	public boolean isGeneratedByIntent(Intent intent) {
		long uid = intent.getLongExtra(Request.Extra.uid, DEFAULT_UID);
		return (this.getUid() == uid);
	}
}
