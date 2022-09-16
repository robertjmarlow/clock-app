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
    fun `Get current date and time in a time zone that doesn't exist`() {
        val badTimeZoneStr = "The/Moon"

        val responseStr = mockMvc.perform(get("/api/time").queryParam("tz", badTimeZoneStr))
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .errorMessage

        assertEquals("Time zone \"The/Moon\" not recognized", responseStr)
    }
}
