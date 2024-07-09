package com.ml.quaterion.facenetdetection // Update with your package name

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.ml.quaterion.facenetdetection.databinding.ActivityAddPeopleBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class AddPeopleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPeopleBinding
    private var selectedImageUri: Uri? = null

    // Launcher for choosing an image from the gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.chosenImageView.setImageURI(selectedImageUri)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.choosePictureButton.setOnClickListener {val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*" // Allow any image type
            addCategory(Intent.CATEGORY_OPENABLE) // Ensure the content is accessible
        }
            pickImageLauncher.launch(intent)
        }

        binding.saveButton.setOnClickListener {
            saveImage()
        }
    }


    /**
     * Rotates the given bitmap based on the orientation information in the EXIF data of the image.
     */
    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {
        val input: InputStream? = contentResolver.openInputStream(selectedImage)
        val ei: ExifInterface? = input?.let { ExifInterface(it) }
        return when (ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    /**
     * Rotates the given bitmap by the given degrees.
     */
    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle() // Recycle the original bitmap to free up memory
        return rotatedImg
    }

    private fun saveImage() {
        val folderName = binding.folderNameEditText.text.toString().trim()
        val imageName = binding.imageNameEditText.text.toString().trim()

        if (folderName.isEmpty()) {
            Toast.makeText(this, "Please enter a folder name", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageName.isEmpty()) {
            Toast.makeText(this, "Please enter an image name", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val appDirectory = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "images")
            val personDirectory = File(appDirectory, folderName)
            if (!personDirectory.exists() && !personDirectory.mkdirs()) {
                throw IOException("Could not create directory")
            }

            val imageFile = File(personDirectory, "$imageName.jpg")

            // Open input stream and decode bitmap
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close() // Close input stream after decoding

            // Handle orientation
            val rotatedBitmap = rotateImageIfRequired(bitmap, selectedImageUri!!)

            // Resize if necessary (adjust newWidth and newHeight as needed)
//            val newWidth = 640
//            val newHeight = 480
//            val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)

            // Save the image with adjusted compression quality
            FileOutputStream(imageFile).use { outputStream ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Use JPEG and adjust quality
            }

            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
            finish() // Close AddPeopleActivity and return to the previous screen
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}