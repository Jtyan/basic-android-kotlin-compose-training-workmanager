package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )
        //A CoroutineWorker, by default, runs as Dispatchers.Default but can be changed by calling withContext() and passing in the desired dispatcher.
        //Create a withContext() block.
        //Inside the call to withContext() pass Dispatchers.IO so the lambda function runs in a special thread pool for potentially blocking IO operations.
        return withContext(Dispatchers.IO) {
             try {
                 // This is an utility function added to emulate slower work.
                 delay(DELAY_TIME_MILLIS)

                val picture = BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.android_cupcake
                )
                val output = blurBitmap(picture, 1)
                // Write bitmap to a temp file
                val outputUri = writeBitmapToFile(applicationContext, output)
                //display a notification message to the user that contains the outputUri variable
                makeStatusNotification(
                    "Output is $outputUri",
                    applicationContext
                )
                Result.success()
            }catch (throwable: Throwable) {
                Log.e(TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )
                Result.failure()
            }
        }
    }
}