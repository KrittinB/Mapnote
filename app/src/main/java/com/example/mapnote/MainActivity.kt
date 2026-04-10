package com.example.mapnote

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
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

        initViews()

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Adjust top bar for status bar and add balanced padding
            findViewById<View>(R.id.topBarContainer)?.let { topBar ->
                topBar.setPadding(
                    dpToPx(24),
                    systemBars.top + dpToPx(16),
                    dpToPx(24),
                    dpToPx(20)
                )
            }

            // Adjust bottom nav height and padding for nav bar
            bottomNav.let { nav ->
                val params = nav.layoutParams
                params.height = dpToPx(60) + systemBars.bottom
                nav.layoutParams = params
                nav.setPadding(0, 0, 0, systemBars.bottom)
            }

            // Adjust FAB margin for nav bar
            fabAddNote.let { fab ->
                val params = fab.layoutParams as android.widget.FrameLayout.LayoutParams
                params.bottomMargin = dpToPx(16) + dpToPx(60) + systemBars.bottom
                fab.layoutParams = params
            }

            insets
        }

        setupMap()
        setupControls()
        setupBottomNavigation()
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
        mapView.setBuiltInZoomControls(false)

        // Initial map: centered on Japan (like the mockup shows)
        val mapController = mapView.controller
        mapController.setZoom(5.5)
        mapController.setCenter(GeoPoint(36.0, 138.0))
    }

    private fun setupControls() {
        findViewById<View>(R.id.btnZoomIn).setOnClickListener {
            mapView.controller.zoomIn()
        }
        findViewById<View>(R.id.btnZoomOut).setOnClickListener {
            mapView.controller.zoomOut()
        }
        findViewById<View>(R.id.btnMyLocation).setOnClickListener {
            if (hasLocationPermission()) {
                val locationManager =
                    getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                try {
                    val lastKnown =
                        locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    if (lastKnown != null) {
                        mapView.controller.animateTo(
                            GeoPoint(lastKnown.latitude, lastKnown.longitude)
                        )
                        mapView.controller.setZoom(15.0)
                    } else {
                        Toast.makeText(this, "ไม่พบตำแหน่งปัจจุบัน", Toast.LENGTH_SHORT).show()
                    }
                } catch (_: SecurityException) {
                    Toast.makeText(this, "ไม่มีสิทธิ์เข้าถึงตำแหน่ง", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestPermissionsIfNeeded()
            }
        }
        fabAddNote.setOnClickListener {
            Toast.makeText(this, "เพิ่มโน้ตใหม่", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_explore
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> true
                R.id.nav_memories -> {
                    startActivity(Intent(this, MemoriesActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Creates a beautiful custom marker matching the mockup:
     * - Rounded thumbnail card with gradient + shadow
     * - White pill label below with location name
     * - Small triangle pointer at bottom
     */
    private fun createBeautifulMarker(label: String, colors: Pair<Int, Int>): BitmapDrawable {
        val density = resources.displayMetrics.density

        // Sizes in pixels
        val cardW = (80 * density).toInt()
        val cardH = (70 * density).toInt()
        val pillH = (24 * density).toInt()
        val pointerH = (8 * density).toInt()
        val padding = (8 * density).toInt()
        val totalW = cardW + padding * 2
        val totalH = cardH + pillH + pointerH + padding * 2

        val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = totalW / 2f

        // === Card shadow ===
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.TRANSPARENT
            setShadowLayer(6 * density, 0f, 3 * density, Color.parseColor("#40000000"))
        }
        val cardLeft = padding.toFloat()
        val cardTop = padding.toFloat()
        val cardRight = (totalW - padding).toFloat()
        val cardBottom = (padding + cardH).toFloat()
        val cornerR = 14 * density
        canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, cornerR, cornerR, shadowPaint)

        // === Card white border ===
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, cornerR, cornerR, borderPaint)

        // === Inner image with gradient ===
        val inset = 4 * density
        val imgRect = RectF(
            cardLeft + inset, cardTop + inset,
            cardRight - inset, cardBottom - inset
        )
        val imgCorner = 10 * density

        val gradPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                imgRect.left, imgRect.top,
                imgRect.right, imgRect.bottom,
                colors.first, colors.second,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(imgRect, imgCorner, imgCorner, gradPaint)

        // === Subtle overlay on image ===
        val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, imgRect.centerY(),
                0f, imgRect.bottom,
                Color.TRANSPARENT,
                Color.parseColor("#55000000"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(imgRect, imgCorner, imgCorner, overlayPaint)

        // === Small text inside image ===
        val innerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10 * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(2 * density, 0f, 1 * density, Color.parseColor("#66000000"))
        }
        canvas.drawText(label, cx, imgRect.bottom - 6 * density, innerText)

        // === White pill label ===
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A2E")
            textSize = 11 * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textW = labelPaint.measureText(label)
        val pillPadH = 14 * density
        val pillWidth = textW + pillPadH * 2
        val pillLeft = cx - pillWidth / 2
        val pillTop = cardBottom + 4 * density
        val pillBottom = pillTop + pillH
        val pillRect = RectF(pillLeft, pillTop, pillLeft + pillWidth, pillBottom)

        val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(3 * density, 0f, 1 * density, Color.parseColor("#22000000"))
        }
        canvas.drawRoundRect(pillRect, pillH / 2f, pillH / 2f, pillBg)
        canvas.drawText(label, cx, pillBottom - 7 * density, labelPaint)

        // === Small pointer triangle ===
        val triPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val triSize = 5 * density
        val path = android.graphics.Path().apply {
            moveTo(cx - triSize, pillBottom - 1)
            lineTo(cx, pillBottom + pointerH - 2 * density)
            lineTo(cx + triSize, pillBottom - 1)
            close()
        }
        canvas.drawPath(path, triPaint)

        return BitmapDrawable(resources, bitmap)
    }

    // =====================
    // Permissions
    // =====================

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

    // =====================
    // Lifecycle
    // =====================

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    // =====================
    // Utilities
    // =====================

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
