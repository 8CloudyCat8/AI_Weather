package com.example.myapplication.network

data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String,
    val region: String,
    val country: String
)

data class Current(
    val temp_c: Double,
    val temp_f: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Double,
    val wind_kph: Double,
    val humidity: Int,
    val pressure_mb: Double,
    val pressure_in: Double,
    val precip_mm: Double,
    val precip_in: Double,
    val feelslike_c: Double,
    val feelslike_f: Double,
    val wind_degree: Int,
    val wind_dir: String,
    val vis_km: Double,
    val cloud: Int,
    val gust_mph: Double,
    val dewpoint_c: Double,
    val dewpoint_f: Double
)



data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)
