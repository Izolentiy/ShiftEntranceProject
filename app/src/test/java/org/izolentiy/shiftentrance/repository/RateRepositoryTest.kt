package org.izolentiy.shiftentrance.repository

import android.util.Log
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.izolentiy.shiftentrance.*
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class RateRepositoryTest {
    private val dao = mockkClass(ExchangeRatesDao::class)
    private val service = mockkClass(CbrService::class)
    private val repos = RateRepository(dao, service)

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
    }

    @Nested
    @DisplayName("exchangeRate flow tests")
    inner class RateFlowTest {
        @Test
        fun `local notNull, currency list notEmpty, should not reload`() = runTest {
            coEvery { dao.getLatestRate() } returns RATE_SAVED_BEFORE

            repos.exchangeRate.testFlow {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.success(RATE_SAVED_BEFORE), awaitItem())
            }
        }

        @Test
        fun `local notNull, currency list empty, should not reload`() = runTest {
            coEvery { dao.getLatestRate() } returns RATE_SAVED_BEFORE.copy(currencies = emptyList())
            coEvery { service.getDailyRate() } returns remoteSuccess(RATE_FROM_NET)
            coEvery { dao.insertExchangeRates(RATE_FROM_NET) } just Runs

            repos.reloadRate()
            repos.exchangeRate.testFlow {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.success(RATE_FROM_NET), awaitItem())
            }
        }

        @Test
        fun `local notNull, currency list notEmpty, should reload`() = runTest {
            coEvery { dao.getLatestRate() } returns RATE_SAVED_BEFORE
            coEvery { service.getDailyRate() } returns remoteSuccess(RATE_FROM_NET)
            coEvery { dao.insertExchangeRates(RATE_FROM_NET) } just Runs

            repos.reloadRate()
            repos.exchangeRate.testFlow {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.success(RATE_FROM_NET), awaitItem())
            }
        }

        @Test
        fun `local null, remote success`() = runTest {
            coEvery { dao.getLatestRate() } returns null
            coEvery { service.getDailyRate() } returns remoteSuccess(RATE_FROM_NET)
            coEvery { dao.insertExchangeRates(any()) } just Runs

            repos.exchangeRate.testFlow {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.success(RATE_FROM_NET), awaitItem())
            }
        }

        @Test
        fun `local null, remote error`() = runTest {
            coEvery { dao.getLatestRate() } returns null
            coEvery { service.getDailyRate() } returns remoteError()

            repos.exchangeRate.testFlow {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.Status.ERROR, awaitItem().status)
            }
        }
    }

    @Nested
    @DisplayName("latestRates flow tests")
    inner class RatesFlowTest {
        @Test
        fun `local notNull`() = runTest {
            coEvery { dao.getLatestRate() } returns RATE_SAVED_BEFORE
            coEvery { dao.getExchangeRateByDate(any()) } returns RATE_SAVED_BEFORE

            val count = 8
            val rates = mutableListOf<ExchangeRate>()
            repeat(count) { rates.add(RATE_SAVED_BEFORE) }

            repos.loadRates(count)
            repos.latestRates.testFlow {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.success(rates), awaitItem())
            }
        }

        @Test
        fun `local null, remote success`() = runTest {
            coEvery { dao.getLatestRate() } returns null
            coEvery { dao.getExchangeRateByDate(any()) } returns null

            coEvery { service.getDailyRate() } returns remoteSuccess(RATE_FROM_NET)
            coEvery { service.getExchangeByUrl(any()) } returns remoteSuccess(RATE_FROM_NET)
            coEvery { dao.insertExchangeRates(any()) } just Runs

            val count = 8
            val rates = mutableListOf<ExchangeRate>()
            repeat(count) { rates.add(RATE_FROM_NET) }

            repos.loadRates(count)
            repos.latestRates.testFlow(API_CALL_DELAY.toTimeout(count)) {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.success(rates), awaitItem())
            }
            coVerify(exactly = count) { dao.insertExchangeRates(any()) }
        }

        @Test
        fun `local null, remote error`() = runTest {
            coEvery { dao.getLatestRate() } returns null
            coEvery { dao.getExchangeRateByDate(any()) } returns null

            coEvery { service.getDailyRate() } returns remoteError()
            coEvery { service.getExchangeByUrl(any()) } returns remoteError()

            repos.loadRates(8)
            repos.latestRates.testFlow {
                assertEquals(Resource.Status.LOADING, awaitItem().status)
                assertEquals(Resource.Status.ERROR, awaitItem().status)
            }

        }
    }

}