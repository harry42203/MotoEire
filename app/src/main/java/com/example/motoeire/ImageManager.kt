package com.example.motoeire

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Handles image storage and retrieval for car photos
 * Images are stored in app's internal cache directory
 */
class ImageManager(private val context: Context) {

    private val imageDir = File(context.noBackupFilesDir, "car_images")

    init {
        // Create directory if it doesn't exist
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
    }

    /**
     * Save a bitmap image to internal storage
     * Returns the file path as a String
     */
    fun saveBitmap(bitmap: Bitmap): String? {
        return try {
            // Create unique filename using timestamp
            val filename = "car_${System.currentTimeMillis()}.jpg"
            val file = File(imageDir, filename)

            // Compress and save the bitmap
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                output.flush()
            }

            file.absolutePath  // Return the file path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save an image from URI to internal storage
     * Returns the file path as a String
     */
    fun saveImageFromUri(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val filename = "car_${System.currentTimeMillis()}.jpg"
            val file = File(imageDir, filename)

            // Copy file
            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath  // Return the file path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete an image by file path
     */
    fun deleteImage(imagePath: String?): Boolean {
        return try {
            if (imagePath.isNullOrEmpty()) return false
            File(imagePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if image file exists
     */
    fun imageExists(imagePath: String?): Boolean {
        return try {
            !imagePath.isNullOrEmpty() && File(imagePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get file size in MB
     */
    fun getImageSizeInMB(imagePath: String?): Double {
        return try {
            if (imagePath.isNullOrEmpty()) return 0.0
            File(imagePath).length() / (1024.0 * 1024.0)
        } catch (e: Exception) {
            0.0
        }
    }
}