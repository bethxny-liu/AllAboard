package org.allaboard.project

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * AndroidX [androidx.lifecycle.ViewModel] uses [kotlinx.coroutines.Dispatchers.Main] for
 * [androidx.lifecycle.viewModelScope]. Plain JVM unit tests have no Android main looper, so
 * [Dispatchers.setMain] from kotlinx-coroutines-test is required.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class ViewModelTestBase {

    @BeforeTest
    fun setupViewModelMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDownViewModelMainDispatcher() {
        Dispatchers.resetMain()
    }
}
