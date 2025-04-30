package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {

        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )
        //A CoroutineWorker, by default, runs as Dispatchers.Default but can be changed by calling withContext() and passing in the desired dispatcher.
        //Create a withContext() block.
        //Inside the call to withContext() pass Dispatchers.IO so the lambda function runs in a special thread pool for potentially blocking IO operations.
        return withContext(Dispatchers.IO) {
            try {
                /*
                     Check that the resourceUri variable is populated.
                     If it is not populated, your code should throw an exception.
                     The code that follows is using the require() statement which throws an IllegalArgumentException
                     if the first argument evaluates to false.
                */
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }

                /*
                   Since the image source is passed in as a URI, we need a ContentResolver object to
                   read the contents pointed to by the URI.
                */
                val resolver = applicationContext.contentResolver

                // This is an utility function added to emulate slower work.
                delay(DELAY_TIME_MILLIS)

                /*
                Because the image source is now the passed in URI, use BitmapFactory.decodeStream()
                instead of BitmapFactory.decodeResource() to create the Bitmap object
                 */
                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )
                val output = blurBitmap(picture, blurLevel)

                // Write bitmap to a temp file
                val outputUri = writeBitmapToFile(applicationContext, output)

                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

                Result.success(outputData)

            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )
                Result.failure()
            }
        }
    }
}