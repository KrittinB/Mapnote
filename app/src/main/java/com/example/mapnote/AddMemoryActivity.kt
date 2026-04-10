package com.example.mapnote

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import com.google.android.material.button.MaterialButton
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMemoryActivity : AppCompatActivity() {

    private lateinit var ivSelectedImage: ImageView
    private lateinit var llAddPhotoPlaceholder: LinearLayout
    private lateinit var tvAddLocation: TextView
    private lateinit var tvAddTimestamp: TextView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                displaySelectedImage(imageUri)
                extractExifData(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_memory)

        initViews()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_memory_root)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.header)?.let { header ->
                header.setPadding(dpToPx(20), systemBars.top + dpToPx(12), dpToPx(20), dpToPx(12))
            }
            findViewById<View>(R.id.add_memory_root).setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, "Memory Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<View>(R.id.btnAddPhoto).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    private fun initViews() {
        ivSelectedImage = findViewById(R.id.ivSelectedImage)
        llAddPhotoPlaceholder = findViewById(R.id.llAddPhotoPlaceholder)
        tvAddLocation = findViewById(R.id.tvAddLocation)
        tvAddTimestamp = findViewById(R.id.tvAddTimestamp)
    }

    private fun displaySelectedImage(uri: Uri) {
        ivSelectedImage.setImageURI(uri)
        ivSelectedImage.visibility = View.VISIBLE
        llAddPhotoPlaceholder.visibility = View.GONE
    }

    private fun extractExifData(uri: Uri) {
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exifInterface = ExifInterface(inputStream)
                
                // Extract Latitude/Longitude
                val latLong = FloatArray(2)
                if (exifInterface.getLatLong(latLong)) {
                    val latitude = latLong[0]
                    val longitude = latLong[1]
                    tvAddLocation.text = String.format(Locale.getDefault(), "%.4f° N, %.4f° E", latitude, longitude)
                } else {
                    tvAddLocation.text = "No location data found in image"
                }

                // Extract Timestamp
                val dateString = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
                if (dateString != null) {
                    // EXIF format is usually "yyyy:MM:dd HH:mm:ss"
                    val exifFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                    val date = exifFormat.parse(dateString)
                    if (date != null) {
                        tvAddTimestamp.text = outputFormat.format(date)
                    }
                } else {
                    tvAddTimestamp.text = "No date data found in image"
                }
            }
        } catch (e: Exception) {
            Log.e("ExifData", "Error extracting EXIF data", e)
            Toast.makeText(this, "Could not read image metadata", Toast.LENGTH_SHORT).show()
        } finally {
            inputStream?.close()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }
}
