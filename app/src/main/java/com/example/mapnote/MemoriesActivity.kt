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
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MemoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_memories)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.memories_root)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Adjust top bar for status bar
            findViewById<View>(R.id.memoriesTopBar)?.setPadding(
                20.dpToPx(), systemBars.top + 12.dpToPx(), 20.dpToPx(), 12.dpToPx()
            )

            // Adjust bottom nav for nav bar
            findViewById<BottomNavigationView>(R.id.bottomNavigationMemories)?.let { nav ->
                nav.setPadding(0, 0, 0, systemBars.bottom)
                val params = nav.layoutParams
                params.height = 60.dpToPx() + systemBars.bottom
                nav.layoutParams = params
            }

            insets
        }

        setupBottomNavigation()
        populateMemories()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationMemories)
        bottomNav.selectedItemId = R.id.nav_memories
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    true
                }
                R.id.nav_memories -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }
    }

    private fun populateMemories() {
        val listContainer = findViewById<LinearLayout>(R.id.llMemoryList)
        val inflater = LayoutInflater.from(this)

        val memories = listOf(
            MemoryData("Sunset over the Seine", "Paris, FR", "Oct 12", "Walking across the Pont Alexandre III as the sky turned a deep shade of...", Pair(Color.parseColor("#1A237E"), Color.parseColor("#42A5F5"))),
            MemoryData("Canal Serenity", "Venice, IT", "Sep 30", "Early morning gondola ride through the quiet canals of the floating city.", Pair(Color.parseColor("#004D40"), Color.parseColor("#4DB6AC"))),
            MemoryData("Future Horizon", "Dubai, UAE", "Aug 15", "Standing at the top of the Burj Khalifa, seeing the city blend into the desert.", Pair(Color.parseColor("#4E342E"), Color.parseColor("#BCAAA4"))),
            MemoryData("Zen Bamboo", "Kyoto, JP", "Jun 04", "The rustling sound of bamboo leaves at Arashiyama is therapy for the soul.", Pair(Color.parseColor("#1B5E20"), Color.parseColor("#81C784"))),
            MemoryData("Marble Marvel", "Agra, IN", "May 10", "A breathtaking sunrise view of the Taj Mahal reflecting in the water.", Pair(Color.parseColor("#FF6F00"), Color.parseColor("#FFD54F")))
        )

        for (memory in memories) {
            val view = inflater.inflate(R.layout.item_memory, listContainer, false)
            
            view.findViewById<TextView>(R.id.tvMemoryTitle).text = memory.title
            view.findViewById<TextView>(R.id.tvLocationTag).text = memory.location
            view.findViewById<TextView>(R.id.tvMemoryDate).text = memory.date
            view.findViewById<TextView>(R.id.tvMemoryDesc).text = memory.description
            
            // Set a gradient as a placeholder for the image
            val imageView = view.findViewById<ImageView>(R.id.ivMemoryImage)
            imageView.setImageDrawable(createGradientDrawable(memory.colors))
            
            listContainer.addView(view)
        }
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

    data class MemoryData(
        val title: String,
        val location: String,
        val date: String,
        val description: String,
        val colors: Pair<Int, Int>
    )
}
