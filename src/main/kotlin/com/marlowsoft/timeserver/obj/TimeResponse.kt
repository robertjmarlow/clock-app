package com.marlowsoft.timeserver.obj

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class TimeResponse(val time: ZonedDateTime) {
    val timeStr: String = time.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}
