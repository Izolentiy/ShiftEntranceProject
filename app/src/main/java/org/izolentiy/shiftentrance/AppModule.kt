package org.izolentiy.shiftentrance

import android.app.Application
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.model.ServerResponseDeserializer
import org.izolentiy.shiftentrance.repository.AppDatabase
import org.izolentiy.shiftentrance.repository.CbrService
import org.izolentiy.shiftentrance.repository.ExchangeRatesDao
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(ExchangeRate::class.java, ServerResponseDeserializer())
        .create()

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(CbrService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideCbrService(retrofit: Retrofit): CbrService =
        retrofit.create(CbrService::class.java)

    @Provides
    @Singleton
    fun provideRoomDatabase(app: Application): AppDatabase = Room
        .databaseBuilder(app, AppDatabase::class.java, "app_database")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideExchangeRateDao(db: AppDatabase): ExchangeRatesDao = db.exchangeRatesDao()

}