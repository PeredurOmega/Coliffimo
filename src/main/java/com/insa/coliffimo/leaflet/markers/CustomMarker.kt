package com.insa.coliffimo.leaflet.markers

abstract class CustomMarker() : Marker {
    fun divIcon(svgCode: String) =
        """
        L.divIcon({
          html: `$svgCode`,
          className: "",
          iconSize: [36, 60],
          iconAnchor: [17, 60],
          popupAnchor: [0, -60],
        })
        """.trimIndent()
}