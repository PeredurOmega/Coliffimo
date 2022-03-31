package com.insa.coliffimo.leaflet

import com.insa.coliffimo.MainController

class MapClickBridge(private val mainController: MainController) {

    fun callbackMapClick(lat: Double, lng: Double) {
        mainController.addPoint(LatLong(lat, lng))
    }

    fun callbackMarkerDelete(idMarker: String) {
        println(idMarker)
        mainController.deletePoint(idMarker)
    }


    fun callbackMapDragged(lat: Double, lng: Double, idMarker: String) {
        mainController.movePoint(idMarker, lat, lng)
    }
}