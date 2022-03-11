package com.insa.coliffimo.leaflet

import com.insa.coliffimo.leaflet.ControlPosition

/**
 * Class for defining the zoom control of the map.

 * @author Stefan Saring
 */
class ZoomControlConfig @JvmOverloads constructor(
        val show: Boolean = true,
        val position: ControlPosition = ControlPosition.TOP_LEFT)