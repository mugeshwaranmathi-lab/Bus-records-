package com.example

import com.example.data.BusEntry
import com.example.util.ExcelReportGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BusEntryReportTest {

    @Test
    fun testReportSectionsStructureWithFirstDayNightShifts() {
        val entries = listOf(
            BusEntry(
                date = "2026-11-11",
                shift = "First",
                tripType = "Pickup",
                contract = "NRL",
                busNo = "TN-04-AL-1001",
                driverName = "K. Murugan",
                routeNo = "R-01",
                routeName = "Tiruvottiyur",
                startingKm = "14220",
                startingTime = "05:30 AM",
                endingKm = "14250",
                endingTime = "06:45 AM",
                runningKm = 30.0
            ),
            BusEntry(
                date = "2026-11-11",
                shift = "First",
                tripType = "Drop",
                contract = "NRL",
                busNo = "TN-04-AL-1001",
                driverName = "K. Murugan",
                routeNo = "R-01",
                routeName = "Tiruvottiyur",
                startingKm = "14250",
                startingTime = "02:00 PM",
                endingKm = "14280",
                endingTime = "03:15 PM",
                runningKm = 30.0
            ),
            BusEntry(
                date = "2026-11-11",
                shift = "Day",
                tripType = "Pickup",
                contract = "SBLT",
                busNo = "TN-04-AL-1002",
                driverName = "S. Rajesh",
                routeNo = "R-02",
                routeName = "Madhavaram",
                startingKm = "22100",
                startingTime = "07:30 AM",
                endingKm = "22135",
                endingTime = "08:45 AM",
                runningKm = 35.0
            )
        )

        val sections = ExcelReportGenerator.getSections("2026-11-11", entries)
        assertEquals(3, sections.size)

        // Section 1: First Shift Pickup NRL
        assertTrue(sections[0].heading.contains("Pickup"))
        assertTrue(sections[0].heading.contains("NRL"))
        assertTrue(sections[0].heading.contains("First Shift"))
        assertEquals(1, sections[0].items.size)
        assertEquals("TN-04-AL-1001", sections[0].items[0].busNo)
        assertEquals(30.0, sections[0].items[0].runningKm, 0.001)

        // Section 2: First Shift Drop NRL
        assertTrue(sections[1].heading.contains("Drop"))
        assertTrue(sections[1].heading.contains("NRL"))
        assertTrue(sections[1].heading.contains("First Shift"))
        assertEquals(1, sections[1].items.size)
        assertEquals(30.0, sections[1].items[0].runningKm, 0.001)

        // Section 3: Day Shift Pickup SBLT
        assertTrue(sections[2].heading.contains("Pickup"))
        assertTrue(sections[2].heading.contains("SBLT"))
        assertTrue(sections[2].heading.contains("Day Shift"))
        assertEquals(1, sections[2].items.size)
        assertEquals("TN-04-AL-1002", sections[2].items[0].busNo)
        assertEquals(35.0, sections[2].items[0].runningKm, 0.001)
    }

    @Test
    fun testMonthlyBusRunningKmSummaryCalculation() {
        val entries = listOf(
            BusEntry(
                date = "2026-11-10",
                shift = "First",
                tripType = "Pickup",
                contract = "NRL",
                busNo = "TN-04-AL-1001",
                driverName = "K. Murugan",
                routeNo = "R-01",
                routeName = "Tiruvottiyur",
                startingKm = "14220",
                startingTime = "05:30 AM",
                endingKm = "14250",
                endingTime = "06:45 AM",
                runningKm = 30.0
            ),
            BusEntry(
                date = "2026-11-10",
                shift = "First",
                tripType = "Drop",
                contract = "NRL",
                busNo = "TN-04-AL-1001",
                driverName = "K. Murugan",
                routeNo = "R-01",
                routeName = "Tiruvottiyur",
                startingKm = "14250",
                startingTime = "02:00 PM",
                endingKm = "14280",
                endingTime = "03:15 PM",
                runningKm = 30.0
            ),
            // Cross-shift direct connection: without returning to yard, directly doing Night Pickup
            BusEntry(
                date = "2026-11-10",
                shift = "Night",
                tripType = "Pickup",
                contract = "NRL",
                busNo = "TN-04-AL-1001",
                driverName = "K. Murugan",
                routeNo = "R-01",
                routeName = "Tiruvottiyur",
                startingKm = "14280",
                startingTime = "08:30 PM",
                endingKm = "14310",
                endingTime = "09:45 PM",
                runningKm = 30.0
            )
        )

        val monthlySummary = ExcelReportGenerator.calculateMonthlyBusSummary(entries)
        assertEquals(1, monthlySummary.size)
        assertEquals("TN-04-AL-1001", monthlySummary[0].busNo)
        assertEquals(3, monthlySummary[0].totalTrips)
        assertEquals(90.0, monthlySummary[0].totalKm, 0.001)
    }
}
