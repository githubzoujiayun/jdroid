package com.jdroid.java.api;

import java.io.File;
import java.util.List;
import java.util.Map;
import com.jdroid.java.collections.Lists;
import com.jdroid.java.http.HttpWebServiceProcessor;
import com.jdroid.java.http.MultipartWebService;
import com.jdroid.java.http.Server;
import com.jdroid.java.http.WebService;
import com.jdroid.java.http.cache.Cache;
import com.jdroid.java.http.cache.CachedWebService;
import com.jdroid.java.http.cache.CachingStrategy;
import com.jdroid.java.http.mock.AbstractMockWebService;
import com.jdroid.java.http.post.EntityEnclosingWebService;
import com.jdroid.java.marshaller.MarshallerMode;
import com.jdroid.java.marshaller.MarshallerProvider;

public abstract class AbstractApiService {
	
	// GET
	
	protected WebService newGetService(Object... urlSegments) {
		return newGetService(false, urlSegments);
	}
	
	protected WebService newGetService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newGetServiceImpl(getServer(), Lists.newArrayList(urlSegments), getHttpWebServiceProcessors());
		}
	}
	
	protected WebService newCachedGetService(Cache cache, CachingStrategy cachingStrategy, Long timeToLive,
			Object... urlSegments) {
		WebService webService = newGetService(urlSegments);
		return new CachedWebService(webService, cache, cachingStrategy, timeToLive) {
			
			@Override
			protected File getHttpCacheDirectory(Cache cache) {
				return AbstractApiService.this.getHttpCacheDirectory(cache);
			}
		};
	}
	
	protected abstract WebService newGetServiceImpl(Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	// POST
	
	protected EntityEnclosingWebService newPostService(Object... urlSegments) {
		return newPostService(false, urlSegments);
	}
	
	protected EntityEnclosingWebService newPostService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newPostServiceImpl(getServer(), Lists.newArrayList(urlSegments), getHttpWebServiceProcessors());
		}
	}
	
	protected abstract EntityEnclosingWebService newPostServiceImpl(Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	protected MultipartWebService newMultipartPostService(Object... urlSegments) {
		return newMultipartPostService(false, urlSegments);
	}
	
	protected MultipartWebService newMultipartPostService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newMultipartPostServiceImpl(getServer(), Lists.newArrayList(urlSegments),
				getHttpWebServiceProcessors());
		}
	}
	
	protected abstract MultipartWebService newMultipartPostServiceImpl(Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	protected EntityEnclosingWebService newFormPostService(Object... urlSegments) {
		return newFormPostService(false, urlSegments);
	}
	
	protected EntityEnclosingWebService newFormPostService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newFormPostServiceImpl(getServer(), Lists.newArrayList(urlSegments), getHttpWebServiceProcessors());
		}
	}
	
	protected abstract EntityEnclosingWebService newFormPostServiceImpl(Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	// PUT
	
	protected EntityEnclosingWebService newPutService(Object... urlSegments) {
		return newPutService(false, urlSegments);
	}
	
	protected EntityEnclosingWebService newPutService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newPutServiceImpl(getServer(), Lists.newArrayList(urlSegments), getHttpWebServiceProcessors());
		}
	}
	
	protected abstract EntityEnclosingWebService newPutServiceImpl(Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	protected MultipartWebService newMultipartPutService(Object... urlSegments) {
		return newMultipartPutService(false, urlSegments);
	}
	
	protected MultipartWebService newMultipartPutService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newMultipartPutServiceImpl(getServer(), Lists.newArrayList(urlSegments),
				getHttpWebServiceProcessors());
		}
	}
	
	protected abstract MultipartWebService newMultipartPutServiceImpl(Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	// DELETE
	
	protected WebService newDeleteService(Object... urlSegments) {
		return newDeleteService(false, urlSegments);
	}
	
	protected WebService newDeleteService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newDeleteServiceImpl(getServer(), Lists.newArrayList(urlSegments), getHttpWebServiceProcessors());
		}
	}
	
	protected abstract WebService newDeleteServiceImpl(Server server, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	// PATCH
	
	protected EntityEnclosingWebService newPatchService(Object... urlSegments) {
		return newPatchService(false, urlSegments);
	}
	
	protected EntityEnclosingWebService newPatchService(Boolean mocked, Object... urlSegments) {
		if (isHttpMockEnabled() || mocked) {
			return getAbstractMockWebServiceInstance(urlSegments);
		} else {
			return newPatchServiceImpl(getServer(), Lists.newArrayList(urlSegments), getHttpWebServiceProcessors());
		}
	}
	
	protected EntityEnclosingWebService newCachedPatchService(Cache cache, CachingStrategy cachingStrategy,
			Long timeToLive, Object... urlSegments) {
		WebService webService = newPutService(urlSegments);
		return new CachedWebService(webService, cache, cachingStrategy, timeToLive) {
			
			@Override
			protected File getHttpCacheDirectory(Cache cache) {
				return AbstractApiService.this.getHttpCacheDirectory(cache);
			}
		};
	}
	
	protected abstract EntityEnclosingWebService newPatchServiceImpl(Server baseURL, List<Object> urlSegments,
			List<HttpWebServiceProcessor> httpWebServiceProcessors);
	
	protected abstract Server getServer();
	
	protected List<HttpWebServiceProcessor> getHttpWebServiceProcessors() {
		return getServer().getHttpWebServiceProcessors();
	}
	
	protected abstract AbstractMockWebService getAbstractMockWebServiceInstance(Object... urlSegments);
	
	protected abstract Boolean isHttpMockEnabled();
	
	protected File getHttpCacheDirectory(Cache cache) {
		return null;
	}
	
	public void marshallSimple(EntityEnclosingWebService webservice, Object object) {
		marshall(webservice, object, MarshallerMode.SIMPLE);
	}
	
	public void marshall(EntityEnclosingWebService webservice, Object object) {
		marshall(webservice, object, MarshallerMode.COMPLETE);
	}
	
	public void marshall(EntityEnclosingWebService webservice, Object object, MarshallerMode mode) {
		marshall(webservice, object, mode, null);
	}
	
	public void marshall(EntityEnclosingWebService webservice, Object object, Map<String, String> extras) {
		marshall(webservice, object, MarshallerMode.COMPLETE, extras);
	}
	
	public void marshall(EntityEnclosingWebService webservice, Object object, MarshallerMode mode,
			Map<String, String> extras) {
		webservice.setEntity(MarshallerProvider.get().marshall(object, mode, extras).toString());
	}
}
