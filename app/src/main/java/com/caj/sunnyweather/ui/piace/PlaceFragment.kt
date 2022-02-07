package com.caj.sunnyweather.ui.piace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.caj.sunnyweather.MainActivity
import com.caj.sunnyweather.SunnyWeatherApplication
import com.caj.sunnyweather.WeatherActivity
import com.caj.sunnyweather.databinding.FragmentPlaceBinding
import com.caj.sunnyweather.logic.Repository

class PlaceFragment: Fragment() {

    private val TAG = "PlaceFragment"

    private lateinit var viewBinding: FragmentPlaceBinding
    val viewModel by lazy { ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(PlaceViewModel::class.java) }
    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentPlaceBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is MainActivity && viewModel.isPlaceSaved()) {
            val place = viewModel.getSavePlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
            activity?.finish()
            return
        }

        val layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this, viewModel.placeList)
        viewBinding.recyclerView.adapter = adapter
        viewBinding.searchPlaceEdit.addTextChangedListener {
            val content = it.toString()
            if (content.isNotEmpty()) {
                if (SunnyWeatherApplication.TOKEN.isEmpty()) {
                    Toast.makeText(context, "请在App配置授权码", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.searchPlaces(content)
                }
            } else {
                viewBinding.recyclerView.visibility = View.GONE
                viewBinding.bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }
        viewModel.placeLiveData.observe(this, Observer {
            Log.d(TAG, "name of thread in PlaceFragment.viewModel.placeLiveData.observe():${Thread.currentThread().name} ")
            val places = it.getOrNull()
            if (places != null) {
                viewBinding.recyclerView.visibility = View.VISIBLE
                viewBinding.bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(context, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                it.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}