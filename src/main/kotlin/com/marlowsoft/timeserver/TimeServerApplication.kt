package com.marlowsoft.timeserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TimeServerApplication

fun main(args: Array<String>) {
	runApplication<TimeServerApplication>(*args)
}
