package com.jdroid.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdSize;
import com.jdroid.android.AbstractApplication;
import com.jdroid.android.R;
import com.jdroid.android.activity.AbstractFragmentActivity;
import com.jdroid.android.activity.ActivityIf;
import com.jdroid.android.ad.AdHelper;
import com.jdroid.android.ad.HouseAdBuilder;
import com.jdroid.android.concurrent.SafeExecuteWrapperRunnable;
import com.jdroid.android.context.AppContext;
import com.jdroid.android.context.SecurityContext;
import com.jdroid.android.domain.User;
import com.jdroid.android.exception.DefaultExceptionHandler;
import com.jdroid.android.loading.FragmentLoading;
import com.jdroid.android.usecase.DefaultAbstractUseCase;
import com.jdroid.android.usecase.UseCase;
import com.jdroid.android.usecase.listener.DefaultUseCaseListener;
import com.jdroid.java.concurrent.ExecutorUtils;
import com.jdroid.java.exception.AbstractException;
import com.jdroid.java.utils.LoggerUtils;

import org.slf4j.Logger;

public class FragmentHelper implements FragmentIf {
	
	private final static Logger LOGGER = LoggerUtils.getLogger(FragmentHelper.class);
	
	private Fragment fragment;
	private AdHelper adHelper;
	
	private FragmentLoading loading;
	
	public FragmentHelper(Fragment fragment) {
		this.fragment = fragment;
	}
	
	public FragmentIf getFragmentIf() {
		return (FragmentIf)fragment;
	}
	
	protected Fragment getFragment() {
		return fragment;
	}
	
	@Override
	public ActivityIf getActivityIf() {
		return (ActivityIf)fragment.getActivity();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		LOGGER.debug("Executing onCreate on " + fragment);
		fragment.setRetainInstance(getFragmentIf().shouldRetainInstance());
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#shouldRetainInstance()
	 */
	@Override
	public Boolean shouldRetainInstance() {
		return true;
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
		LOGGER.debug("Executing onViewCreated on " + fragment);
		
		adHelper = createAdLoader();
		if (adHelper != null) {
			adHelper.loadBanner(fragment.getActivity(), (ViewGroup)(fragment.getView().findViewById(R.id.adViewContainer)),
					getFragmentIf().getAdSize(), getFragmentIf().getBannerAdUnitId(), getHouseAdBuilder());
		}
		
		if (loading == null) {
			loading = getFragmentIf().getDefaultLoading();
		}
		if (loading != null) {
			loading.onViewCreated(getFragmentIf());
		}
	}
	
	protected AdHelper createAdLoader() {
		return new AdHelper();
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		LOGGER.debug("Executing onActivityCreated on " + fragment);
	}
	
	public void onStart() {
		LOGGER.debug("Executing onStart on " + fragment);
		FragmentIf fragmentIf = getFragmentIf();
		if ((fragmentIf != null) && fragmentIf.shouldTrackOnFragmentStart()) {
			AbstractApplication.get().getAnalyticsSender().onFragmentStart(fragmentIf.getScreenViewName());
		}
	}
	
	public void onResume() {
		LOGGER.debug("Executing onResume on " + fragment);
		if (adHelper != null) {
			adHelper.onResume();
		}
	}
	
	public void onBeforePause() {
		if (adHelper != null) {
			adHelper.onPause();
		}
	}
	
	public void onPause() {
		LOGGER.debug("Executing onPause on " + fragment);
	}
	
	public void onStop() {
		LOGGER.debug("Executing onStop on " + fragment);
	}
	
	public void onDestroyView() {
		LOGGER.debug("Executing onDestroyView on " + fragment);
	}
	
	public void onBeforeDestroy() {
		if (adHelper != null) {
			adHelper.onDestroy();
		}
	}
	
	public void onDestroy() {
		LOGGER.debug("Executing onDestroy on " + fragment);
	}
	
	@Override
	public <E> E getArgument(String key) {
		return getArgument(key, null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E> E getArgument(String key, E defaultValue) {
		Bundle arguments = fragment.getArguments();
		E value = (arguments != null) && arguments.containsKey(key) ? (E)arguments.get(key) : null;
		return value != null ? value : defaultValue;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#executeOnUIThread(java.lang.Runnable)
	 */
	@Override
	public void executeOnUIThread(Runnable runnable) {
		Activity activity = fragment.getActivity();
		if ((activity != null) && activity.equals(AbstractApplication.get().getCurrentActivity())) {
			activity.runOnUiThread(new SafeExecuteWrapperRunnable(fragment, runnable));
		}
	}
	
	// Use case
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#onResumeUseCase(com.jdroid.android.usecase.DefaultAbstractUseCase,
	 *      com.jdroid.android.usecase.listener.DefaultUseCaseListener)
	 */
	@Override
	public void onResumeUseCase(DefaultAbstractUseCase useCase, DefaultUseCaseListener listener) {
		onResumeUseCase(useCase, listener, UseCaseTrigger.MANUAL);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#onResumeUseCase(com.jdroid.android.usecase.DefaultAbstractUseCase,
	 *      com.jdroid.android.usecase.listener.DefaultUseCaseListener,
	 *      com.jdroid.android.fragment.FragmentHelper.UseCaseTrigger)
	 */
	@Override
	public void onResumeUseCase(final DefaultAbstractUseCase useCase, final DefaultUseCaseListener listener,
			final UseCaseTrigger useCaseTrigger) {
		if (useCase != null) {
			ExecutorUtils.execute(new Runnable() {
				
				@Override
				public void run() {
					useCase.addListener(listener);
					if (useCase.isNotified()) {
						if (useCaseTrigger.equals(UseCaseTrigger.ALWAYS)) {
							useCase.run();
						}
					} else {
						if (useCase.isInProgress()) {
							listener.onStartUseCase();
						} else if (useCase.isFinishSuccessful()) {
							listener.onFinishUseCase();
							useCase.markAsNotified();
						} else if (useCase.isFinishFailed()) {
							try {
								listener.onFinishFailedUseCase(useCase.getAbstractException());
							} finally {
								useCase.markAsNotified();
							}
						} else if (useCase.isNotInvoked()
								&& (useCaseTrigger.equals(UseCaseTrigger.ONCE) || useCaseTrigger.equals(UseCaseTrigger.ALWAYS))) {
							useCase.run();
						}
					}
				}
			});
		}
	}
	
	public enum UseCaseTrigger {
		MANUAL,
		ONCE,
		ALWAYS;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#onPauseUseCase(com.jdroid.android.usecase.DefaultAbstractUseCase,
	 *      com.jdroid.android.usecase.listener.DefaultUseCaseListener)
	 */
	@Override
	public void onPauseUseCase(final DefaultAbstractUseCase userCase, final DefaultUseCaseListener listener) {
		if (userCase != null) {
			userCase.removeListener(listener);
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#executeUseCase(com.jdroid.android.usecase.UseCase)
	 */
	@Override
	public void executeUseCase(UseCase<?> useCase) {
		ExecutorUtils.execute(useCase);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#executeUseCase(com.jdroid.android.usecase.UseCase, java.lang.Long)
	 */
	@Override
	public void executeUseCase(UseCase<?> useCase, Long delaySeconds) {
		ExecutorUtils.schedule(useCase, delaySeconds);
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onStartUseCase()
	 */
	@Override
	public void onStartUseCase() {
		FragmentIf fragmentIf = getFragmentIf();
		if (fragmentIf != null) {
			fragmentIf.showLoading();
		}
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onUpdateUseCase()
	 */
	@Override
	public void onUpdateUseCase() {
		// Do nothing by default
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onFinishUseCase()
	 */
	@Override
	public void onFinishUseCase() {
		FragmentIf fragmentIf = getFragmentIf();
		if (fragmentIf != null) {
			fragmentIf.dismissLoading();
		}
	}
	
	/**
	 * @see com.jdroid.android.usecase.listener.DefaultUseCaseListener#onFinishFailedUseCase(com.jdroid.java.exception.AbstractException)
	 */
	@Override
	public void onFinishFailedUseCase(AbstractException abstractException) {
		FragmentIf fragmentIf = getFragmentIf();
		if (fragmentIf != null) {
			if (fragmentIf.goBackOnError(abstractException)) {
				DefaultExceptionHandler.markAsGoBackOnError(abstractException);
			} else {
				DefaultExceptionHandler.markAsNotGoBackOnError(abstractException);
			}
			fragmentIf.dismissLoading();
		}
		throw abstractException;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#goBackOnError(com.jdroid.java.exception.AbstractException)
	 */
	@Override
	public Boolean goBackOnError(AbstractException abstractException) {
		return true;
	}
	
	/**
	 * @see com.jdroid.android.activity.ComponentIf#findView(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V extends View> V findView(int id) {
		return (V)fragment.getView().findViewById(id);
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#findViewOnActivity(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V extends View> V findViewOnActivity(int id) {
		return (V)fragment.getActivity().findViewById(id);
	}
	
	/**
	 * @see com.jdroid.android.activity.ComponentIf#inflate(int)
	 */
	@Override
	public View inflate(int resource) {
		return getActivityIf().inflate(resource);
	}
	
	/**
	 * @see com.jdroid.android.activity.ComponentIf#getInstance(java.lang.Class)
	 */
	@Override
	public <I> I getInstance(Class<I> clazz) {
		return getActivityIf().getInstance(clazz);
	}
	
	/**
	 * @see com.jdroid.android.activity.ComponentIf#getExtra(java.lang.String)
	 */
	@Override
	public <E> E getExtra(String key) {
		return getActivityIf().getExtra(key);
	}
	
	/**
	 * @see com.jdroid.android.activity.ComponentIf#getAppContext()
	 */
	@Override
	public AppContext getAppContext() {
		return getActivityIf().getAppContext();
	}
	
	/**
	 * @see com.jdroid.android.activity.ComponentIf#getAdSize()
	 */
	@Override
	public AdSize getAdSize() {
		return getActivityIf().getAdSize();
	}

	@Override
	public String getBannerAdUnitId() {
		return getActivityIf().getBannerAdUnitId();
	}

	public HouseAdBuilder getHouseAdBuilder() {
		return null;
	}
	
	/**
	 * @see com.jdroid.android.activity.ComponentIf#getUser()
	 */
	@Override
	public User getUser() {
		return getActivityIf().getUser();
	}
	
	public Boolean isAuthenticated() {
		return SecurityContext.get().isAuthenticated();
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getActionBar()
	 */
	@Override
	public ActionBar getActionBar() {
		return ((AbstractFragmentActivity)fragment.getActivity()).getSupportActionBar();
	}
	
	// //////////////////////// Analytics //////////////////////// //
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#shouldTrackOnFragmentStart()
	 */
	@Override
	public Boolean shouldTrackOnFragmentStart() {
		return false;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getScreenViewName()
	 */
	@Override
	public String getScreenViewName() {
		return fragment.getClass().getSimpleName();
	}
	
	// //////////////////////// Loading //////////////////////// //
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#showLoading()
	 */
	@Override
	public void showLoading() {
		final FragmentIf fragmentIf = getFragmentIf();
		if (fragmentIf != null) {
			
			fragmentIf.executeOnUIThread(new Runnable() {
				
				@Override
				public void run() {
					if (loading != null) {
						loading.show(fragmentIf);
					} else {
						getActivityIf().showLoading();
					}
				}
			});
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#dismissLoading()
	 */
	@Override
	public void dismissLoading() {
		final FragmentIf fragmentIf = getFragmentIf();
		if (fragmentIf != null) {
			
			fragmentIf.executeOnUIThread(new Runnable() {
				
				@Override
				public void run() {
					if (loading != null) {
						loading.dismiss(fragmentIf);
					} else {
						getActivityIf().dismissLoading();
					}
				}
			});
			
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#getDefaultLoading()
	 */
	@Override
	public FragmentLoading getDefaultLoading() {
		return null;
	}
	
	/**
	 * @see com.jdroid.android.fragment.FragmentIf#setLoading(com.jdroid.android.loading.FragmentLoading)
	 */
	@Override
	public void setLoading(FragmentLoading loading) {
		this.loading = loading;
	}
	
	/**
	 * @see android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh()
	 */
	@Override
	public void onRefresh() {
		// Do nothing
	}
}
