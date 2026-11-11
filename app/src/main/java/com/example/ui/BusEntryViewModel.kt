package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BusEntry
import com.example.data.BusEntryRepository
import com.example.util.ExcelReportGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EntryFormState(
    val date: String = getTodayDateString(),
    val shift: String = "First",         // "First", "Day", "Night"
    val tripType: String = "Pickup",     // "Pickup" or "Drop"
    val contract: String = "NRL",        // "NRL" or "SBLT"
    val busNo: String = "",
    val driverName: String = "",
    val routeNo: String = "",
    val routeName: String = "",
    val startingKm: String = "",         // Starting KM
    val startingTime: String = "",       // Starting Time
    val endingKm: String = "",           // Ending KM
    val endingTime: String = "",         // Ending Time
    val runningKm: Double = 0.0,         // Auto Calculated: Ending KM - Starting KM
    val remarks: String = "",
    val autoLookupInfo: String? = null,
    val isExistingRecord: Boolean = false,
    val errorMessage: String? = null
)

fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentTimeString(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date())
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    object EntrySaved : UiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class BusEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BusEntryRepository

    private val _formState = MutableStateFlow(EntryFormState())
    val formState: StateFlow<EntryFormState> = _formState.asStateFlow()

    private val _selectedReportDate = MutableStateFlow(getTodayDateString())
    val selectedReportDate: StateFlow<String> = _selectedReportDate.asStateFlow()

    // Filters for Share Screen:
    private val _selectedShiftFilter = MutableStateFlow("ALL")       // "ALL", "First", "Day", "Night"
    val selectedShiftFilter: StateFlow<String> = _selectedShiftFilter.asStateFlow()

    private val _selectedTripFilter = MutableStateFlow("ALL")        // "ALL", "Pickup", "Drop"
    val selectedTripFilter: StateFlow<String> = _selectedTripFilter.asStateFlow()

    private val _selectedContractFilter = MutableStateFlow("ALL")    // "ALL", "NRL", "SBLT"
    val selectedContractFilter: StateFlow<String> = _selectedContractFilter.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    val distinctDates: StateFlow<List<String>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val entriesForSelectedDate: StateFlow<List<BusEntry>>

    val allMonthEntries: StateFlow<List<BusEntry>>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = BusEntryRepository(database.busEntryDao())

        distinctDates = repository.distinctDates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(getTodayDateString())
        )

        entriesForSelectedDate = _selectedReportDate.flatMapLatest { date ->
            repository.getEntriesByDate(date)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // All entries for current month to calculate monthly running KM
        val currentMonth = getTodayDateString().take(7) // "YYYY-MM"
        allMonthEntries = repository.getEntriesByMonth(currentMonth).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun onDateChange(newDate: String) {
        _formState.update { it.copy(date = newDate, errorMessage = null) }
        checkVehicleLookup(_formState.value.busNo, newDate, _formState.value.shift, _formState.value.tripType)
    }

    fun onShiftChange(newShift: String) {
        _formState.update { it.copy(shift = newShift, errorMessage = null) }
        checkVehicleLookup(_formState.value.busNo, _formState.value.date, newShift, _formState.value.tripType)
    }

    fun onTripTypeChange(newTripType: String) {
        _formState.update { it.copy(tripType = newTripType, errorMessage = null) }
        checkVehicleLookup(_formState.value.busNo, _formState.value.date, _formState.value.shift, newTripType)
    }

    fun onContractChange(newContract: String) {
        _formState.update { it.copy(contract = newContract, errorMessage = null) }
    }

    fun onBusNoChange(newBusNo: String) {
        _formState.update { it.copy(busNo = newBusNo, errorMessage = null) }
        checkVehicleLookup(newBusNo, _formState.value.date, _formState.value.shift, _formState.value.tripType)
    }

    /**
     * Intelligent vehicle lookup across trips & shifts:
     * Handles: "the bus was drop running after not return to the transport its possible
     * to directly pickup other shift need to calculate this data properly"
     */
    private fun checkVehicleLookup(busNo: String, date: String, shift: String, tripType: String) {
        val cleanBusNo = busNo.trim()
        if (cleanBusNo.length < 3) {
            _formState.update {
                it.copy(
                    isExistingRecord = false,
                    autoLookupInfo = null
                )
            }
            return
        }

        viewModelScope.launch {
            // 1. Check if exact record exists in current date, shift, and trip
            val existing = repository.findSpecificEntry(date, shift, tripType, cleanBusNo)
            if (existing != null) {
                _formState.update {
                    it.copy(
                        driverName = existing.driverName,
                        routeNo = existing.routeNo,
                        routeName = existing.routeName,
                        contract = existing.contract,
                        startingKm = existing.startingKm,
                        startingTime = existing.startingTime,
                        endingKm = existing.endingKm,
                        endingTime = existing.endingTime,
                        runningKm = existing.runningKm,
                        remarks = existing.remarks,
                        isExistingRecord = true,
                        autoLookupInfo = "Existing $shift Shift $tripType entry loaded.",
                        errorMessage = null
                    )
                }
                return@launch
            }

            // 2. Check the most recent trip of this bus across ANY shift or date
            val latest = repository.findLatestTripForBus(cleanBusNo)
            if (latest != null) {
                // If the vehicle had an ending KM, pre-fill as starting KM (direct pickup after drop without return)
                val prefilledStartKm = if (latest.endingKm.isNotBlank()) latest.endingKm else itStartingKm()
                val startVal = prefilledStartKm.toDoubleOrNull() ?: 0.0
                val endVal = _formState.value.endingKm.toDoubleOrNull() ?: 0.0
                val running = if (endVal >= startVal && startVal > 0.0) endVal - startVal else 0.0

                val note = if (latest.endingKm.isNotBlank()) {
                    "✓ Direct connection: Last ended at ${latest.endingKm} KM (${latest.shift} ${latest.tripType}). Starting KM pre-filled!"
                } else {
                    "Auto-filled Driver, Route & Contract from history."
                }

                _formState.update {
                    it.copy(
                        driverName = if (it.driverName.isBlank()) latest.driverName else it.driverName,
                        routeNo = if (it.routeNo.isBlank()) latest.routeNo else it.routeNo,
                        routeName = if (it.routeName.isBlank()) latest.routeName else it.routeName,
                        contract = latest.contract,
                        startingKm = if (it.startingKm.isBlank()) prefilledStartKm else it.startingKm,
                        runningKm = running,
                        isExistingRecord = false,
                        autoLookupInfo = note,
                        errorMessage = null
                    )
                }
            } else {
                _formState.update {
                    it.copy(
                        isExistingRecord = false,
                        autoLookupInfo = null
                    )
                }
            }
        }
    }

    private fun itStartingKm(): String = _formState.value.startingKm

    fun onDriverNameChange(newDriver: String) {
        _formState.update { it.copy(driverName = newDriver, errorMessage = null) }
    }

    fun onRouteNoChange(newRouteNo: String) {
        _formState.update { it.copy(routeNo = newRouteNo, errorMessage = null) }
    }

    fun onRouteNameChange(newRouteName: String) {
        _formState.update { it.copy(routeName = newRouteName, errorMessage = null) }
    }

    fun onStartingKmChange(newKm: String) {
        _formState.update { current ->
            val startVal = newKm.toDoubleOrNull() ?: 0.0
            val endVal = current.endingKm.toDoubleOrNull() ?: 0.0
            val running = if (endVal >= startVal && startVal > 0.0) endVal - startVal else 0.0
            current.copy(
                startingKm = newKm,
                runningKm = running,
                errorMessage = null
            )
        }
    }

    fun onStartingTimeChange(newTime: String) {
        _formState.update { it.copy(startingTime = newTime, errorMessage = null) }
    }

    fun onEndingKmChange(newKm: String) {
        _formState.update { current ->
            val endVal = newKm.toDoubleOrNull() ?: 0.0
            val startVal = current.startingKm.toDoubleOrNull() ?: 0.0
            val running = if (endVal >= startVal && startVal > 0.0) endVal - startVal else 0.0
            current.copy(
                endingKm = newKm,
                runningKm = running,
                errorMessage = null
            )
        }
    }

    fun onEndingTimeChange(newTime: String) {
        _formState.update { it.copy(endingTime = newTime, errorMessage = null) }
    }

    fun onRemarksChange(newRemarks: String) {
        _formState.update { it.copy(remarks = newRemarks) }
    }

    fun setStartingTimeToNow() {
        onStartingTimeChange(getCurrentTimeString())
    }

    fun setEndingTimeToNow() {
        onEndingTimeChange(getCurrentTimeString())
    }

    fun onReportDateChange(newDate: String) {
        _selectedReportDate.value = newDate
    }

    fun onShiftFilterChange(filter: String) {
        _selectedShiftFilter.value = filter
    }

    fun onTripFilterChange(filter: String) {
        _selectedTripFilter.value = filter
    }

    fun onContractFilterChange(filter: String) {
        _selectedContractFilter.value = filter
    }

    fun saveEntry() {
        val state = _formState.value

        if (state.busNo.isBlank()) {
            val error = "Please enter Bus No"
            _formState.update { it.copy(errorMessage = error) }
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError(error)) }
            return
        }

        if (state.driverName.isBlank()) {
            val error = "Please enter Driver Name"
            _formState.update { it.copy(errorMessage = error) }
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError(error)) }
            return
        }

        if (state.routeNo.isBlank()) {
            val error = "Please enter Route No"
            _formState.update { it.copy(errorMessage = error) }
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError(error)) }
            return
        }

        if (state.startingKm.isBlank()) {
            val error = "Please enter Starting KM"
            _formState.update { it.copy(errorMessage = error) }
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError(error)) }
            return
        }

        if (state.startingTime.isBlank()) {
            val error = "Please enter Starting Time"
            _formState.update { it.copy(errorMessage = error) }
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError(error)) }
            return
        }

        if (state.endingKm.isBlank()) {
            val error = "Please enter Ending KM"
            _formState.update { it.copy(errorMessage = error) }
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError(error)) }
            return
        }

        if (state.endingTime.isBlank()) {
            val error = "Please enter Ending Time"
            _formState.update { it.copy(errorMessage = error) }
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError(error)) }
            return
        }

        val startVal = state.startingKm.toDoubleOrNull() ?: 0.0
        val endVal = state.endingKm.toDoubleOrNull() ?: 0.0
        val calculatedRunningKm = if (endVal >= startVal) endVal - startVal else 0.0

        val entry = BusEntry(
            date = state.date.trim(),
            shift = state.shift.trim(),
            tripType = state.tripType.trim(),
            contract = state.contract.trim(),
            busNo = state.busNo.trim().uppercase(Locale.getDefault()),
            driverName = state.driverName.trim(),
            routeNo = state.routeNo.trim().uppercase(Locale.getDefault()),
            routeName = state.routeName.trim(),
            startingKm = state.startingKm.trim(),
            startingTime = state.startingTime.trim(),
            endingKm = state.endingKm.trim(),
            endingTime = state.endingTime.trim(),
            runningKm = calculatedRunningKm,
            remarks = state.remarks.trim()
        )

        viewModelScope.launch {
            repository.insertOrUpdate(entry)
            _selectedReportDate.value = entry.date

            val successMsg = if (state.isExistingRecord) {
                "Updated ${entry.shift} Shift ${entry.tripType} for ${entry.busNo} (${calculatedRunningKm} KM)"
            } else {
                "Saved ${entry.shift} Shift ${entry.tripType} for ${entry.busNo} (${calculatedRunningKm} KM)"
            }
            _uiEvents.emit(UiEvent.ShowToast(successMsg))
            _uiEvents.emit(UiEvent.EntrySaved)

            // Reset form for next entry while preserving Date, Shift, Trip, and Contract
            _formState.update {
                it.copy(
                    busNo = "",
                    driverName = "",
                    routeNo = "",
                    routeName = "",
                    startingKm = "",
                    startingTime = "",
                    endingKm = "",
                    endingTime = "",
                    runningKm = 0.0,
                    remarks = "",
                    isExistingRecord = false,
                    autoLookupInfo = null,
                    errorMessage = null
                )
            }
        }
    }

    fun clearForm() {
        _formState.update {
            it.copy(
                busNo = "",
                driverName = "",
                routeNo = "",
                routeName = "",
                startingKm = "",
                startingTime = "",
                endingKm = "",
                endingTime = "",
                runningKm = 0.0,
                remarks = "",
                isExistingRecord = false,
                autoLookupInfo = null,
                errorMessage = null
            )
        }
    }

    fun deleteEntry(entry: BusEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            _uiEvents.emit(UiEvent.ShowToast("Record deleted"))
        }
    }
}
