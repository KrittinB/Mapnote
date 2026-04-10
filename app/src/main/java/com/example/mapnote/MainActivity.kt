package com.example.mapnote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAddNote: FloatingActionButton

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // OSMDroid configuration
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)

            // Adjust top bar padding for status bar
            findViewById<FrameLayout>(R.id.topBarContainer)?.let { topBar ->
                topBar.setPadding(0, systemBars.top, 0, 0)
            }
            insets
        }

        initViews()
        setupMap()
        setupControls()
        setupBottomNavigation()
        addSampleNoteMarkers()
        requestPermissionsIfNeeded()
    }

    private fun initViews() {
        mapView = findViewById(R.id.mapView)
        bottomNav = findViewById(R.id.bottomNavigation)
        fabAddNote = findViewById(R.id.fabAddNote)
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Disable default zoom controls (we have our own)
        mapView.setBuiltInZoomControls(false)

        // Set initial position (world view like the mockup)
        val mapController = mapView.controller
        mapController.setZoom(3.0)
        mapController.setCenter(GeoPoint(30.0, 50.0)) // Centered to show world map nicely
    }

    private fun setupControls() {
        // Zoom In
        findViewById<View>(R.id.btnZoomIn).setOnClickListener {
            mapView.controller.zoomIn()
        }

        // Zoom Out
        findViewById<View>(R.id.btnZoomOut).setOnClickListener {
            mapView.controller.zoomOut()
        }

        // My Location
        findViewById<View>(R.id.btnMyLocation).setOnClickListener {
            if (hasLocationPermission()) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                try {
                    val lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    if (lastKnown != null) {
                        val geoPoint = GeoPoint(lastKnown.latitude, lastKnown.longitude)
                        mapView.controller.animateTo(geoPoint)
                        mapView.controller.setZoom(15.0)
                    } else {
                        Toast.makeText(this, "ไม่พบตำแหน่งปัจจุบัน", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(this, "ไม่มีสิทธิ์เข้าถึงตำแหน่ง", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestPermissionsIfNeeded()
            }
        }

        // FAB - Add Note
        fabAddNote.setOnClickListener {
            Toast.makeText(this, "เพิ่มโน้ตใหม่", Toast.LENGTH_SHORT).show()
            // TODO: Open Add Note screen
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_explore
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    // Already on explore (map) screen
                    true
                }
                R.id.nav_memories -> {
                    Toast.makeText(this, "Memories", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to Memories screen
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to Profile screen
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Add sample note markers on the map to demonstrate the UI.
     * Each marker has a thumbnail image and a label, similar to the mockup.
     */
    private fun addSampleNoteMarkers() {
        data class NotePin(val title: String, val lat: Double, val lon: Double, val color: Int)

        val sampleNotes = listOf(
            NotePin("KYOTO", 35.0116, 135.7681, Color.parseColor("#4CAF50")),
            NotePin("SHIBUYA", 35.6580, 139.7016, Color.parseColor("#FF9800")),
            NotePin("METROPOLIS", 6.5244, 3.3792, Color.parseColor("#9C27B0")),
            NotePin("PARIS", 48.8566, 2.3522, Color.parseColor("#E91E63")),
            NotePin("NEW YORK", 40.7128, -74.0060, Color.parseColor("#2196F3"))
        )

        for (note in sampleNotes) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(note.lat, note.lon)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = note.title

            // Create custom marker icon with thumbnail + label
            val markerIcon = createNoteMarkerDrawable(note.title, note.color)
            marker.icon = markerIcon

            marker.setOnMarkerClickListener { m, _ ->
                Toast.makeText(this, "📍 ${m.title}", Toast.LENGTH_SHORT).show()
                true
            }

            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    /**
     * Creates a custom drawable for a map marker that looks like the mockup:
     * a rounded-rect thumbnail with a label underneath.
     */
    private fun createNoteMarkerDrawable(label: String, accentColor: Int): BitmapDrawable {
        val width = 160
        val height = 180
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // -- Thumbnail card (rounded rect with shadow) --
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(6f, 0f, 3f, Color.parseColor("#33000000"))
        }
        val cardRect = RectF(8f, 4f, (width - 8).toFloat(), 120f)
        canvas.drawRoundRect(cardRect, 16f, 16f, cardPaint)

        // Inner image area (simulated with accent color)
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
        }
        val imageRect = RectF(16f, 12f, (width - 16).toFloat(), 112f)
        canvas.drawRoundRect(imageRect, 12f, 12f, imagePaint)

        // A subtle gradient overlay on the image
        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 60f, 0f, 112f,
                Color.TRANSPARENT,
                Color.parseColor("#66000000"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(imageRect, 12f, 12f, gradientPaint)

        // Small title text inside image
        val innerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(label, width / 2f, 100f, innerTextPaint)

        // -- Label pill below card --
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#22000000"))
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A2E")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textWidth = textPaint.measureText(label)
        val pillW = textWidth + 32f
        val pillLeft = (width - pillW) / 2f
        val pillRect = RectF(pillLeft, 126f, pillLeft + pillW, 164f)
        canvas.drawRoundRect(pillRect, 18f, 18f, pillPaint)
        canvas.drawText(label, width / 2f, 152f, textPaint)

        // -- Small triangle pointer below pill --
        val triPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val path = android.graphics.Path().apply {
            moveTo(width / 2f - 8f, 162f)
            lineTo(width / 2f, 174f)
            lineTo(width / 2f + 8f, 162f)
            close()
        }
        canvas.drawPath(path, triPaint)

        return BitmapDrawable(resources, bitmap)
    }

    // -- Permissions --

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMISSIONS_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ได้รับสิทธิ์ตำแหน่งแล้ว", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // -- Lifecycle --

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}