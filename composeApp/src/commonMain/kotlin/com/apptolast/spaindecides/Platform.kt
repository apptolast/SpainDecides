package com.apptolast.spaindecides

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform