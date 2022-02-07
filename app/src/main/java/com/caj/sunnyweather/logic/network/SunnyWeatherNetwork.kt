package com.caj.sunnyweather.logic.network

import android.app.DownloadManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {
    private val placeService = ServiceCreator.create(PlaceService::class.java)
    private suspend fun <T> Call<T>.await(): T = suspendCoroutine {
        enqueue(object: Callback<T>{
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()

                if (body == null) {
                    it.resumeWithException(RuntimeException("response body is null"))
                } else {
                    it.resume(body)
                }

            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                it.resumeWithException(t)
            }
        })
    }

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()


    val weatherService = ServiceCreator.create(WeatherService::class.java)
    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()
    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()
}