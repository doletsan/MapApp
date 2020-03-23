package com.example.maps

import com.google.android.gms.maps.model.Marker

class MapMarker {

    var id: Int = 0
    var name: String = ""
    var desc: String = ""
    var lat: Double = 0.0
    var lng: Double = 0.0

    constructor(name:String, desc:String, lat: Double, lng: Double){
        this.name = name
        this.desc = desc
        this.lat = lat
        this.lng = lng
    }

    // Empty constructor for MapDBHandler
    constructor(){
    }


}