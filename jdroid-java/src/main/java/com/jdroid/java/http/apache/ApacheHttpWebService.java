package com.jdroid.java.http.apache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import com.jdroid.java.collections.Lists;
import com.jdroid.java.collections.Maps;
import com.jdroid.java.exception.ConnectionException;
import com.jdroid.java.exception.UnexpectedException;
import com.jdroid.java.http.HttpResponseWrapper;
import com.jdroid.java.http.HttpWebServiceProcessor;
import com.jdroid.java.http.Server;
import com.jdroid.java.http.WebService;
import com.jdroid.java.parser.Parser;
import com.jdroid.java.utils.EncodingUtils;
import com.jdroid.java.utils.FileUtils;
import com.jdroid.java.utils.LoggerUtils;
import com.jdroid.java.utils.StringUtils;

public abstract class ApacheHttpWebService implements WebService {
	
	protected static final Logger LOGGER = LoggerUtils.getLogger(ApacheHttpWebService.class);
	
	private Boolean ssl = false;
	
	/** Connection timeout in milliseconds */
	private Integer connectionTimeout;
	private String userAgent;
	
	private Server server;
	private List<Object> urlSegments;
	
	/** Query Parameter values of the request. */
	private Map<String, String> queryParameters = Maps.newLinkedHashMap();
	
	/** Header values of the request. */
	private Map<String, String> headers = Maps.newHashMap();
	
	private List<Cookie> cookies = Lists.newArrayList();
	
	private InputStream inputStream;
	
	private List<HttpWebServiceProcessor> httpWebServiceProcessors;
	
	private HttpClientFactory httpClientFactory;
	
	/**
	 * @param httpClientFactory the httpClientFactory
	 * @param httpWebServiceProcessors
	 * @param urlSegments
	 * @param server The {@link Server} where execute the request
	 */
	public ApacheHttpWebService(HttpClientFactory httpClientFactory, Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors) {
		
		this.urlSegments = Lists.newArrayList();
		if (urlSegments != null) {
			for (Object segment : urlSegments) {
				addUrlSegment(segment);
			}
		}
		this.httpClientFactory = httpClientFactory;
		this.server = server;
		this.httpWebServiceProcessors = httpWebServiceProcessors;
	}
	
	public abstract String getMethodName();
	
	/**
	 * @see com.jdroid.java.http.WebService#execute()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final <T> T execute() {
		return (T)execute(null);
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#execute(com.jdroid.java.parser.Parser)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T execute(Parser parser) {
		HttpClient client = null;
		try {
			
			if (httpWebServiceProcessors != null) {
				for (HttpWebServiceProcessor each : httpWebServiceProcessors) {
					each.beforeExecute(this);
				}
			}
			
			// make client for http.
			client = httpClientFactory.createHttpClient(connectionTimeout, userAgent);
			
			// Add Cookies
			addCookies(client);
			
			// make request.
			HttpUriRequest request = createHttpUriRequest(getUrl());
			
			// Log request
			LOGGER.debug(getMethodName() + ": " + request.getRequestLine().getUri());
			if (!queryParameters.isEmpty()) {
				LOGGER.debug("Query Parameters: " + queryParameters.toString());
			}
			if (!headers.isEmpty()) {
				LOGGER.debug("Headers: " + headers.toString());
			}
			
			// Add Headers
			addHeaders(request);
			
			// execute request.
			HttpResponse httpResponse = client.execute(request);
			
			if (httpWebServiceProcessors != null) {
				HttpResponseWrapper httpResponseWrapper = new ApacheHttpResponseWrapper(httpResponse);
				for (HttpWebServiceProcessor each : httpWebServiceProcessors) {
					each.afterExecute(this, httpResponseWrapper);
				}
			}
			
			// obtain input stream.
			inputStream = httpResponse.getEntity() != null ? httpResponse.getEntity().getContent() : null;
			Header contentEncoding = httpResponse.getFirstHeader(CONTENT_ENCODING_HEADER);
			if ((contentEncoding != null) && contentEncoding.getValue().equalsIgnoreCase(GZIP_ENCODING)) {
				inputStream = new GZIPInputStream(inputStream);
			}
			
			// parse and return response.
			return (T)((parser != null) && (inputStream != null) ? parser.parse(inputStream) : null);
		} catch (ClientProtocolException e) {
			Throwable cause = e.getCause();
			if ((cause != null) && (cause instanceof CircularRedirectException)) {
				throw new ConnectionException(e, false);
			} else {
				throw new UnexpectedException(e);
			}
		} catch (ConnectTimeoutException e) {
			throw new ConnectionException(e, true);
		} catch (IOException e) {
			throw new ConnectionException(e, false);
		} finally {
			FileUtils.safeClose(inputStream);
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#getUrl()
	 */
	@Override
	public String getUrl() {
		StringBuilder builder = new StringBuilder();
		builder.append(ssl && server.supportsSsl() ? HTTPS_PROTOCOL : HTTP_PROTOCOL);
		builder.append("://");
		builder.append(server.getBaseUrl());
		builder.append(getUrlSuffix());
		return builder.toString();
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#getUrlSuffix()
	 */
	@Override
	public String getUrlSuffix() {
		StringBuilder builder = new StringBuilder();
		builder.append(getUrlSegments());
		builder.append(makeStringParameters());
		return builder.toString();
	}
	
	protected String makeStringParameters() {
		StringBuilder params = new StringBuilder();
		boolean isFirst = true;
		
		for (Entry<String, String> entry : getQueryParameters().entrySet()) {
			if (isFirst) {
				params.append(QUESTION_MARK);
				isFirst = false;
			} else {
				params.append(AMPERSAND);
			}
			params.append(entry.getKey());
			params.append(EQUALS);
			params.append(entry.getValue());
		}
		
		return params.toString();
	}
	
	protected String getUrlSegments() {
		return urlSegments.isEmpty() ? StringUtils.EMPTY : StringUtils.SLASH
				+ StringUtils.join(urlSegments, StringUtils.SLASH);
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#addUrlSegment(java.lang.Object)
	 */
	@Override
	public void addUrlSegment(Object segment) {
		String segmentString = segment.toString();
		if (StringUtils.isNotEmpty(segmentString)) {
			urlSegments.add(EncodingUtils.encodeURL(segmentString));
		}
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String name, String value) {
		if (value != null) {
			headers.put(name, value);
		}
	}
	
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#addQueryParameter(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addQueryParameter(String name, Object value) {
		if (value != null) {
			queryParameters.put(name, EncodingUtils.encodeURL(value.toString()));
		}
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#addQueryParameter(java.lang.String, java.util.Collection)
	 */
	@Override
	public void addQueryParameter(String name, Collection<?> values) {
		addQueryParameter(name, StringUtils.join(values));
	}
	
	protected void addHeaders(HttpUriRequest httpUriRequest) {
		for (Entry<String, String> entry : headers.entrySet()) {
			httpUriRequest.addHeader(entry.getKey(), entry.getValue());
		}
	}
	
	protected void addCookies(HttpClient client) {
		if (client instanceof DefaultHttpClient) {
			for (Cookie cookie : cookies) {
				DefaultHttpClient.class.cast(client).getCookieStore().addCookie(cookie);
			}
		}
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#addHttpWebServiceProcessor(com.jdroid.java.http.HttpWebServiceProcessor)
	 */
	@Override
	public void addHttpWebServiceProcessor(HttpWebServiceProcessor httpWebServiceProcessor) {
		httpWebServiceProcessors.add(httpWebServiceProcessor);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " - " + server.getBaseUrl();
	}
	
	/**
	 * @return the Query parameters
	 */
	protected Map<String, String> getQueryParameters() {
		return queryParameters;
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#setConnectionTimeout(java.lang.Integer)
	 */
	@Override
	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	
	/**
	 * Create the {@link HttpUriRequest} to send.
	 * 
	 * @param url
	 */
	protected abstract HttpUriRequest createHttpUriRequest(String url);
	
	/**
	 * @see com.jdroid.java.http.WebService#setUserAgent(java.lang.String)
	 */
	@Override
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	/**
	 * @see com.jdroid.java.http.WebService#setSsl(java.lang.Boolean)
	 */
	@Override
	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}
	
}
