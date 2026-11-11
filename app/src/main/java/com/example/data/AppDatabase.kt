package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(entities = [BusEntry::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busEntryDao(): BusEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bus_entry_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.busEntryDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: BusEntryDao) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Seed realistic records for Ashok Leyland - Ennore
            // Shows NRL & SBLT contracts and First, Day, Night shifts
            dao.insert(
                BusEntry(
                    date = today,
                    shift = "First",
                    tripType = "Pickup",
                    contract = "NRL",
                    busNo = "TN-04-AL-1001",
                    driverName = "K. Murugan",
                    routeNo = "R-01",
                    routeName = "Tiruvottiyur to Gate 1",
                    startingKm = "14220",
                    startingTime = "05:30 AM",
                    endingKm = "14250",
                    endingTime = "06:45 AM",
                    runningKm = 30.0,
                    remarks = "Morning pickup arrival OK"
                )
            )
            dao.insert(
                BusEntry(
                    date = today,
                    shift = "First",
                    tripType = "Drop",
                    contract = "NRL",
                    busNo = "TN-04-AL-1001",
                    driverName = "K. Murugan",
                    routeNo = "R-01",
                    routeName = "Gate 1 to Tiruvottiyur",
                    startingKm = "14250",
                    startingTime = "02:00 PM",
                    endingKm = "14280",
                    endingTime = "03:15 PM",
                    runningKm = 30.0,
                    remarks = "Drop completed, waiting at terminal"
                )
            )
            // The bus did not return to transport, directly did Day Shift Pickup:
            dao.insert(
                BusEntry(
                    date = today,
                    shift = "Day",
                    tripType = "Pickup",
                    contract = "NRL",
                    busNo = "TN-04-AL-1001",
                    driverName = "K. Murugan",
                    routeNo = "R-01",
                    routeName = "Tiruvottiyur to Gate 1",
                    startingKm = "14280",
                    startingTime = "03:45 PM",
                    endingKm = "14310",
                    endingTime = "04:50 PM",
                    runningKm = 30.0,
                    remarks = "Direct pickup without returning to transport"
                )
            )
            dao.insert(
                BusEntry(
                    date = today,
                    shift = "First",
                    tripType = "Pickup",
                    contract = "SBLT",
                    busNo = "TN-04-AL-1002",
                    driverName = "S. Ramesh",
                    routeNo = "R-02",
                    routeName = "Madhavaram to Gate 2",
                    startingKm = "28070",
                    startingTime = "05:45 AM",
                    endingKm = "28100",
                    endingTime = "07:00 AM",
                    runningKm = 30.0,
                    remarks = "Gate 2 check completed"
                )
            )
            dao.insert(
                BusEntry(
                    date = today,
                    shift = "Day",
                    tripType = "Drop",
                    contract = "SBLT",
                    busNo = "TN-04-AL-1005",
                    driverName = "M. Senthil",
                    routeNo = "R-03",
                    routeName = "Plant to Minjur",
                    startingKm = "31200",
                    startingTime = "05:30 PM",
                    endingKm = "31235",
                    endingTime = "06:40 PM",
                    runningKm = 35.0,
                    remarks = "Day shift drop OK"
                )
            )
            dao.insert(
                BusEntry(
                    date = today,
                    shift = "Night",
                    tripType = "Pickup",
                    contract = "NRL",
                    busNo = "TN-04-AL-1007",
                    driverName = "R. Anand",
                    routeNo = "R-04",
                    routeName = "Gummidipoondi to Gate 1",
                    startingKm = "51965",
                    startingTime = "09:10 PM",
                    endingKm = "52000",
                    endingTime = "10:20 PM",
                    runningKm = 35.0,
                    remarks = "Night shift arrival on time"
                )
            )
        }
    }
}
