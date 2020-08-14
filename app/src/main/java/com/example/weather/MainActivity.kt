package com.example.weather

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    private val retrofitImpl: RetrofitImpl = RetrofitImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        icon.typeface = Typeface.createFromAsset(assets, "weather.ttf")
        sendServerRequest()
    }


    private fun sendServerRequest() {
        retrofitImpl.getRetrofit()
            .getWeather(
                // "c5143177-9c97-4f4a-8e72-a1a258c5014b",
                 "8df85a2d-de57-4e99-be0f-4d7cb50a67ef",
                47.709601, // 47.730560,
                40.215797// 40.225110
            ).enqueue(
                object :
                    Callback<DataModel> {
                    override fun onResponse(call: Call<DataModel>, response: Response<DataModel>) {
                        if (response.isSuccessful && response.body() != null) renderData(
                            response.body(),
                            null
                        )
                        else renderData(null, Throwable("Пустой ответ от сервера"))
                    }

                    override fun onFailure(call: Call<DataModel>, t: Throwable) {
                        renderData(null, t)
                    }
                }
            )

    }


    private fun renderData(dataModel: DataModel?, error: Throwable?) {
        if (dataModel?.fact == null || error != null)
            Toast.makeText(this, "Ошибка нет данных", Toast.LENGTH_LONG).show()
        else {
            val fact: Fact? = dataModel.fact
            val info: Info? = dataModel.info
            val temperature: Int? = fact?.temp
            val city: String? = info?.url
            if (city != null) city_field.text = "Шахты" // city.toString()
            else Toast.makeText(this, "Ошибка города", Toast.LENGTH_LONG).show()
            if (temperature == null)
                Toast.makeText(this, "Ошибка нет температуры", Toast.LENGTH_LONG).show()
            else current_temper.text = temperature.toString()
            val feelsLike: Int? = fact?.feels_like
            if (feelsLike == null)
                Toast.makeText(this, "Ошибка нет ощущения", Toast.LENGTH_LONG).show()
            else temper_feel.text = feelsLike.toString()
            val condition: String? = fact?.condition
            if (condition.isNullOrEmpty())
                Toast.makeText(this, "Ошибка нет иконки", Toast.LENGTH_LONG).show()
            else icon.text = when (condition) {
                "clear" ->
                    getString(R.string.weather_sunny)
                "party-cloudy", "cloudy", "overcast" ->
                    getString(R.string.weather_sunny)
                "party-cloudy-and-light-rain", "cloudy-and-light-rain", "overcast-and-light-rain" ->
                    getString(R.string.weather_drizzle)
                "party-cloudy-and-rain", "cloudy-and-rain", "overcast-and-rain", "heavy-rain" ->
                    getString(R.string.weather_rainy)
                "overcast-thonderstorms-with-rain" ->
                    getString(R.string.weather_thunder)
                "party-cloudy-and-light-snow", "cloudy-and-light-snow", "overcast-and-wet-snow",
                "overcast-and-snow", "overcast-and-light-snow", "cloudy-and-snow", "party-cloudy-and-snow" ->
                    getString(R.string.weather_snowy)
                else -> getString(R.string.weather_sunny)
            }
        }
    }
}

data class DataModel(
    val fact: Fact?,
    val info: Info?
)


data class Info(
    val url: String?
)

data class Fact(
    val temp: Int?,
    val feel: Int?,
    val condition: String?,
    val feels_like: Int?
)

interface WeatherAPI {
    @GET("v2/informers")
    fun getWeather(
        @Header("X-Yandex-API-Key") token: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Call<DataModel>
}

class RetrofitImpl {

    fun getRetrofit(): WeatherAPI {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.weather.yandex.ru/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()
        return retrofit.create(WeatherAPI::class.java)
    }
}