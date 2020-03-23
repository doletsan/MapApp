package com.example.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class Table : AppCompatActivity() {

    // List of strings for creating new rows
    private val TABLE_COL = listOf<Int>(R.string.table_id, R.string.table_name, R.string.table_desc, R.string.table_lat, R.string.table_lng)

    // Creates menu and menu items on action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.my_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // Decision function for when a menu item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        finish()

        when (item.itemId){
            R.id.item_view -> this.startActivity(Intent(this, MapsActivity::class.java))
            R.id.item_info -> this.startActivity(Intent(this, Info::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    // Creates activity and loads in SQL table
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_table)

        loadTable()
    }

    // Clears table of previous entries, leaves header and title
    private fun resetTable(){

        val title = findViewById<TextView>(R.id.table_title)
        val header = findViewById<TableRow>(R.id.table_header)

        val layout = findViewById<TableLayout>(R.id.table_layout)
        layout.removeAllViews()

        layout.addView(title)
        layout.addView(header)

    }

    // Add a new row based on MapMarker information
    private fun addRow(marker: MapMarker, colour: Int = ContextCompat.getColor(applicationContext, R.color.table_default)){

        var existingTable = findViewById<TableLayout>(R.id.table_layout)
        var newRow = TableRow(this)

        newRow.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)

        newRow.setBackgroundColor(colour)

        for (col in TABLE_COL){

            var newText = TextView(this)

            var weight: Float = 0F
            var text: String = ""

            when(col){
                R.string.table_id -> {
                    weight = 1F
                    text = marker.id.toString()
                }
                R.string.table_name -> {
                    weight = 3F
                    text = marker.name.toString()
                }
                R.string.table_desc -> {
                    weight = 4F
                    text = marker.desc.toString()
                }
                R.string.table_lat -> {
                    weight = 2F
                    text = marker.lat.toString()
                }
                R.string.table_lng -> {
                    weight = 2F
                    text = marker.lng.toString()
                }
            }


            newText.setText(R.string.table_lng)
            newText.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                weight
            )
            newText.gravity = Gravity.CENTER
            newText.text = text

            newRow.addView(newText)

        }

        existingTable.addView(newRow)

    }

    // Load marker data from SQL table and display it in a table
    private fun loadTable(){

        resetTable()

        val db = MapDBHandler(applicationContext)
        val listMarkers = db.showData()

        // true for default colour, false for secondary
        var currColour = true

        for (marker in listMarkers){
            when (currColour){
                true -> {
                    addRow(marker)
                    currColour = false
                }
                false -> {
                    addRow(marker,  ContextCompat.getColor(applicationContext, R.color.table_second))
                    currColour = true
                }
            }
        }
    }
}
