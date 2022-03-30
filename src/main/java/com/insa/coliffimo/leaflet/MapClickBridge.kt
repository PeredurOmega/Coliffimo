package com.insa.coliffimo.leaflet

import com.insa.coliffimo.MainController

class MapClickBridge(private val mainController: MainController) {

    fun callbackMapClick(lat: Double, lng: Double) {
        println("$lat $lng marker CREATION")
        mainController.addPoint(LatLong(lat, lng))
    }

    fun callbackMapDragged(lat: Double, lng: Double, name: String) {
        println("dragged $lat $lng marker $name")
    }
}