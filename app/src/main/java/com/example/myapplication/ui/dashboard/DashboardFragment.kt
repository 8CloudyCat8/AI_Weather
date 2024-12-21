package com.example.myapplication.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.HelperClass
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.network.WeatherResponse
import com.example.myapplication.network.WeatherService
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val apiKey = "2508ee25ae5b4769938124125242012"
    private val baseUrl = "https://api.weatherapi.com/v1/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val helperClass = HelperClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        swipeRefreshLayout = binding.root.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            val city = binding.cityEditText.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWeatherData(city)
            } else {
                binding.locationText.text = "Пожалуйста, введите название города."
                swipeRefreshLayout.isRefreshing = false
            }
        }

        val fetchWeatherButton: Button = binding.root.findViewById(R.id.fetchWeatherButton)
        val cityEditText: EditText = binding.root.findViewById(R.id.cityEditText)

        fetchWeatherButton.setOnClickListener {
            val city = cityEditText.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWeatherData(city)
            } else {
                binding.locationText.text = "Пожалуйста, введите название города."
            }
        }

        return root
    }

    private fun fetchOutfitRecommendation(weather: WeatherResponse) {
        val locationName = weather.location.name
        val temperature = weather.current.temp_c
        val condition = weather.current.condition.text
        val windSpeed = weather.current.wind_mph
        val humidity = weather.current.humidity
        val pressure = weather.current.pressure_mb
        val visibility = weather.current.vis_km
        val feelsLike = weather.current.feelslike_c
        val cloudiness = weather.current.cloud
        val windGust = weather.current.gust_mph
        val dewPoint = weather.current.dewpoint_c

        val userRequest = """
        Как мне одеться в городе $locationName, где температура $temperature°C, 
        погода: $condition, ветер: $windSpeed м/ч, влажность: $humidity%, 
        давление: $pressure мб, видимость: $visibility км, 
        температура по ощущениям: $feelsLike°C, облачность: $cloudiness%, 
        порывы ветра: $windGust м/ч, точка росы: $dewPoint°C? 
        Дай небольшой ответ c разными эмодзи, характеризующими погоду.
    """.trimIndent()

        lifecycleScope.launch {
            try {
                val recommendations = helperClass.sendRequest(userRequest)
                binding.recommendationsText.text = recommendations
            } catch (e: Exception) {
                binding.recommendationsText.text = "Не удалось получить рекомендации."
            }
        }
    }

    private fun fetchWeatherData(location: String) {
        val call = weatherService.getCurrentWeather(apiKey, location)
        call.enqueue(object : Callback<WeatherResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val weather = response.body()
                    if (weather != null) {
                        val locationName = weather.location.name
                        val temperature = weather.current.temp_c
                        val condition = weather.current.condition.text
                        val windSpeed = weather.current.wind_mph
                        val humidity = weather.current.humidity
                        val pressure = weather.current.pressure_mb
                        val visibility = weather.current.vis_km
                        val feelsLike = weather.current.feelslike_c
                        val cloudiness = weather.current.cloud
                        val windGust = weather.current.gust_mph
                        val dewPoint = weather.current.dewpoint_c

                        val translatedCondition = translateConditionToRussian(condition)

                        binding.locationText.text = "Город: $locationName"
                        binding.temperatureText.text = "Температура: $temperature°C"
                        binding.conditionText.text = "Погода: $translatedCondition"
                        binding.windText.text = "Ветер: $windSpeed м/ч"
                        binding.humidityText.text = "Влажность: $humidity%"
                        binding.pressureText.text = "Давление: $pressure мб"
                        binding.visibilityText.text = "Видимость: $visibility км"
                        binding.feelsLikeText.text = "Температура по ощущениям: $feelsLike°C"
                        binding.cloudinessText.text = "Облачность: $cloudiness%"
                        binding.windGustText.text = "Порывы ветра: $windGust м/ч"
                        binding.dewPointText.text = "Точка росы: $dewPoint°C"

                        binding.humidityProgressBar.progress = humidity
                        binding.windSpeedProgressBar.progress = windSpeed.toInt()
                        binding.cloudinessProgressBar.progress = cloudiness

                        fetchOutfitRecommendation(weather)
                    }
                } else {
                    binding.locationText.text = "Ошибка: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                binding.locationText.text = "Не удалось загрузить данные о погоде: ${t.message}"
            }
        })
    }

    private fun translateConditionToRussian(condition: String): String {
        return when (condition.lowercase()) {
            "clear" -> "Ясно"
            "partly cloudy" -> "Частично облачно"
            "cloudy" -> "Облачно"
            "overcast" -> "Пасмурно"
            "rain" -> "Дождь"
            "thunderstorm" -> "Гроза"
            "snow" -> "Снег"
            "fog" -> "Туман"
            "hail" -> "Град"
            "sleet" -> "Дождь со снегом"
            "sunny" -> "Солнечно"
            "light rain" -> "Легкий дождь"
            "heavy rain" -> "Сильный дождь"
            "light snow" -> "Легкий снег"
            "heavy snow" -> "Сильный снег"
            "light thunderstorm" -> "Легкая гроза"
            "heavy thunderstorm" -> "Сильная гроза"
            "snow showers" -> "Снежные осадки"
            "rain showers" -> "Дождевые осадки"
            "thunderstorms with light rain" -> "Гроза с легким дождем"
            "thunderstorms with heavy rain" -> "Гроза с сильным дождем"
            "partly cloudy with light rain" -> "Частично облачно с легким дождем"
            "partly cloudy with snow" -> "Частично облачно со снегом"
            "clear with light rain" -> "Ясно с легким дождем"
            "clear with snow" -> "Ясно со снегом"
            "mist" -> "Мгла"
            "dust" -> "Пыль"
            "smoke" -> "Дым"
            "volcanic ash" -> "Вулканический пепел"
            "clear sky" -> "Чистое небо"
            "light fog" -> "Легкий туман"
            "moderate rain" -> "Умеренный дождь"
            "moderate snow" -> "Умеренный снег"
            "blizzard" -> "Метель"
            "freezing rain" -> "Ледяной дождь"
            "drizzle" -> "Морось"
            "scattered clouds" -> "Рассеивающиеся облака"
            "broken clouds" -> "Разорванные облака"
            "thunderstorm with hail" -> "Гроза с градом"
            "tropical storm" -> "Тропический шторм"
            "cold" -> "Холодно"
            "hot" -> "Жарко"
            "windy" -> "Ветрено"
            "calm" -> "Штиль"
            "gusty wind" -> "Порывистый ветер"
            "light snow showers" -> "Легкие снежные осадки"
            "patchy moderate snow" -> "Местами умеренный снег"
            else -> condition
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
