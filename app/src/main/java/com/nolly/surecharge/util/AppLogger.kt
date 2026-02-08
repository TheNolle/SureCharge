package com.nolly.surecharge.util

import android.util.Log

object AppLogger {
	private const val GLOBAL_TAG_PREFIX = "SureCharge"

	private fun buildTag(tag: String): String = "$GLOBAL_TAG_PREFIX-$tag"

	fun d(tag: String, message: String) {
		Log.d(buildTag(tag), message)
	}

	fun i(tag: String, message: String) {
		Log.i(buildTag(tag), message)
	}

	fun w(tag: String, message: String, throwable: Throwable? = null) {
		if (throwable != null) {
			Log.w(buildTag(tag), message, throwable)
		} else {
			Log.w(buildTag(tag), message)
		}
	}

	fun e(tag: String, message: String, throwable: Throwable? = null) {
		if (throwable != null) {
			Log.e(buildTag(tag), message, throwable)
		} else {
			Log.e(buildTag(tag), message)
		}
	}
}
