package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BusEntry): Long

    @Update
    suspend fun update(entry: BusEntry)

    @Delete
    suspend fun delete(entry: BusEntry)

    @Query("DELETE FROM bus_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM bus_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<BusEntry>>

    @Query("SELECT * FROM bus_entries WHERE date = :date ORDER BY shift ASC, tripType ASC, id ASC")
    fun getEntriesByDate(date: String): Flow<List<BusEntry>>

    @Query("SELECT * FROM bus_entries WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC, id ASC")
    fun getEntriesByMonth(monthPrefix: String): Flow<List<BusEntry>>

    @Query("SELECT * FROM bus_entries WHERE date = :date AND shift = :shift AND tripType = :tripType AND UPPER(busNo) = UPPER(:busNo) LIMIT 1")
    suspend fun findSpecificEntry(date: String, shift: String, tripType: String, busNo: String): BusEntry?

    // Finds the most recent trip for this vehicle across ANY shift or date (to chain Drop Ending KM into next Pickup Starting KM)
    @Query("SELECT * FROM bus_entries WHERE UPPER(busNo) = UPPER(:busNo) ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun findLatestTripForBus(busNo: String): BusEntry?

    @Query("SELECT DISTINCT busNo FROM bus_entries ORDER BY busNo ASC")
    fun getAllBusNumbers(): Flow<List<String>>

    @Query("SELECT DISTINCT date FROM bus_entries ORDER BY date DESC")
    fun getDistinctDates(): Flow<List<String>>
}
