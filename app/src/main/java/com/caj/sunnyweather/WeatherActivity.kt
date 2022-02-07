package com.caj.sunnyweather

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.caj.sunnyweather.databinding.ActivityWeatherBinding
import com.caj.sunnyweather.databinding.ForecastItemBinding
import com.caj.sunnyweather.logic.model.Weather
import com.caj.sunnyweather.logic.model.getSky
import com.caj.sunnyweather.ui.weather.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    val viewBinding by lazy { ActivityWeatherBinding.inflate(layoutInflater) }

    val viewModel by lazy { ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
//        ViewCompat.getWindowInsetsController(viewBinding.root)?.apply {
//            hide(WindowInsetsCompat.Type.statusBars())
//            hide(WindowInsetsCompat.Type.navigationBars())
//        }
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }

        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }

        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        viewModel.weatherLiveData.observe(this, Observer {
            val weather = it.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                it.exceptionOrNull()?.printStackTrace()
            }
            viewBinding.swipeRefresh.isRefreshing = false
        })

        viewBinding.swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary)
        refreshWeather()
        viewBinding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        viewBinding.now.navBtn.setOnClickListener {
            viewBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
        viewBinding.drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    fun refreshWeather() {
        if (SunnyWeatherApplication.TOKEN.isEmpty()) {
            Toast.makeText(this, "请在App配置授权码", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
            viewBinding.swipeRefresh.isRefreshing = true
        }
    }

    private fun showWeatherInfo(weather: Weather) {
        viewBinding.now.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} °C"
        viewBinding.now.currentTemp.text = currentTempText
        viewBinding.now.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数${realtime.airQuality.aqi.chn.toInt()}"
        viewBinding.now.currentAQI.text = currentPM25Text
        viewBinding.now.root.setBackgroundResource(getSky(realtime.skycon).bg)
        // 填充forecast.xml布局中的数据
        viewBinding.forecast.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val forecastItemBinding = ForecastItemBinding.inflate(layoutInflater, viewBinding.forecast.forecastLayout, false)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            forecastItemBinding.dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            forecastItemBinding.skyIcon.setImageResource(sky.icon)
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} °C"
            forecastItemBinding.temperatureInfo.text = tempText
            viewBinding.forecast.forecastLayout.addView(forecastItemBinding.root)
        }
        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        viewBinding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        viewBinding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        viewBinding.lifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        viewBinding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        viewBinding.weatherLayout.visibility = View.VISIBLE
    }
}