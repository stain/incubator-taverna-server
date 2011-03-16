/*
 * Copyright (C) 2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.notification;

import static org.taverna.server.master.notification.NotificationEngine.log;
import static twitter4j.conf.PropertyConfiguration.OAUTH_ACCESS_TOKEN;
import static twitter4j.conf.PropertyConfiguration.OAUTH_ACCESS_TOKEN_SECRET;
import static twitter4j.conf.PropertyConfiguration.OAUTH_CONSUMER_KEY;
import static twitter4j.conf.PropertyConfiguration.OAUTH_CONSUMER_SECRET;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.joda.time.DateTime;
import org.springframework.web.context.ServletConfigAware;
import org.taverna.server.master.interfaces.MessageDispatcher;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;
import twitter4j.http.AuthorizationFactory;

/**
 * Super simple-minded twitter dispatcher. You need to tell it your consumer key
 * and secret as part of the connection parameters, for example via a dispatcher
 * URN of "<tt>twitter:fred:bloggs</tt>" where <tt>fred</tt> is the key and
 * <tt>bloggs</tt> is the secret.
 * 
 * @author Donal Fellows
 */
public class TwitterDispatcher implements MessageDispatcher, ServletConfigAware {
	public static final int MAX_MESSAGE_LENGTH = 140;
	public static final char ELLIPSIS = '\u2026';

	private Properties props = new Properties();
	private int cooldownSeconds;
	private Map<String, DateTime> lastSend = new HashMap<String, DateTime>();

	public void setProperties(Properties properties) {
		props = properties;
	}

	private Properties getProperties() throws NotConfiguredException {
		Properties p = (Properties) props.clone();

		if (config != null) {
			String str;
			str = config.getInitParameter(ACCESS_TOKEN_PROP);
			if (str != null)
				p.setProperty(ACCESS_TOKEN_PROP, str);
			str = config.getInitParameter(ACCESS_SECRET_PROP);
			if (str != null)
				p.setProperty(ACCESS_SECRET_PROP, str);
		}
		if (p.getProperty(ACCESS_TOKEN_PROP, "").isEmpty()
				|| p.getProperty(ACCESS_SECRET_PROP, "").isEmpty())
			throw new NotConfiguredException();
		return p;
	}

	/**
	 * Set how long must elapse between updates to the status of any particular
	 * user. Calls before that time are just silently dropped. This is so we can
	 * be a better Twitter-enabled citizen.
	 * 
	 * @param cooldownSeconds
	 *            Time to elapse, in seconds.
	 */
	public void setCooldownSeconds(int cooldownSeconds) {
		this.cooldownSeconds = cooldownSeconds;
	}

	public static final String ACCESS_TOKEN_PROP = OAUTH_ACCESS_TOKEN;
	public static final String ACCESS_SECRET_PROP = OAUTH_ACCESS_TOKEN_SECRET;

	private ServletConfig config;
	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		config = servletConfig;
		try {
			getProperties();
		} catch (NotConfiguredException e) {
			log.warn("incomplete configuration; disabling Twitter dispatcher");
		}
	}

	private Twitter getTwitter(String key, String secret) throws Exception {
		if (key.isEmpty() || secret.isEmpty())
			throw new NoCredentialsException();

		Properties p = getProperties();
		p.setProperty(OAUTH_CONSUMER_KEY, key);
		p.setProperty(OAUTH_CONSUMER_SECRET, secret);

		Configuration config = new PropertyConfiguration(p);
		TwitterFactory factory = new TwitterFactory(config);
		Twitter t = factory.getInstance(AuthorizationFactory
				.getInstance(config));
		// Verify that we can connect!
		t.getOAuthAccessToken();
		return t;
	}

	// TODO: Get secret from credential manager
	@Override
	public void dispatch(String messageSubject, String messageContent,
			String targetParameter) throws Exception {
		// messageSubject ignored
		String[] target = targetParameter.split(":", 2);
		if (target == null || target.length != 2)
			throw new Exception("missing consumer key or secret");
		String who = target[0];
		synchronized (lastSend) {
			DateTime last = lastSend.get(who);
			if (last != null) {
				DateTime now = new DateTime();
				if (!now.isAfter(last.plusSeconds(cooldownSeconds)))
					return;
				lastSend.put(who, now);
			}
		}
		Twitter twitter = getTwitter(who, target[1]);

		if (messageContent.length() > MAX_MESSAGE_LENGTH)
			messageContent = messageContent
					.substring(0, MAX_MESSAGE_LENGTH - 1) + ELLIPSIS;
		twitter.updateStatus(messageContent);
	}

	@Override
	public boolean isAvailable() {
		try {
			// Try to create the configuration and push it through as far as
			// confirming that we can build an access object (even if it isn't
			// bound to a user)
			new TwitterFactory(new PropertyConfiguration(getProperties()))
					.getInstance();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static class NotConfiguredException extends Exception {
		NotConfiguredException() {
			super("not configured with xAuth key and secret; "
					+ "dispatch not possible");
		}
	}

	public static class NoCredentialsException extends Exception {
		NoCredentialsException() {
			super("no consumer key and secret present; "
					+ "dispatch not possible");
		}
	}
}
