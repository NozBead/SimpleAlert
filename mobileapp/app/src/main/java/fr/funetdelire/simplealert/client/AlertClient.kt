package fr.funetdelire.simplealert.client

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

interface AlertClient {
    companion object {
        @Volatile
        private var INSTANCE: AlertClient? = null

        fun getClient(baseUrl : String): AlertClient {
            //return INSTANCE ?: synchronized(this) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                val instance = retrofit.create(AlertClient::class.java)
                //INSTANCE = instance
                //instance
                return instance
            //}
        }
    }

    @GET("alert")
    fun getAlert(): Call<String>
}
