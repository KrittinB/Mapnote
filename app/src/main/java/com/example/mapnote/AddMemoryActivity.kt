package com.example.mapnote

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMemoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_memory)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_memory_root)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Adjust header for status bar
            findViewById<View>(R.id.header)?.setPadding(
                0, systemBars.top, 0, 0
            )

            // Adjust root padding for navigation bar
            findViewById<View>(R.id.add_memory_root).setPadding(
                0, 0, 0, systemBars.bottom
            )

            insets
        }

        // Set current timestamp
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val currentTimestamp = sdf.format(Date())
        findViewById<android.widget.TextView>(R.id.tvAddTimestamp).text = currentTimestamp

        // Close button
        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Save button
        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, "Memory Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Add Photo Area
        findViewById<View>(R.id.btnAddPhoto).setOnClickListener {
            Toast.makeText(this, "Opening Camera...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
