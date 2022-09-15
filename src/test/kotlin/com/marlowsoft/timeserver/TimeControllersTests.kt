package com.marlowsoft.timeserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.marlowsoft.timeserver.obj.TimeResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
        val currentDateTime = ZonedDateTime.parse(currentDateTimeObj.timeStr)

        // assumption: the REST call isn't instantaneous
        assertTrue(now.isBefore(currentDateTime))
    }
}
