package com.jdroid.android.exception;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.UnknownHostException;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.slf4j.Logger;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import com.jdroid.android.AbstractApplication;
import com.jdroid.android.R;
import com.jdroid.android.utils.AndroidUtils;
import com.jdroid.android.utils.GooglePlayUtils;
import com.jdroid.android.utils.LocalizationUtils;
import com.jdroid.java.collections.Lists;
import com.jdroid.java.concurrent.ExecutorUtils;
import com.jdroid.java.exception.AbstractException;
import com.jdroid.java.exception.ConnectionException;
import com.jdroid.java.exception.ErrorCode;
import com.jdroid.java.exception.ErrorCodeException;
import com.jdroid.java.utils.LoggerUtils;

public class DefaultExceptionHandler implements ExceptionHandler {
	
	private final static Logger LOGGER = LoggerUtils.getLogger(DefaultExceptionHandler.class);
	
	private static final String MAIN_THREAD_NAME = "main";
	private static final String GO_BACK_KEY = "goBack";
	
	private UncaughtExceptionHandler wrappedExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
	private UncaughtExceptionHandler defautlExceptionHandler;
	
	/**
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		
		Boolean mainThread = thread.getName().equals(MAIN_THREAD_NAME);
		
		if (mainThread) {
			handleMainThreadException(thread, throwable);
		} else if (DefaultExceptionHandler.matchAnyErrorCode(throwable, CommonErrorCode.INVALID_API_VERSION)) {
			handleInvalidApiVersionError();
		} else if (!doUncaughtException(thread, throwable)) {
			try {
				handleThrowable(thread, throwable);
			} catch (Exception e) {
				logHandledException("Error when trying to handle an exception", e);
			}
		}
	}
	
	protected Boolean doUncaughtException(Thread thread, Throwable throwable) {
		return false;
	}
	
	protected void handleMainThreadException(Thread thread, Throwable throwable) {
		try {
			wrappedExceptionHandler.uncaughtException(thread, throwable);
		} catch (Exception e) {
			LOGGER.error("Error when trying to handle an exception", e);
			defautlExceptionHandler.uncaughtException(thread, throwable);
		}
	}
	
	protected void handleInvalidApiVersionError() {
		GooglePlayUtils.showUpdateDialog();
	}
	
	/**
	 * @see com.jdroid.android.exception.ExceptionHandler#logWarningException(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void logWarningException(String errorMessage, Throwable throwable) {
		if (throwable instanceof ConnectionException) {
			logHandledException(throwable);
		} else {
			logHandledException(new WarningException(errorMessage, throwable));
		}
	}
	
	/**
	 * @see com.jdroid.android.exception.ExceptionHandler#logWarningException(java.lang.String)
	 */
	@Override
	public void logWarningException(String errorMessage) {
		logHandledException(new WarningException(errorMessage));
	}
	
	/**
	 * @see com.jdroid.android.exception.ExceptionHandler#logHandledException(java.lang.Throwable)
	 */
	@Override
	public void logHandledException(Throwable throwable) {
		logHandledException(null, throwable);
	}
	
	/**
	 * @see com.jdroid.android.exception.ExceptionHandler#logHandledException(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void logHandledException(String errorMessage, Throwable throwable) {
		
		if (throwable instanceof ConnectionException) {
			ConnectionException connectionException = (ConnectionException)throwable;
			
			Boolean isSSLPeerUnverifiedError = false;
			// Added to log at least the exception message, because Logcat does not show the stackTrace when it
			// includes a UnknownHostException
			errorMessage = throwable.getMessage();
			Throwable cause = connectionException.getCause();
			while (cause != null) {
				if (cause instanceof UnknownHostException) {
					errorMessage += ": " + cause.getMessage();
					break;
				} else if (cause instanceof SSLPeerUnverifiedException) {
					isSSLPeerUnverifiedError = true;
					break;
				}
				cause = cause.getCause();
			}
			
			if (isSSLPeerUnverifiedError) {
				LOGGER.error(errorMessage, throwable);
				final Throwable sslCause = cause;
				// This exception is logged on a new thread to always generate the exactly same stack trace, so
				// Crittercism can group all of them.
				ExecutorUtils.execute(new Runnable() {
					
					@Override
					public void run() {
						AbstractApplication.get().getAnalyticsSender().trackHandledException(
							new SSLPeerUnverifiedException(sslCause.getMessage()));
					}
				});
			} else {
				LOGGER.warn(errorMessage, throwable);
			}
			
		} else {
			Boolean trackable = true;
			Throwable throwableToLog = throwable;
			if (throwable instanceof AbstractException) {
				AbstractException abstractException = (AbstractException)throwable;
				trackable = abstractException.isTrackable();
				throwableToLog = abstractException.getThrowableToLog();
			}
			
			if (errorMessage == null) {
				errorMessage = throwable.getMessage();
				if (errorMessage == null) {
					errorMessage = throwableToLog.getMessage();
					if (errorMessage == null) {
						errorMessage = "Error";
					}
				}
			}
			
			if (trackable) {
				LOGGER.error(errorMessage, throwableToLog);
				AbstractApplication.get().getAnalyticsSender().trackHandledException(throwableToLog);
			} else {
				LOGGER.warn(errorMessage);
			}
		}
	}
	
	protected void displayError(String title, String message, Throwable throwable) {
		Activity activity = AbstractApplication.get().getCurrentActivity();
		if (activity != null) {
			ErrorDialogFragment.show((FragmentActivity)activity, title, message, goBackOnError(throwable));
		}
	}
	
	private Boolean goBackOnError(Throwable throwable) {
		if (throwable instanceof AbstractException) {
			AbstractException abstractException = (AbstractException)throwable;
			return abstractException.hasParameter(GO_BACK_KEY) ? abstractException.<Boolean>getParameter(GO_BACK_KEY)
					: goBackOnErrorByDefault(abstractException);
		} else {
			return goBackOnErrorByDefault(throwable);
		}
	}
	
	protected Boolean goBackOnErrorByDefault(Throwable throwable) {
		return true;
	}
	
	public static void markAsGoBackOnError(AbstractException abstractException) {
		abstractException.addParameter(GO_BACK_KEY, true);
	}
	
	public static void markAsNotGoBackOnError(AbstractException abstractException) {
		abstractException.addParameter(GO_BACK_KEY, false);
	}
	
	public static void setMessageOnConnectionTimeout(RuntimeException runtimeException, String text) {
		if (runtimeException instanceof ConnectionException) {
			ConnectionException connectionException = (ConnectionException)runtimeException;
			if (connectionException.isTimeout()) {
				connectionException.setDescription(text);
			}
		}
		
	}
	
	public static void setMessageOnConnectionTimeout(RuntimeException runtimeException, int resId) {
		setMessageOnConnectionTimeout(runtimeException, LocalizationUtils.getString(resId));
	}
	
	/**
	 * @see com.jdroid.android.exception.ExceptionHandler#handleThrowable(java.lang.Throwable)
	 */
	@Override
	public void handleThrowable(Throwable throwable) {
		handleThrowable(Thread.currentThread(), throwable);
	}
	
	private void handleThrowable(Thread thread, Throwable throwable) {
		
		String title = null;
		String description = null;
		if (throwable instanceof AbstractException) {
			AbstractException abstractException = (AbstractException)throwable;
			title = abstractException.getTitle();
			description = abstractException.getDescription();
			if (((title == null) || (description == null)) && (abstractException instanceof ErrorCodeException)) {
				ErrorCodeException errorCodeException = (ErrorCodeException)abstractException;
				if (title == null) {
					title = LocalizationUtils.getTitle(errorCodeException.getErrorCode());
					if ((title == null)
							&& errorCodeException.getErrorCode().equals(
								com.jdroid.java.exception.CommonErrorCode.CONNECTION_ERROR)) {
						title = LocalizationUtils.getString(R.string.connectionErrorTitle);
					}
				}
				if (description == null) {
					description = LocalizationUtils.getDescription(errorCodeException.getErrorCode(),
						errorCodeException.getErrorCodeDescriptionArgs());
					if ((description == null)
							&& errorCodeException.getErrorCode().equals(
								com.jdroid.java.exception.CommonErrorCode.CONNECTION_ERROR)) {
						description = LocalizationUtils.getString(R.string.connectionErrorDescription);
					}
				}
			}
		}
		
		if (title == null) {
			title = LocalizationUtils.getString(R.string.defaultErrorTitle, AndroidUtils.getApplicationName());
		}
		
		if (description == null) {
			description = LocalizationUtils.getString(R.string.defaultErrorDescription);
		}
		
		displayError(title, description, throwable);
		
		logHandledException(throwable);
	}
	
	public static Boolean matchAnyErrorCode(Throwable throwable, ErrorCode... errorCodes) {
		List<ErrorCode> errorCodesList = Lists.newArrayList(errorCodes);
		if (throwable instanceof ErrorCodeException) {
			ErrorCodeException errorCodeException = (ErrorCodeException)throwable;
			return errorCodesList.contains(errorCodeException.getErrorCode());
		}
		return false;
	}
	
	/**
	 * @see com.jdroid.android.exception.ExceptionHandler#setDefaultExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
	 */
	@Override
	public void setDefaultExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
		defautlExceptionHandler = uncaughtExceptionHandler;
	}
	
}
