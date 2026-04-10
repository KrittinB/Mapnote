package com.example.mapnote

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MemoryDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_memory_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            findViewById<View>(R.id.detailTopBar)?.setPadding(
                20.dpToPx(), systemBars.top + 12.dpToPx(), 20.dpToPx(), 12.dpToPx()
            )

            findViewById<BottomNavigationView>(R.id.bottomNavigationDetail)?.let { nav ->
                nav.setPadding(0, 0, 0, systemBars.bottom)
                val params = nav.layoutParams
                params.height = 60.dpToPx() + systemBars.bottom
                nav.layoutParams = params
            }

            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupBottomNavigation()
        displayData()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationDetail)
        bottomNav.selectedItemId = R.id.nav_memories
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_memories -> {
                    startActivity(Intent(this, MemoriesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun displayData() {
        val title = intent.getStringExtra("title") ?: "Shibuya Crossing, Tokyo"
        val date = intent.getStringExtra("date") ?: "October 24, 2023 • 08:30 PM"
        val desc = intent.getStringExtra("desc") ?: "Standing at the edge of the world's busiest intersection, the sheer energy is palpable."
        val colorStart = intent.getIntExtra("colorStart", Color.parseColor("#1A237E"))
        val colorEnd = intent.getIntExtra("colorEnd", Color.parseColor("#42A5F5"))

        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailDate).text = date
        findViewById<TextView>(R.id.tvDetailStory).text = desc
        
        val imageView = findViewById<ImageView>(R.id.ivDetailImage)
        imageView.setImageDrawable(createGradientDrawable(Pair(colorStart, colorEnd)))
    }

    private fun createGradientDrawable(colors: Pair<Int, Int>): PaintDrawable {
        val shape = PaintDrawable()
        shape.shape = RectShape()
        shape.shaderFactory = object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                return LinearGradient(0f, 0f, 0f, height.toFloat(),
                    intArrayOf(colors.first, colors.second), null, Shader.TileMode.CLAMP)
            }
        }
        return shape
    }

    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
