package com.jdroid.android.debug;

import android.app.Activity;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import com.jdroid.android.AbstractApplication;
import com.jdroid.android.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DebugImageLoaderHelper {
	
	public static void initPreferences(final Activity activity, PreferenceScreen preferenceScreen) {
		
		if (AbstractApplication.get().isImageLoaderEnabled()) {
			
			PreferenceCategory preferenceCategory = new PreferenceCategory(activity);
			preferenceCategory.setTitle(R.string.universalImageLoader);
			preferenceScreen.addPreference(preferenceCategory);
			
			Preference preference = new Preference(activity);
			preference.setTitle(R.string.clearImagesDiscCache);
			preference.setSummary(R.string.clearImagesDiscCache);
			preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					ImageLoader.getInstance().clearDiskCache();
					return true;
				}
			});
			preferenceCategory.addPreference(preference);
			
			preference = new Preference(activity);
			preference.setTitle(R.string.clearImagesMemoryCache);
			preference.setSummary(R.string.clearImagesMemoryCache);
			preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					ImageLoader.getInstance().clearMemoryCache();
					return true;
				}
			});
			preferenceCategory.addPreference(preference);
		}
	}
	
}