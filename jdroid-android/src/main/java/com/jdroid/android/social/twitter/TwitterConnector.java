package com.jdroid.android.social.twitter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import com.jdroid.android.AbstractApplication;

/**
 * 
 * @author Maxi Rosson
 */
public class TwitterConnector {
	
	public static void openProfile(String account) {
		try {
			Intent intent = new Intent();
			intent.setClassName("com.twitter.android", "com.twitter.android.ProfileActivity");
			intent.putExtra("screen_name", account);
			AbstractApplication.get().getCurrentActivity().startActivity(intent);
		} catch (ActivityNotFoundException e) {
			AbstractApplication.get().getCurrentActivity().startActivity(
				new Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/" + account)));
		}
	}
}