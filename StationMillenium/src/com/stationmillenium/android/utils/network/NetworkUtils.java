/**
 * 
 */
package com.stationmillenium.android.utils.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.stationmillenium.android.BuildConfig;

/**
 * Network utils for the app 
 * @author vincent
 *
 */
public class NetworkUtils {

	private static final String TAG = "NetworkUtils";
	private static final String CONTENT_TYPE = "Content-Type";
	
	/**
	 * Connect to the URL 
	 * @param urlText the URL to connect as text
	 * @param parameters the {@link Map} of parameters as {@link String}
	 * @param requestMethod the request method as {@link String}
	 * @param contentType the content type as {@link String}
	 * @param connectTimeout the connect timeout as int
	 * @param readTimeout the read timeout as int
	 * @return the {@link InputStream} of the connection
	 */
	public static InputStream connectToURL(String urlText, Map<String, String> parameters, String requestMethod, String contentType, int connectTimeout, int readTimeout) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Connect to server to get data");
		try {
			//manage parameters
			String urlTextWithPAram = writeQueryString(parameters, urlText);
			
			//set up connection
			URL url = new URL(urlTextWithPAram);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(connectTimeout);
			connection.setReadTimeout(readTimeout);
			connection.setRequestMethod(requestMethod);
			connection.setRequestProperty(CONTENT_TYPE, contentType);
			
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Connection to use : " + connection);
			
			//connect
			connection.connect();
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Response code : " + connection.getResponseCode());
			return connection.getInputStream();
			
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error with URL", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "Error while getting XML data", e);
			return null;
		}
	}
	
	/**
	 * Append params to connection
	 * @param params the {@link Map} for params
	 * @param connection the base URL to append params
	 */
	private static String writeQueryString(Map<String, String> params, String baseURL) {
		if (params != null) {
			StringBuilder queryString = new StringBuilder();
			for (Entry<String, String> param : params.entrySet()) { //process each param
				queryString.append(queryString.length() == 0 ? "" : "&"); //append a separator if needed
				try { //
					queryString.append(URLEncoder.encode(param.getKey(), "UTF-8")); //append param name
					queryString.append("="); //append = sign
					queryString.append(URLEncoder.encode(param.getValue(), "UTF-8")); //append param value
				} catch (UnsupportedEncodingException e) {
					Log.w(TAG, "Error while encoding param : " + param, e);
				}
			}
			
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Query string to write to connection : " + queryString); 
			
			return baseURL + "?" + queryString;
			
		} else { //no params to add
			if (BuildConfig.DEBUG)
				Log.d(TAG, "No params specified"); 
			return baseURL; 
		}
			
	}
}
