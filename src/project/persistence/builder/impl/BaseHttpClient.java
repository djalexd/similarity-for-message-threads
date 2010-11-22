package project.persistence.builder.impl;

import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class BaseHttpClient {

    /**
     * The default parameters.
     * Instantiated in {@link #setup setup}.
     */
    private HttpParams defaultParameters = null;
	
    /**
     * The scheme registry.
     * Instantiated in {@link #setup setup}.
     */
    private static SchemeRegistry supportedSchemes;

    
    public BaseHttpClient () {
    	setup ();
    }
	
    protected final HttpClient createHttpClient() {

        ClientConnectionManager ccm =
            new ThreadSafeClientConnManager(getParams(), supportedSchemes);

        DefaultHttpClient dhc =
            new DefaultHttpClient(ccm, getParams());

        return dhc;
    }


    /**
     * Performs general setup.
     * This should be called only once.
     */
    private final void setup() {

        supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));

        // prepare parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        defaultParameters = params;

    }

    protected final HttpParams getParams() {
        return defaultParameters;
    }

    /**
     * Creates a request to execute in this example.
     *
     * @return  a request without an entity
     */
    protected final HttpRequest createRequest(String path) {

        HttpRequest req = new BasicHttpRequest
            ("GET", path, HttpVersion.HTTP_1_1);

        return req;
    }	
}
