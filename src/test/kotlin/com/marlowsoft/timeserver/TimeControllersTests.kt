package com.marlowsoft.timeserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.marlowsoft.timeserver.obj.TimeResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime

@WebMvcTest
class TimeControllersTests(@Autowired val mockMvc: MockMvc) {
    private val jacksonObjectMapper: ObjectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())

    @Test
    fun `Get current date and time`() {
        val now = ZonedDateTime.now()
        val currentDateTimeJson = mockMvc.perform(get("/api/time"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .response
            .contentAsString

        val currentDateTimeObj: TimeResponse = jacksonObjectMapper.readValue(currentDateTimeJson)

        // assumption: the REST call isn't instantaneous
        assertTrue(now.isBefore(currentDateTimeObj.time))
    }

    @Test
    fun `Get current date and time in UTC time zone`() {
        val utcZoneStr = "UTC"
        val utcZoneId = ZoneId.of(utcZoneStr)
        val now = ZonedDateTime.now(utcZoneId)

        val currentDateTimeJson = mockMvc.perform(get("/api/time").queryParam("tz", utcZoneStr))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .response
            .contentAsString

        val currentDateTimeObj: TimeResponse = jacksonObjectMapper.readValue(currentDateTimeJson)
        val currentDateTime = ZonedDateTime.parse(currentDateTimeObj.timeStr)

        // assumption: the REST call isn't instantaneous
        assertTrue(now.isBefore(currentDateTime))
        assertEquals(utcZoneId, currentDateTime.zone)
    }

    @Test
    fun `Get current date and time in Japan`() {
        val tokyoZoneStr = "Asia/Tokyo"
        val tokyoZoneId = ZoneId.of(tokyoZoneStr)
        val now = ZonedDateTime.now(tokyoZoneId)

        val currentDateTimeJson = mockMvc.perform(get("/api/time").queryParam("tz", tokyoZoneStr))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .response
            .contentAsString

        val currentDateTimeObj: TimeResponse = jacksonObjectMapper.readValue(currentDateTimeJson)
        val currentDateTime = ZonedDateTime.parse(currentDateTimeObj.timeStr)

        // assumption: the REST call isn't instantaneous
        assertTrue(now.isBefore(currentDateTime))
        assertEquals(tokyoZoneId, currentDateTime.zone)
    }

    @Test
    fun `Get current date and time but a month in the past`() {
        val nowButAMonthAgo = ZonedDateTime.now().minus(Period.ofMonths(1))

        val aMonthAgoDateTimeJson = mockMvc.perform(get("/api/time").queryParam("past", "1M"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .response
            .contentAsString

        val aMonthAgoDateTimeObj: TimeResponse = jacksonObjectMapper.readValue(aMonthAgoDateTimeJson)
        val aMonthAgoDateTime = ZonedDateTime.parse(aMonthAgoDateTimeObj.timeStr)

        assertTrue(nowButAMonthAgo.isBefore(aMonthAgoDateTime))
    }

    @Test
    fun `Get current date and time but a month in the future`() {
        val nowButAMonthInTheFuture = ZonedDateTime.now().plus(Period.ofMonths(1))

        val aMonthInTheFutureDateTimeJson = mockMvc.perform(get("/api/time").queryParam("future", "1M"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .response
            .contentAsString

        val aMonthInTheFutureDateTimeObj: TimeResponse = jacksonObjectMapper.readValue(aMonthInTheFutureDateTimeJson)
        val aMonthInTheFutureDateTime = ZonedDateTime.parse(aMonthInTheFutureDateTimeObj.timeStr)

        assertTrue(nowButAMonthInTheFuture.isBefore(aMonthInTheFutureDateTime))
    }

    @Test
    fun `Get current date and time but a year in the past in Japan`() {
        val tokyoZoneStr = "Asia/Tokyo"
        val tokyoZoneId = ZoneId.of(tokyoZoneStr)
        val nowButAYearAgo = ZonedDateTime.now(tokyoZoneId).minus(Period.ofYears(1))

        val aYearAgoDateTimeJson = mockMvc.perform(get("/api/time").queryParam("tz", tokyoZoneStr).queryParam("past", "1Y"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .response
            .contentAsString

        val aYearAgoDateTimeObj: TimeResponse = jacksonObjectMapper.readValue(aYearAgoDateTimeJson)
        val aYearAgoDateTime = ZonedDateTime.parse(aYearAgoDateTimeObj.timeStr)

        // assumption: the REST call isn't instantaneous
        assertTrue(nowButAYearAgo.isBefore(aYearAgoDateTime))
        assertEquals(tokyoZoneId, aYearAgoDateTime.zone)
    }

    @Test
    fun `Get current date and time in a time zone that doesn't exist`() {
        val badTimeZoneStr = "The/Moon"

        val responseStr = mockMvc.perform(get("/api/time").queryParam("tz", badTimeZoneStr))
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .errorMessage

        assertEquals("Time zone \"The/Moon\" not recognized", responseStr)
    }

    @Test
    fun `Try to set a future and past query param`() {
        val responseStr = mockMvc.perform(get("/api/time").queryParam("future", "1Y").queryParam("past", "1Y"))
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .errorMessage

        assertEquals("Either \"future\" or \"past\" can be populated, but not both", responseStr)
    }

    @Test
    fun `Try to set an invalid future query param`() {
        val responseStr = mockMvc.perform(get("/api/time").queryParam("future", "1Year"))
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .errorMessage

        assertEquals("Time format \"1Year\" not recognized for future parameter", responseStr)
    }

    @Test
    fun `Try to set an invalid past query param`() {
        val responseStr = mockMvc.perform(get("/api/time").queryParam("past", "1Year"))
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .errorMessage

        assertEquals("Time format \"1Year\" not recognized for past parameter", responseStr)
    }
}
