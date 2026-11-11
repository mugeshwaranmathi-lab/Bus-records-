package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus_entries")
data class BusEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,              // e.g. "2026-11-11"
    val shift: String,             // "First", "Day", "Night"
    val tripType: String,          // "Pickup" or "Drop"
    val contract: String = "NRL",  // "NRL" or "SBLT"
    val busNo: String,
    val driverName: String,
    val routeNo: String,
    val routeName: String,
    val startingKm: String = "",   // Starting KM
    val startingTime: String = "", // Starting Time
    val endingKm: String = "",     // Ending KM
    val endingTime: String = "",   // Ending Time
    val runningKm: Double = 0.0,   // Auto calculated: Ending KM - Starting KM
    val remarks: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
