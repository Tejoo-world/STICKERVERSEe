package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.StickerViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Stickerverse", appName)
  }

  @Test
  fun testViewModelInstantiation() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    try {
      val viewModel = StickerViewModel(application)
      println("ViewModel successfully created! $viewModel")
    } catch (t: Throwable) {
      t.printStackTrace()
      throw t
    }
  }
}
