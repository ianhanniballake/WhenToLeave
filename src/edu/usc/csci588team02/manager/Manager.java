package edu.usc.csci588team02.manager;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.xml.atom.AtomParser;

import edu.usc.csci588team02.model.Namespace;

public class Manager
{
	private static HttpTransport transport;

	/**
	 * Create a new Manager with a new HttpTransport. Note the transport must be
	 * authenticated using setAuthToken!
	 */
	public Manager()
	{
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		transport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("google-calendarandroidsample-1.0");
		headers.gdataVersion = "2";
		final AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Namespace.DICTIONARY;
		transport.addParser(parser);
	}

	/**
	 * Create a manager using an existing HttpTransport
	 * 
	 * @param transport
	 *            the transport to use
	 */
	public Manager(final HttpTransport transport)
	{
		Manager.transport = transport;
	}

	protected HttpTransport getTransport()
	{
		return transport;
	}

	public void setAuthToken(final String authToken)
	{
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
	}
}
