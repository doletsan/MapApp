package com.example.maps

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // States for removing, adding, or doing nothing when touching markers
    private val DELETE_MARKER = -1
    private val NEUTRAL_MARKER = 0
    private val ADD_MARKER = 1
    private var stateRecordPoint = NEUTRAL_MARKER

    // Tracks current markers on map
    private var currentListMarkers: MutableList<Marker> = mutableListOf()

    // Creates menu and menu items on action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.my_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // Decision function for when a menu item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Closes current activity, switches to the new one
        finish()

        // Switch case depending on id
        when (item.itemId) {
            R.id.item_view -> this.startActivity(Intent(this, Table::class.java)) // Change to table view
            R.id.item_info -> this.startActivity(Intent(this, Info::class.java)) // Change to info view
        }

        return super.onOptionsItemSelected(item)
    }

    // When activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    // Requires Google Play services
    // Triggered when map is read to use
    // Encapsulates the map interface and buttons
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enables compass when direction is not North
        mMap.uiSettings.isCompassEnabled = true

        // Move camera when short pressing marker
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker): Boolean {

                mMap.moveCamera(CameraUpdateFactory.newLatLng(p0.position))

                // Confirm whether user would like to delete marker
                if (stateRecordPoint == DELETE_MARKER) {

                    val givenMarkerID: Int = p0.tag as Int
                    deleteConfirmationDialog(givenMarkerID)

                }

                return false
            }
        })

        // Add Map listener to grab location of map click when button is pressed
        mMap.setOnMapClickListener(object : GoogleMap.OnMapClickListener {

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onMapClick(location: LatLng) {

                // If current state is ADD, get LatLng object and User input to create
                if (stateRecordPoint == ADD_MARKER) {

                    // Create popup form to fill in
                    // https://android--code.blogspot.com/2018/02/android-kotlin-popup-window-example.html
                    val inflater: LayoutInflater =
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                    val newView = inflater.inflate(R.layout.marker_form, null)
                    val popupWindow = PopupWindow(
                        newView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    val buttonCancel = newView.findViewById<Button>(R.id.btn_cancel)
                    val buttonSubmit = newView.findViewById<Button>(R.id.btn_submit)
                    val textName = newView.findViewById<EditText>(R.id.text_name)
                    val textDesc = newView.findViewById<EditText>(R.id.text_desc)

                    // Submit adds user input into database
                    buttonSubmit.setOnClickListener {

                        val inputName = textName.text.toString()
                        val inputDesc = textDesc.text.toString()
                        val lat = location.latitude
                        val lng = location.longitude

                        // Check if user provides input for all text boxes
                        if (inputName.isNotEmpty() && inputDesc.isNotEmpty()) {

                            // Create new MapMarker object to store into DB
                            val newMarker = MapMarker(inputName, inputDesc, lat, lng)
                            val db = MapDBHandler(applicationContext)
                            val result = db.insertData(newMarker)

                            // Check if MapMarker has been added to table
                            if (result >= 0) {

                                // Get ID from table
                                val id = db.getIndex(result).id

                                // Place marker on map
                                placeMarkerOnMap(id, inputName, inputDesc, LatLng(lat, lng))
                                makeToast("Successful")

                            } else {

                                makeToast("Failed")

                            }

                            popupWindow.dismiss()

                        } else {

                            makeToast("Please fill all entries")

                        }
                    }

                    // Close popup if cancel
                    buttonCancel.setOnClickListener {
                        popupWindow.dismiss()
                        makeToast("Closed")
                    }

                    // Show popup
                    TransitionManager.beginDelayedTransition(root_layout)
                    popupWindow.showAtLocation(
                        root_layout,
                        Gravity.CENTER,
                        0,
                        0
                    )
                    popupWindow.isFocusable = true
                    popupWindow.update()

                    //makeToast("The toast is toastered")
                }
            }
        })

        // Triple state button depending on what user wants to do
        // DELETE -> NEUTRAL -> ADD -> DELETE
        val stateButton = findViewById<Button>(R.id.btn_state)
        stateButton.setOnClickListener {
            if (stateRecordPoint == DELETE_MARKER) {

                stateRecordPoint = NEUTRAL_MARKER
                val colourVal = ContextCompat.getColor(applicationContext, R.color.neutral_colour)
                stateButton.setBackgroundColor(colourVal)
                stateButton.setText(R.string.neutral_state)

            } else if (stateRecordPoint == NEUTRAL_MARKER) {

                stateRecordPoint = ADD_MARKER
                val colourVal = ContextCompat.getColor(applicationContext, R.color.add_colour)
                stateButton.setBackgroundColor(colourVal)
                stateButton.setText(R.string.add_state)

            } else if (stateRecordPoint == ADD_MARKER) {

                stateRecordPoint = DELETE_MARKER
                val colourVal = ContextCompat.getColor(applicationContext, R.color.delete_colour)
                stateButton.setBackgroundColor(colourVal)
                stateButton.setText(R.string.delete_state)

            }
        }

        refreshMarkers()

    }

    // Creates and adds a marker to the map
    private fun placeMarkerOnMap(markerID: Int, markerName: String, markerDesc: String, location: LatLng): Marker {

        val markerOptions = MarkerOptions()
            .title(markerName)
            .position(location)
            .snippet(markerDesc)
            .draggable(false)

        val newMarker = mMap.addMarker(markerOptions)

        // Unique identifier
        newMarker.tag = markerID

        // Keeps track of current markers on map
        currentListMarkers.add(newMarker)

        return newMarker
    }

    // Removes marker from map based on unique ID
    private fun deleteMarkerFromMap(markerID: Int) {

        var db = MapDBHandler(applicationContext)

        for (currMarker in currentListMarkers) {

            if (currMarker.tag == markerID) {

                db.deleteDataByID(currMarker.tag as Int)
                currMarker.remove()
                currentListMarkers.remove(currMarker)
                break
            }
        }

        db.close()

    }

    // Reloads markers by reading database file
    private fun refreshMarkers() {
        var db = MapDBHandler(applicationContext)

        // Reset list
        currentListMarkers = mutableListOf()
        mMap.clear()

        var listMarkers = db.showData()
        for (marker in listMarkers) {
            placeMarkerOnMap(marker.id, marker.name, marker.desc, LatLng(marker.lat, marker.lng))
        }
        db.close()

    }

    // Dialog that appears for user to confirm marker deletion
    private fun deleteConfirmationDialog(markerID: Int) {

        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Confirmation")
        builder.setMessage("Would you like to delete this marker?")

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> deleteMarkerFromMap(markerID)
                //DialogInterface.BUTTON_NEGATIVE -> reply = false
                //DialogInterface.BUTTON_NEUTRAL -> reply = false
            }
        }

        builder.setPositiveButton("YES", dialogClickListener)
        builder.setNegativeButton("NO", dialogClickListener)
        builder.setNeutralButton("CANCEL", dialogClickListener)

        dialog = builder.create()

        dialog.show()

    }

    // Makes toast message :)
    private fun makeToast(message: String) {
        val newToast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        newToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        newToast.show()
    }

}