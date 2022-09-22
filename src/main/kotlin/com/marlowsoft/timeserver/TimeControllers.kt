package com.marlowsoft.timeserver

import com.google.common.collect.ImmutableSet
import com.marlowsoft.timeserver.obj.TimeResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Period
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.TimeZone

@RestController
@RequestMapping("/api")
class TimeControllers {
    companion object {
        val KNOWN_TIMEZONES: Set<String> = ImmutableSet.copyOf(TimeZone.getAvailableIDs())
        const val FORWARD_PERIOD_FORMAT = "P%s"
        const val BACKWARD_PERIOD_FORMAT = "-P%s"
    }

    @GetMapping("/time")
    fun getTime(@RequestParam tz: String?, @RequestParam future: String?, @RequestParam past: String?): TimeResponse {
        if (future != null && past != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Either \"future\" or \"past\" can be populated, but not both")
        }

        var curTime = if (tz != null) {
            if (!KNOWN_TIMEZONES.contains(tz)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Time zone \"%s\" not recognized", tz))
            }
            ZonedDateTime.now(TimeZone.getTimeZone(tz).toZoneId())
        } else {
            ZonedDateTime.now()
        }

        if (future != null || past != null) {
            try {
                val period: Period = if (future != null) {
                    Period.parse(String.format(FORWARD_PERIOD_FORMAT, future))
                } else {
                    Period.parse(String.format(BACKWARD_PERIOD_FORMAT, past))
                }
                curTime = curTime.plus(period)
            } catch (e: DateTimeParseException) {
                val errorMessage: String = if (future != null) {
                    String.format("Time format \"%s\" not recognized for future parameter", future)
                } else {
                    String.format("Time format \"%s\" not recognized for past parameter", past)
                }
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
            }
        }

        return TimeResponse(curTime)
    }
}
