package com.frlgrd.ten.core;

import java.util.WeakHashMap;

import timber.log.Timber;

public class Logger {

	private static final WeakHashMap<String, String> SHORTEN_CLASS_NAMES = new WeakHashMap<>();

	public static void trace(String message, Object... args) {
		Timber.v(improveLog(message), args);
	}

	protected static String improveLog(String message) {
		String caller = retrieveCaller();
		if (caller == null)
			return message;

		return String.format("%s - %s", caller, message);
	}

	protected static String retrieveCaller() {
		try {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

			boolean isInLoggerClass = false;
			for (StackTraceElement stackTraceElement : stackTraceElements) {
				if (Logger.class.getCanonicalName().equals(stackTraceElement.getClassName())) {
					isInLoggerClass = true;
				} else if (isInLoggerClass) {
					return String.format("%s:%d", shortenCanonicalName(stackTraceElement.getClassName()), stackTraceElement.getLineNumber());
				}
			}
		} catch (Exception ignored) {
		}

		return null;
	}

	protected static String shortenCanonicalName(final String canonicalName) {
		if (canonicalName == null)
			return null;

		synchronized (SHORTEN_CLASS_NAMES) {
			String shortenClassName = SHORTEN_CLASS_NAMES.get(canonicalName);
			if (shortenClassName == null) {
				StringBuilder shortenName = new StringBuilder();

				int lastIndex = 0;
				int nextSep = -1;
				do {
					shortenName.append(canonicalName.charAt(nextSep + 1));
					nextSep = canonicalName.indexOf('.', nextSep + 1);

					if (nextSep > -1) {
						shortenName.append('.');
						lastIndex = nextSep + 1;
					}
				} while (nextSep > -1);

				if (lastIndex < canonicalName.length() - 1) {
					shortenName.append(canonicalName.substring(lastIndex + 1));
				}

				shortenClassName = shortenName.toString();
				SHORTEN_CLASS_NAMES.put(canonicalName, shortenClassName);
			}

			return shortenClassName;
		}
	}

	public static void debug(String message, Object... args) {
		Timber.d(improveLog(message), args);
	}

	public static void info(String message, Object... args) {
		Timber.i(improveLog(message), args);
	}

	public static void warn(String message, Object... args) {
		Timber.w(improveLog(message), args);
	}

	public static void warn(String message, Throwable tr, Object... args) {
		Timber.w(tr, improveLog(message), args);
	}

	public static void error(String message, Object... args) {
		Timber.e(improveLog(message), args);
	}

	public static void error(String message, Throwable tr, Object... args) {
		Timber.e(tr, improveLog(message), args);
	}

}
