package com.insa.coliffimo.leaflet

class MapClickBridge {

    fun callbackMapClick(lat: Double, lng: Double){
        println("$lat $lng")
    }

    fun callbackMapDragged(lat: Double, lng: Double, name: String){
        println("dragged $lat $lng marker $name")
    }
}