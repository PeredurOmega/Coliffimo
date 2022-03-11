package com.insa.coliffimo.leaflet

import javafx.concurrent.Worker
import javafx.scene.layout.StackPane
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import java.util.concurrent.CompletableFuture


/**
 * JavaFX component for displaying OpenStreetMap based maps by using the Leaflet.js JavaScript library inside a WebView
 * browser component.<br/>
 * This component can be embedded most easily by placing it inside a StackPane, the component uses then the size of the
 * parent automatically.
 *
 * @author Stefan Saring
 */
open class LeafletMapView : StackPane() {

    private val webView = WebView()
    private val webEngine: WebEngine = webView.engine

    private var varNameSuffix: Int = 1

    /**
     * Creates the LeafletMapView component, it does not show any map yet.
     */
    init {
        this.children.add(webView)
    }

    /**
     * Displays the initial map in the web view. Needs to be called and complete before adding any markers or tracks.
     * The returned CompletableFuture will provide the final map load state, the map can be used when the load has
     * completed with state SUCCEEDED (use CompletableFuture#whenComplete() for waiting to complete).
     *
     * @param mapConfig configuration of the map layers and controls
     * @return the CompletableFuture which will provide the final map load state
     */
    fun displayMap(mapConfig: MapConfig): CompletableFuture<Worker.State> {
        val finalMapLoadState = CompletableFuture<Worker.State>()

        webEngine.loadWorker.stateProperty().addListener { _, _, newValue ->
            if (newValue == Worker.State.SUCCEEDED) {
                executeMapSetupScripts(mapConfig)
            }

            if (newValue == Worker.State.SUCCEEDED || newValue == Worker.State.FAILED) {
                finalMapLoadState.complete(newValue)
            }
        }

        val localFileUrl = LeafletMapView::class.java.getResource("/leafletmap/leafletmap.html")
        webEngine.load(localFileUrl!!.toExternalForm())
        return finalMapLoadState
    }

    private fun executeMapSetupScripts(mapConfig: MapConfig) {
        // execute scripts for layer definition
        mapConfig.layers.forEachIndexed { i, layer ->
            execScript("var layer${i + 1} = ${layer.javaScriptCode};")
        }

        val jsLayers = mapConfig.layers
            .mapIndexed { i, layer -> "'${layer.displayName}': layer${i + 1}" }
            .joinToString(", ")
        execScript("var baseMaps = { $jsLayers };")

        // execute script for map view creation (Leaflet attribution must not be a clickable link)
        execScript(
            """
                |var myMap = L.map('map', {
                |    center: new L.LatLng(${mapConfig.initialCenter.latitude}, ${mapConfig.initialCenter.longitude}),
                |    zoom: 8,
                |    zoomControl: false,
                |    layers: [layer1]
                |});
                |
                |var attribution = myMap.attributionControl;
                |attribution.setPrefix('Leaflet');""".trimMargin()
        )

        // execute script for layer control definition if there are multiple layers
        if (mapConfig.layers.size > 1) {
            execScript(
                """
                    |var overlayMaps = {};
                    |var controlLayer = L.control.layers(baseMaps, overlayMaps).addTo(myMap);""".trimMargin()
            )
        }

        // execute script for scale control definition
        if (mapConfig.scaleControlConfig.show) {
            execScript(
                "L.control.scale({position: '${mapConfig.scaleControlConfig.position.positionName}', " +
                        "metric: ${mapConfig.scaleControlConfig.metric}, " +
                        "imperial: ${!mapConfig.scaleControlConfig.metric}})" +
                        ".addTo(myMap);"
            )
        }

        // execute script for zoom control definition
        if (mapConfig.zoomControlConfig.show) {
            execScript(
                "L.control.zoom({position: '${mapConfig.zoomControlConfig.position.positionName}'})" +
                        ".addTo(myMap);"
            )
        }
    }

    /**
     * Sets the view of the map to the specified geographical center position and zoom level.
     *
     * @param position map center position
     * @param zoomLevel zoom level (0 - 19 for OpenStreetMap)
     */
    fun setView(position: LatLong, zoomLevel: Int) =
        execScript("myMap.setView([${position.latitude}, ${position.longitude}], $zoomLevel);")

    /**
     * Pans the map to the specified geographical center position.
     *
     * @param position map center position
     */
    fun panTo(position: LatLong) =
        execScript("myMap.panTo([${position.latitude}, ${position.longitude}]);")

    /**
     * Sets the zoom of the map to the specified level.
     *
     * @param zoomLevel zoom level (0 - 19 for OpenStreetMap)
     */
    fun setZoom(zoomLevel: Int) = execScript("myMap.setZoom([$zoomLevel]);")

    /**
     * Sets a marker at the specified geographical position.
     *
     * @param position marker position
     * @param title marker title shown in tooltip (pass empty string when tooltip not needed)
     * @param marker marker to set
     * @param zIndexOffset zIndexOffset (higher number means on top)
     * @return variable name of the created marker
     */
    fun addMarker(position: LatLong, title: String, marker: Marker, zIndexOffset: Int): String {
        val varName = "marker${varNameSuffix++}"

        execScript(
            "var $varName = L.marker([${position.latitude}, ${position.longitude}], "
                    + "{title: '$title', icon: ${marker.iconName}, zIndexOffset: $zIndexOffset}).addTo(myMap);"
        )
        return varName;
    }

    /**
     * Moves the existing marker specified by the variable name to the new geographical position.
     *
     * @param markerName variable name of the marker
     * @param position new marker position
     */
    fun moveMarker(markerName: String, position: LatLong) {
        execScript("$markerName.setLatLng([${position.latitude}, ${position.longitude}]);")
    }

    /**
     * Removes the existing marker specified by the variable name.
     *
     * @param markerName variable name of the marker
     */
    fun removeMarker(markerName: String) {
        execScript("myMap.removeLayer($markerName);")
    }

    /**
     * Draws a track path along the specified positions in the color red and zooms the map to fit the track perfectly.
     *
     * @param positions list of track positions
     * @return variable name of the created track
     */
    fun addTrack(positions: List<LatLong>): String {
        val varName = "track${varNameSuffix++}"

        val jsPositions = positions
            .map { "    [${it.latitude}, ${it.longitude}]" }
            .joinToString(", \n")

        execScript(
            """
            |var latLngs = [
            |$jsPositions
            |];

            |var $varName = L.polyline(latLngs, {color: 'red', weight: 2}).addTo(myMap);
            |myMap.fitBounds($varName.getBounds());""".trimMargin()
        )
        return varName;
    }

    /**
     * Remove all current markers and tracks from the displayed map.
     */
    fun clearMarkersAndTracks() {
        execScript(
            """
            |for (i in myMap._layers) {
            |   if (myMap._layers[i] instanceof L.Marker || myMap._layers[i] instanceof L.Path) {
            |       myMap.removeLayer(myMap._layers[i]);
            |   }
            |}""".trimMargin()
        )
    }

    /**
     * Executes the specified JavaScript code inside the WebView browser component.
     *
     * @param script JavaScript code
     */
    private fun execScript(script: String) = webEngine.executeScript(script)
}
