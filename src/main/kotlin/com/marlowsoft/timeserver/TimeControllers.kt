package com.marlowsoft.timeserver

import com.google.common.collect.ImmutableSet
import com.marlowsoft.timeserver.obj.TimeResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.TimeZone

@RestController
@RequestMapping("/api")
class TimeControllers {
    companion object {
        val KNOWN_TIMEZONES: Set<String> = ImmutableSet.copyOf(TimeZone.getAvailableIDs())
    }

    // dumb stuff to add:
    //  get time in the past or future
    @GetMapping("/time")
    fun getTime(@RequestParam tz: String?): TimeResponse {
        if (tz != null) {
            if (!KNOWN_TIMEZONES.contains(tz)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Time zone \"%s\" not recognized", tz))
            }
            val zoneId = TimeZone.getTimeZone(tz).toZoneId()
            return TimeResponse(ZonedDateTime.now(zoneId))
        }
        return TimeResponse(ZonedDateTime.now())
    }
}
