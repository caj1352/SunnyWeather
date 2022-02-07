package com.caj.sunnyweather.ui.piace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.caj.sunnyweather.logic.Repository
import com.caj.sunnyweather.logic.model.Place

class PlaceViewModel: ViewModel() {
    private val queryLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val placeLiveData = Transformations.switchMap(queryLiveData) {
        Repository.searchPlaces(it)
    }

    fun searchPlaces(query: String) {
        queryLiveData.value = query
    }

    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavePlace() = Repository.getSavePlace()

    fun isPlaceSaved() = Repository.isPlaceSaved()
}