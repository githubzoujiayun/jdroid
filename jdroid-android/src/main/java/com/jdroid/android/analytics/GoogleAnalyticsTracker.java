package com.jdroid.android.analytics;

import android.app.Activity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.jdroid.android.AbstractApplication;
import com.jdroid.java.exception.ConnectionException;

/**
 * 
 * @author Maxi Rosson
 */
public class GoogleAnalyticsTracker extends DefaultAnalyticsTracker {
	
	private static final GoogleAnalyticsTracker INSTANCE = new GoogleAnalyticsTracker();
	
	public static GoogleAnalyticsTracker get() {
		return INSTANCE;
	}
	
	public GoogleAnalyticsTracker() {
		GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(AbstractApplication.get());
		
		if (AbstractApplication.get().getAndroidApplicationContext().isGoogleAnalyticsDebugEnabled()) {
			googleAnalytics.getLogger().setLogLevel(LogLevel.VERBOSE);
		}
		googleAnalytics.getTracker(AbstractApplication.get().getAndroidApplicationContext().getGoogleAnalyticsTrackingId());
	}
	
	/**
	 * @see com.jdroid.android.analytics.AnalyticsTracker#isEnabled()
	 */
	@Override
	public Boolean isEnabled() {
		return AbstractApplication.get().getAndroidApplicationContext().isGoogleAnalyticsEnabled();
	}
	
	/**
	 * @see com.jdroid.android.analytics.DefaultAnalyticsTracker#onActivityStart(android.app.Activity)
	 */
	@Override
	public void onActivityStart(Activity activity) {
		EasyTracker.getInstance(activity).activityStart(activity);
	}
	
	/**
	 * @see com.jdroid.android.analytics.DefaultAnalyticsTracker#onActivityStop(android.app.Activity)
	 */
	@Override
	public void onActivityStop(Activity activity) {
		EasyTracker.getInstance(activity).activityStop(activity);
	}
	
	/**
	 * @see com.jdroid.android.analytics.AnalyticsTracker#trackConnectionException(com.jdroid.java.exception.ConnectionException)
	 */
	@Override
	public void trackConnectionException(ConnectionException connectionException) {
		// TODO Implement this
	}
}
