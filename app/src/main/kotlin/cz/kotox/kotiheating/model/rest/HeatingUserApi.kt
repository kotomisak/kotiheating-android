package cz.kotox.kotiheating.model.rest

import android.app.Application
import com.google.gson.Gson
import cz.kotox.kotiheating.BuildConfig
import cz.kotox.kotiheating.model.entity.HeatingAuthResult
import cz.kotox.ktools.getRetrofit
import cz.kotox.ktools.inject
import okhttp3.logging.HttpLoggingInterceptor

class HeatingUserApi {

	val application by inject<Application>()

	private val headerRequestInterceptor by inject<HeaderRequestInterceptor>()

	val gson by inject<Gson>()

	val api: HeatingRouter
		get() {
			return getRetrofit(application, BuildConfig.REST_BASE_URL, HttpLoggingInterceptor.Level.BODY, gson = gson,
					customInterceptors = *arrayOf(headerRequestInterceptor)).create(HeatingRouter::class.java)
		}

	private val apiNoAuth: HeatingRouter
		get() {
			return getRetrofit(application, BuildConfig.REST_BASE_URL, HttpLoggingInterceptor.Level.BODY, gson = gson)
					.create(HeatingRouter::class.java)
		}

	suspend fun authorizeGoogleUser(idToken: String?): HeatingAuthResult? {
		if (idToken != null) {
			return apiNoAuth.authorizeGoogleUser(idToken).await()
		} else {
			throw IllegalStateException("idToken must not be null!")
		}

	}
}