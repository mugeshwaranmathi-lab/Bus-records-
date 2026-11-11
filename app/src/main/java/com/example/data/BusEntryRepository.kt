package com.example.data

import kotlinx.coroutines.flow.Flow

class BusEntryRepository(private val dao: BusEntryDao) {

    val allEntries: Flow<List<BusEntry>> = dao.getAllEntries()
    val distinctDates: Flow<List<String>> = dao.getDistinctDates()
    val allBusNumbers: Flow<List<String>> = dao.getAllBusNumbers()

    fun getEntriesByDate(date: String): Flow<List<BusEntry>> = dao.getEntriesByDate(date)

    fun getEntriesByMonth(monthPrefix: String): Flow<List<BusEntry>> = dao.getEntriesByMonth(monthPrefix)

    suspend fun findSpecificEntry(
        date: String,
        shift: String,
        tripType: String,
        busNo: String
    ): BusEntry? = dao.findSpecificEntry(date, shift, tripType, busNo)

    suspend fun findLatestTripForBus(busNo: String): BusEntry? = dao.findLatestTripForBus(busNo)

    suspend fun insertOrUpdate(entry: BusEntry): Long {
        val existing = dao.findSpecificEntry(entry.date, entry.shift, entry.tripType, entry.busNo)
        return if (existing != null) {
            val updated = entry.copy(id = existing.id, updatedAt = System.currentTimeMillis())
            dao.update(updated)
            existing.id
        } else {
            dao.insert(entry)
        }
    }

    suspend fun delete(entry: BusEntry) = dao.delete(entry)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
