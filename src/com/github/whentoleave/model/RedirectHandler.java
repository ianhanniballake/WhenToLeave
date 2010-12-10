package com.github.whentoleave.model;

import java.io.IOException;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;

/**
 * Handle required redirects to allow Google to add sessionid. See <a
 * href="http://code.google.com/apis/calendar/faq.html#redirect_handling" >How
 * do I handle redirects...?</a>.<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/Namespace.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class RedirectHandler
{
	/**
	 * Intercepts redirects needed by Google
	 */
	private static class SessionIntercepter implements HttpExecuteIntercepter
	{
		/**
		 * The Google Session ID
		 */
		private final String gsessionid;

		/**
		 * Constructor for the SessionIntercepter
		 * 
		 * @param locationUrl
		 *            url to intercept
		 */
		private SessionIntercepter(final GoogleUrl locationUrl)
		{
			gsessionid = (String) locationUrl.getFirst("gsessionid");
		}

		@Override
		public void intercept(final HttpRequest request)
		{
			request.url.set("gsessionid", gsessionid);
		}

		/**
		 * Registers this intercepter with the given HttpTransport
		 * 
		 * @param transport
		 *            HttpTransport to register this Intercepter with
		 */
		public void register(final HttpTransport transport)
		{
			transport.removeIntercepters(SessionIntercepter.class);
			transport.intercepters.add(0, this); // must be first
		}
	}

	/**
	 * Executes the given request, handling redirects as needed
	 * 
	 * @param request
	 *            request to execute
	 * @return the response of the request
	 * @throws IOException
	 *             on IO error
	 */
	static HttpResponse execute(final HttpRequest request) throws IOException
	{
		try
		{
			return request.execute();
		} catch (final HttpResponseException e)
		{
			if (e.response.statusCode == 302)
			{
				final GoogleUrl url = new GoogleUrl(e.response.headers.location);
				request.url = url;
				final SessionIntercepter sessionIntercepter = new SessionIntercepter(
						url);
				sessionIntercepter.register(request.transport);
				e.response.ignore(); // force the connection to close
				return request.execute();
			}
			throw e;
		}
	}
}