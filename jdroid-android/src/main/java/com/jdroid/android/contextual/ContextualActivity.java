package com.jdroid.android.contextual;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.jdroid.android.ActionItem;
import com.jdroid.android.R;
import com.jdroid.android.activity.AbstractFragmentActivity;
import com.jdroid.android.fragment.OnItemSelectedListener;
import com.jdroid.android.utils.ScreenUtils;

/**
 * 
 * @param <T>
 */
public abstract class ContextualActivity<T extends ActionItem> extends AbstractFragmentActivity implements
		OnItemSelectedListener<T> {
	
	private static final String DEFAULT_CONTEXTUAL_ITEM_EXTRA = "defaultContextualItem";
	public static final String DEFAULT_CONTEXTUAL_ITEM_BUNDLE_EXTRA = "defaultContextualItemBundle";
	
	public static Intent getStartIntent(Context context,
			Class<? extends ContextualActivity<?>> contextualActivityClass, ActionItem defaultContextualItem) {
		Intent intent = new Intent(context, contextualActivityClass);
		intent.putExtra(ContextualActivity.DEFAULT_CONTEXTUAL_ITEM_EXTRA, defaultContextualItem);
		return intent;
	}
	
	/**
	 * @see com.jdroid.android.activity.ActivityIf#getContentView()
	 */
	@Override
	public int getContentView() {
		if (isNavDrawerEnabled()) {
			return ScreenUtils.is7InchesOrLarger() ? R.layout.nav_contextual_activity
					: R.layout.nav_fragment_container_activity;
		} else {
			return ScreenUtils.is7InchesOrLarger() ? R.layout.contextual_activity
					: R.layout.fragment_container_activity;
		}
	}
	
	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			
			T defaultContextualItem = getExtra(DEFAULT_CONTEXTUAL_ITEM_EXTRA);
			if (defaultContextualItem == null) {
				defaultContextualItem = getDefaultContextualItem();
			}
			
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.fragmentContainer,
				newContextualListFragment(getContextualItems(), defaultContextualItem));
			
			if (ScreenUtils.is7InchesOrLarger()) {
				fragmentTransaction.add(R.id.detailsFragmentContainer,
					defaultContextualItem.createFragment(getExtra(DEFAULT_CONTEXTUAL_ITEM_BUNDLE_EXTRA)),
					defaultContextualItem.getName());
			}
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.commit();
		}
	}
	
	protected Fragment newContextualListFragment(List<T> actions, T defaultContextualItem) {
		return new ContextualListFragment(getContextualItems(), defaultContextualItem);
	}
	
	protected abstract List<T> getContextualItems();
	
	protected abstract T getDefaultContextualItem();
	
	/**
	 * @see com.jdroid.android.fragment.OnItemSelectedListener#onItemSelected(java.lang.Object)
	 */
	@Override
	public void onItemSelected(T item) {
		
		Fragment oldFragment = getDetailsFragment();
		if (oldFragment == null) {
			item.startActivity(this);
		} else {
			if (!oldFragment.getTag().equals(item.getName())) {
				replaceDetailsFragment(item);
			}
		}
	}
	
	public Fragment getContextualFragment() {
		return getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
	}
	
	public Fragment getDetailsFragment() {
		return getSupportFragmentManager().findFragmentById(R.id.detailsFragmentContainer);
	}
	
	private void replaceDetailsFragment(T item) {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.detailsFragmentContainer, item.createFragment(null), item.getName());
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
	}
	
	/**
	 * @see android.support.v4.app.FragmentActivity#onNewIntent(android.content.Intent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		T contextualItem = (T)intent.getExtras().getSerializable(DEFAULT_CONTEXTUAL_ITEM_EXTRA);
		
		if (ScreenUtils.is7InchesOrLarger()) {
			
			// Refresh the detail
			replaceDetailsFragment(contextualItem);
			
			// Refresh the list (only on large or bigger)
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.fragmentContainer,
				newContextualListFragment(getContextualItems(), contextualItem));
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.commit();
		}
	}
}