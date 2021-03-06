package com.jdroid.android.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.jdroid.android.R;
import com.jdroid.android.adapter.BaseArrayAdapter;
import com.jdroid.android.fragment.FragmentHelper.UseCaseTrigger;
import com.jdroid.android.listener.OnEnterKeyListener;
import com.jdroid.android.usecase.PaginatedUseCase;
import com.jdroid.android.usecase.SearchUseCase;
import com.jdroid.android.utils.AndroidUtils;
import com.jdroid.android.utils.ToastUtils;
import com.jdroid.java.exception.AbstractException;
import com.jdroid.java.utils.StringUtils;

/**
 * Base search Fragment. It has a search text and a list with the results.
 * 
 * @param <T> An item in the list.
 */
public abstract class AbstractSearchFragment<T> extends AbstractPaginatedGridFragment<T> {
	
	private int threshold = 1;
	private EditText searchText;
	private View loading;
	
	/**
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
	 *      android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.abstract_search_fragment, container, false);
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractListFragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		getGridView().getEmptyView().setVisibility(View.GONE);
		
		searchText = findView(R.id.searchText);
		searchText.setHint(getSearchEditTextHintResId());
		searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					AndroidUtils.showSoftInput(getActivity());
				} else {
					AndroidUtils.hideSoftInput(v);
				}
			}
		});
		searchText.requestFocus();
		
		if (isInstantSearchEnabled()) {
			searchText.addTextChangedListener(getTextWatcher());
			searchText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		} else {
			searchText.setOnKeyListener(new OnEnterKeyListener(false) {
				
				@Override
				public void onRun(View view) {
					search();
				}
			});
		}

		View searchButton = findView(R.id.searchButton);
		if (isInstantSearchEnabled()) {
			if (searchButton != null) {
				searchButton.setVisibility(View.GONE);
			}
		} else {
			searchButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					search();
				}
			});
		}

		View cancelButton = findView(R.id.cancelButton);
		if (displayCancelButton()) {
			cancelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					doCancel();
				}
			});
		} else {
			cancelButton.setVisibility(View.GONE);
		}
		
		TextView emptyLegend = findView(android.R.id.empty);
		emptyLegend.setText(getNoResultsResId());
		emptyLegend.setVisibility(View.GONE);
		
		loading = findView(R.id.loading);
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractPaginatedGridFragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		AndroidUtils.hideSoftInput(getView());
	}
	
	public Boolean isInstantSearchEnabled() {
		return true;
	}
	
	/**
	 * @return <code>true</code> if the amount of text in the field meets or exceeds the {@link #getThreshold}
	 *         requirement. You can override this to impose a different standard for when filtering will be triggered.
	 */
	public boolean enoughToFilter() {
		return searchText.getText().length() >= threshold;
	}
	
	protected TextWatcher getTextWatcher() {
		return new TextWatcher() {
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void afterTextChanged(Editable prefix) {
				AbstractSearchFragment.this.afterTextChanged(prefix.toString());
			}
		};
	}
	
	protected void afterTextChanged(String text) {
		if (enoughToFilter()) {
			search();
		} else {
			if (getBaseArrayAdapter() != null) {
				getBaseArrayAdapter().clear();
			}
		}
	}
	
	protected int getNoResultsResId() {
		return R.string.noResultsSearch;
	}
	
	protected int getSearchEditTextHintResId() {
		return R.string.typeHere;
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractPaginatedListFragment#getUseCaseTrigger()
	 */
	@Override
	protected UseCaseTrigger getUseCaseTrigger() {
		return UseCaseTrigger.MANUAL;
	}
	
	private void search() {
		if (StringUtils.isNotEmpty(searchText.getText().toString()) || !isSearchValueRequired()) {
			doSearch();
			if (!isInstantSearchEnabled()) {
				searchText.clearFocus();
			}
		} else {
			ToastUtils.showToast(R.string.requiredSearchTerm);
		}
	}
	
	protected void doCancel() {
		searchText.setText(null);
		getSearchUseCase().setSearchValue(null);
		getSearchUseCase().reset();
		dismissLoading();
		
		if (getBaseArrayAdapter() != null) {
			getBaseArrayAdapter().clear();
		}
	}
	
	protected void doSearch() {
		getSearchUseCase().setSearchValue(searchText.getText().toString());
		getSearchUseCase().reset();
		
		if (!isInstantSearchEnabled()) {
			if (getBaseArrayAdapter() != null) {
				getBaseArrayAdapter().clear();
			}
		}
		executeUseCase(getSearchUseCase());
		
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractFragment#showLoading()
	 */
	@Override
	public void showLoading() {
		executeOnUIThread(new Runnable() {
			
			@Override
			public void run() {
				if (loading != null) {
					loading.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#goBackOnError(com.jdroid.java.exception.AbstractException)
	 */
	@Override
	public Boolean goBackOnError(AbstractException abstractException) {
		return false;
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractFragment#dismissLoading()
	 */
	@Override
	public void dismissLoading() {
		executeOnUIThread(new Runnable() {
			
			@Override
			public void run() {
				if (loading != null) {
					loading.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private BaseArrayAdapter<T> getBaseArrayAdapter() {
		return (BaseArrayAdapter<T>)getListAdapter();
	}
	
	/**
	 * @return The {@link SearchUseCase} to use.
	 */
	protected abstract SearchUseCase<T> getSearchUseCase();
	
	protected boolean displayCancelButton() {
		return true;
	}
	
	/**
	 * @return Whether a search can be performed without a search value or not.
	 */
	public boolean isSearchValueRequired() {
		return false;
	}
	
	/**
	 * @return the threshold
	 */
	public int getThreshold() {
		return threshold;
	}
	
	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(int threshold) {
		if (threshold <= 0) {
			threshold = 1;
		}
		this.threshold = threshold;
	}
	
	/**
	 * @return the searchText
	 */
	public EditText getSearchText() {
		return searchText;
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractPaginatedListFragment#getPaginatedUseCase()
	 */
	@Override
	protected PaginatedUseCase<T> getPaginatedUseCase() {
		return getSearchUseCase();
	}
}
