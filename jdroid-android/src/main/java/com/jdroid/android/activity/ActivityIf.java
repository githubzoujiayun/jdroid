package com.jdroid.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.jdroid.android.loading.ActivityLoading;

import java.util.List;

public interface ActivityIf extends ComponentIf {
	
	public Boolean onBeforeSetContentView();
	
	public void onAfterSetContentView(Bundle savedInstanceState);
	
	public int getContentView();
	
	public Integer getMenuResourceId();
	
	public void doOnCreateOptionsMenu(Menu menu);
	
	/**
	 * @return Whether this {@link Activity} requires authentication or not
	 */
	public Boolean requiresAuthentication();
	
	public MenuInflater getMenuInflater();
	
	public Boolean isLauncherActivity();
	
	public Long getLocationFrequency();
	
	public List<Integer> getContextualMenuItemsIds();
	
	public Intent getUpIntent();
	
	public Boolean isInterstitialEnabled();

	public String getInterstitialAdUnitId();
	
	public void displayInterstitial(Boolean retryIfNotLoaded);
	
	public Boolean isActivityDestroyed();
	
	// //////////////////////// Loading //////////////////////// //
	
	public ActivityLoading getDefaultLoading();
	
	public void setLoading(ActivityLoading loading);
	
	// //////////////////////// Navigation Drawer //////////////////////// //
	
	public Boolean isNavDrawerEnabled();
	
	public Boolean isNavDrawerTopLevelView();

	public void onBackPressed();
	
}
