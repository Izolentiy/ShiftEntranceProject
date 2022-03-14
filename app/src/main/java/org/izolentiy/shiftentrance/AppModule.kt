package org.izolentiy.shiftentrance

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.izolentiy.shiftentrance.model.ServerResponse
import org.izolentiy.shiftentrance.model.ServerResponseDeserializer
import org.izolentiy.shiftentrance.repository.CbrService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(ServerResponse::class.java, ServerResponseDeserializer())
        .create()

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(CbrService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideCbrService(retrofit: Retrofit): CbrService {
        return retrofit.create(CbrService::class.java)
    }

}