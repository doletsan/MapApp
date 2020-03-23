package com.example.maps

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import android.content.Context

const val DATABASE_NAME = "DemoDB"
const val TABLE_NAME = "Markers"
const val COL_ID = "ID"
const val COL_NAME = "Name"
const val COL_DESC = "Description"
const val COL_LAT = "Latitude"
const val COL_LNG = "Longitude"

class MapDBHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1){

    override fun onCreate(db: SQLiteDatabase?){
        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_NAME + " VARCHAR(256)," +
                COL_DESC + " VARCHAR(256)," +
                COL_LAT + " FLOAT," +
                COL_LNG + " FLOAT);"

        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Returns true if successful
    fun insertData(marker: MapMarker): Int{

        val db = this.writableDatabase
        var cv = ContentValues()

        cv.put(COL_NAME, marker.name)
        cv.put(COL_DESC, marker.desc)
        cv.put(COL_LAT, marker.lat)
        cv.put(COL_LNG, marker.lng)

        val result = db.insert(TABLE_NAME, null, cv)

        db.close()

        // Returns -1 if the result could not be added into the database
        //return result != -1.toLong()
        return result.toInt()
    }

    fun getIndex(i: Int): MapMarker{

        val db = this.readableDatabase
        val query = "Select * FROM $TABLE_NAME WHERE $COL_ID = $i"
        val result = db.rawQuery(query, null)

        var returnMarker: MapMarker = MapMarker()

        // Cursor moves
        if (result.moveToFirst()){
            do {

                returnMarker.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                returnMarker.name = result.getString(result.getColumnIndex(COL_NAME))
                returnMarker.desc = result.getString(result.getColumnIndex(COL_DESC))
                returnMarker.lat = result.getString(result.getColumnIndex(COL_LAT)).toDouble()
                returnMarker.lng = result.getString(result.getColumnIndex(COL_LNG)).toDouble()

            } while (result.moveToNext())
        }
        result.close()
        db.close()

        return returnMarker
    }

    fun deleteData(marker: MapMarker){

        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(marker.id.toString()))
        db.close()

    }

    fun deleteDataByID(markerID: Int){

        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(markerID.toString()))
        db.close()
    }

    fun showData(): MutableList<MapMarker>{

        var markerList: MutableList<MapMarker> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * FROM $TABLE_NAME"
        val result = db.rawQuery(query, null)

        // Cursor moves
        if (result.moveToFirst()){
            do {
                var newMarker = MapMarker()
                newMarker.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                newMarker.name = result.getString(result.getColumnIndex(COL_NAME))
                newMarker.desc = result.getString(result.getColumnIndex(COL_DESC))
                newMarker.lat = result.getString(result.getColumnIndex(COL_LAT)).toDouble()
                newMarker.lng = result.getString(result.getColumnIndex(COL_LNG)).toDouble()

                markerList.add(newMarker)
            } while (result.moveToNext())
        }
        result.close()
        db.close()

        return markerList
    }

}

// Show data in table
// Show a small menu blurb about how to use
// Save and add all points to table