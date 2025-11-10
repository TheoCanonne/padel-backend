package com.padel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PadelApplication

fun main(args: Array<String>) {
    runApplication<PadelApplication>(*args)
}
