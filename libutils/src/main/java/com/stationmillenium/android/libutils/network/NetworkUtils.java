/**
 *
 */
package com.stationmillenium.android.libutils.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import timber.log.Timber;

/**
 * Network utils for the app
 *
 * @author vincent
 */
public class NetworkUtils {

    private static final String TAG = "NetworkUtils";
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Connect to the URL
     *
     * @param urlText        the URL to connect as text
     * @param parameters     the {@link Map} of parameters as {@link String}
     * @param requestMethod  the request method as {@link String}
     * @param contentType    the content type as {@link String}
     * @param connectTimeout the connect timeout as int
     * @param readTimeout    the read timeout as int
     * @return the {@link InputStream} of the connection
     */
    public static InputStream connectToURL(String urlText, Map<String, String> parameters, String requestMethod, String contentType, int connectTimeout, int readTimeout) {
        Timber.d("Connect to server to get data");
        try {
            //manage parameters
            String urlTextWithPAram = writeQueryString(parameters, urlText);

            //set up connection
            URL url = new URL(urlTextWithPAram);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod(requestMethod);
            if (contentType != null)
                connection.setRequestProperty(CONTENT_TYPE, contentType);

            Timber.d("Connection to use : %s", connection);

            //connect
            connection.connect();
            Timber.d("Response code : %s", connection.getResponseCode());
            return connection.getInputStream();

        } catch (MalformedURLException e) {
            Timber.e(e, "Error with URL");
            return null;
        } catch (IOException e) {
            Timber.e(e, "Error while getting XML data");
            return null;
        }
    }

    /**
     * Append params to connection
     *
     * @param params     the {@link Map} for params
     * @param baseURL the base URL to append params
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
                    Timber.w(e, "Error while encoding param : %s", param);
                }
            }

            Timber.d("Query string to write to connection : %s", queryString);
            return baseURL + "?" + queryString;

        } else { //no params to add
            Timber.d("No params specified");
            return baseURL;
        }

    }
}
