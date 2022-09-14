package com.marlowsoft.timeserver

import com.marlowsoft.timeserver.obj.TimeResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api")
class TimeControllers {
    @GetMapping("/time")
    fun getTime() = TimeResponse(ZonedDateTime.now())
}
