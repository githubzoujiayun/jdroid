package com.jdroid.java.exception;

public class UnexpectedException extends ErrorCodeException {
	
	private static final long serialVersionUID = 7653653326088130933L;
	
	public UnexpectedException(String message, Throwable cause) {
		super(CommonErrorCode.UNEXPECTED_ERROR, message, cause);
		setTrackable(true);
	}
	
	public UnexpectedException(String message) {
		super(CommonErrorCode.UNEXPECTED_ERROR, message);
		setTrackable(true);
	}
	
	public UnexpectedException(Throwable cause) {
		super(CommonErrorCode.UNEXPECTED_ERROR, cause);
		setTrackable(true);
	}
	
	/**
	 * @see com.jdroid.java.exception.AbstractException#getThrowableToLog()
	 */
	@Override
	public Throwable getThrowableToLog() {
		return (getCause() != null) && (getMessage() == null) ? getCause() : super.getThrowableToLog();
	}
}