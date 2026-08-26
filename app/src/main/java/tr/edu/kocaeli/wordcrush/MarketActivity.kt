package tr.edu.kocaeli.wordcrush

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MarketActivity : AppCompatActivity() {

    private var mevcutAltin = 1
    private var isAnimating = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)

        val tvAltin = findViewById<TextView>(R.id.tvAltin)

        val ayarlar = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val aktifKullanici = ayarlar.getString("KULLANICI_ADI", "Oyuncu")
        val sharedPref = getSharedPreferences("WordCrushMarket_$aktifKullanici", Context.MODE_PRIVATE)
        mevcutAltin = sharedPref.getInt("KULLANICI_ALTIN", 9999999)
        tvAltin.text = "💰 Bakiye: $mevcutAltin Altın"
        val btnBalikAl = findViewById<Button>(R.id.btnBalikAl)
        val btnIsinKiliciAl = findViewById<Button>(R.id.btnIsinKiliciAl)
        val btnKeskinNisanciAl = findViewById<Button>(R.id.btnKeskinNisanciAl)
        val btnDegistirmeAl = findViewById<Button>(R.id.btnDegistirmeAl)
        val btnKaristirmaAl = findViewById<Button>(R.id.btnKaristirmaAl)
        val btnPartiAl = findViewById<Button>(R.id.btnPartiAl)
        val btnIpucuAl = findViewById<Button>(R.id.btnIpucuAl)

        btnBalikAl.setOnClickListener { satinAl("Joker_Balik", 100, btnBalikAl) }
        btnIsinKiliciAl.setOnClickListener { satinAl("Joker_IsinKilici", 200, btnIsinKiliciAl) }
        btnKeskinNisanciAl.setOnClickListener { satinAl("Joker_KeskinNisanci", 75, btnKeskinNisanciAl) }
        btnDegistirmeAl.setOnClickListener { satinAl("Joker_Degistirme", 125, btnDegistirmeAl) }
        btnKaristirmaAl.setOnClickListener { satinAl("Joker_Karistirma", 300, btnKaristirmaAl) }
        btnPartiAl.setOnClickListener { satinAl("Joker_Parti", 400, btnPartiAl) }
        btnIpucuAl.setOnClickListener { satinAl("Joker_Ipucu", 150, btnIpucuAl) }

        findViewById<Button>(R.id.btnFishWatch).setOnClickListener { simulateFish() }
        findViewById<Button>(R.id.btnIsinKiliciWatch).setOnClickListener { simulateIsinKilici() }
        findViewById<Button>(R.id.btnKeskinNisanciWatch).setOnClickListener { simulateKeskinNisanci() }
        findViewById<Button>(R.id.btnSwapWatch).setOnClickListener { simulateSwap() }
        findViewById<Button>(R.id.btnShuffleWatch).setOnClickListener { simulateShuffle() }
        findViewById<Button>(R.id.btnPartyWatch).setOnClickListener { simulateParty() }
        findViewById<Button>(R.id.btnIpucuWatch).setOnClickListener { simulateIpucu() }

        val btnMarketGeri = findViewById<Button>(R.id.btnMarketGeri)
        btnMarketGeri.setOnClickListener {
            butonEfektiVer(btnMarketGeri, true)
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 150)
        }
        setupAllSimGrids()
        jokerSayilariniGuncelle()
    }

    private fun setupAllSimGrids() {
        setupMiniGrid(findViewById(R.id.glFishSimMarket), "F")
        setupMiniGrid(findViewById(R.id.glIsinKiliciSimMarket), "H")
        setupMiniGrid(findViewById(R.id.glKeskinNisanciSimMarket), "S")
        setupMiniGrid(findViewById(R.id.glSwapSimMarket), "W")
        setupMiniGrid(findViewById(R.id.glShuffleSimMarket), "R")
        setupMiniGrid(findViewById(R.id.glPartySimMarket), "P")
        setupMiniGrid(findViewById(R.id.glIpucuSimMarket), "I")
    }

    private fun setupMiniGrid(gl: GridLayout, text: String) {
        gl.removeAllViews()
        val harfHavuzu = listOf("A", "E", "I", "İ", "L", "R", "N", "K", "M", "T", "S", "Y")
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val tv = TextView(this)
                tv.layoutParams = GridLayout.LayoutParams().apply {
                    width = 60; height = 60; setMargins(2, 2, 2, 2)
                }
                tv.background = ContextCompat.getDrawable(this, R.drawable.bg_hucre_modern)
                tv.text = if (i == 1 && j == 1 && (text == "H" || text == "S")) "🎯" else harfHavuzu.random()
                tv.gravity = Gravity.CENTER
                tv.setTextColor(Color.parseColor("#2C3E50"))
                tv.textSize = 12f
                gl.addView(tv)
            }
        }
    }

    private fun simulateFish() {
        if (isAnimating) return
        isAnimating = true
        val gl = findViewById<GridLayout>(R.id.glFishSimMarket)
        val indices = listOf(0, 2, 4, 6, 8).shuffled()
        indices.forEachIndexed { i, idx ->
            gl.getChildAt(idx).animate().scaleX(0f).scaleY(0f).alpha(0f).setStartDelay(i * 100L).setDuration(250).start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            setupMiniGrid(gl, "F")
            isAnimating = false
        }, 1200)
    }

    private fun simulateIsinKilici() {
        if (isAnimating) return
        isAnimating = true
        val gl = findViewById<GridLayout>(R.id.glIsinKiliciSimMarket)
        val previewContainer = gl.parent as ViewGroup
        val centerCell = gl.getChildAt(4)
        val targetX = centerCell.x + (centerCell.width / 2f)
        val targetY = centerCell.y + (centerCell.height / 2f)
        val hLaser = View(this)
        hLaser.setBackgroundColor(Color.RED)
        hLaser.layoutParams = FrameLayout.LayoutParams(gl.width, 8)
        hLaser.x = gl.x; hLaser.y = gl.y + targetY - 4f; hLaser.scaleX = 0f; hLaser.elevation = 2000f
        val vLaser = View(this)
        vLaser.setBackgroundColor(Color.RED)
        vLaser.layoutParams = FrameLayout.LayoutParams(8, gl.height)
        vLaser.x = gl.x + targetX - 4f; vLaser.y = gl.y; vLaser.scaleY = 0f; vLaser.elevation = 2000f

        previewContainer.addView(hLaser)
        previewContainer.addView(vLaser)

        hLaser.animate().scaleX(1f).setDuration(400).start()
        vLaser.animate().scaleY(1f).setDuration(400).withEndAction {
            for (i in 0 until gl.childCount) {
                val row = i / 3
                val col = i % 3
                if (row == 1 || col == 1) {
                    gl.getChildAt(i).animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(200).start()
                }
            }
            
            Handler(Looper.getMainLooper()).postDelayed({
                hLaser.animate().alpha(0f).setDuration(200).withEndAction { previewContainer.removeView(hLaser) }.start()
                vLaser.animate().alpha(0f).setDuration(200).withEndAction { 
                    previewContainer.removeView(vLaser)
                    setupMiniGrid(gl, "H")
                    isAnimating = false
                }.start()
            }, 500)
        }.start()
    }

    private fun simulateKeskinNisanci() {
        if (isAnimating) return
        isAnimating = true
        val gl = findViewById<GridLayout>(R.id.glKeskinNisanciSimMarket)
        val target = gl.getChildAt(4)
        val root = findViewById<ViewGroup>(android.R.id.content)
        val koor = IntArray(2); target.getLocationOnScreen(koor)
        val rootKoor = IntArray(2); root.getLocationOnScreen(rootKoor)

        val size = 80
        val crosshair = View(this)
        val bg = GradientDrawable(); bg.shape = GradientDrawable.OVAL; bg.setStroke(3, Color.RED)
        crosshair.background = bg
        crosshair.layoutParams = FrameLayout.LayoutParams(size, size)
        crosshair.x = (koor[0] - rootKoor[0]).toFloat() + (target.width / 2f) - (size / 2f)
        crosshair.y = (koor[1] - rootKoor[1]).toFloat() + (target.height / 2f) - (size / 2f)
        crosshair.alpha = 0f; crosshair.scaleX = 2f; crosshair.scaleY = 2f
        root.addView(crosshair)

        crosshair.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).withEndAction {
            Handler(Looper.getMainLooper()).postDelayed({
                target.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(200).start()
                crosshair.animate().scaleX(1.5f).scaleY(1.5f).alpha(0f).setDuration(200).withEndAction {
                    root.removeView(crosshair)
                    setupMiniGrid(gl, "S")
                    isAnimating = false
                }.start()
            }, 400)
        }.start()
    }

    private fun simulateSwap() {
        if (isAnimating) return
        isAnimating = true
        val gl = findViewById<GridLayout>(R.id.glSwapSimMarket)
        val cell1 = gl.getChildAt(4)
        val cell2 = gl.getChildAt(5)
        
        val move = cell1.width + 4f
        cell1.animate().translationX(move).setDuration(400).start()
        cell2.animate().translationX(-move).setDuration(400).withEndAction {
            Handler(Looper.getMainLooper()).postDelayed({
                cell1.translationX = 0f; cell2.translationX = 0f
                isAnimating = false
            }, 600)
        }.start()
    }

    private fun simulateShuffle() {
        if (isAnimating) return
        isAnimating = true
        val gl = findViewById<GridLayout>(R.id.glShuffleSimMarket)
        val centerChild = gl.getChildAt(4)
        val centerX = centerChild.x
        val centerY = centerChild.y

        for (i in 0 until gl.childCount) {
            val child = gl.getChildAt(i)
            val dx = centerX - child.x
            val dy = centerY - child.y
            child.animate()
                .translationX(dx)
                .translationY(dy)
                .scaleX(0f)
                .scaleY(0f)
                .rotation(720f)
                .setDuration(450)
                .setInterpolator(AccelerateInterpolator())
                .start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            setupMiniGrid(gl, "R")
            for (i in 0 until gl.childCount) {
                val child = gl.getChildAt(i)
                val dx = centerX - child.x
                val dy = centerY - child.y
                
                child.translationX = dx
                child.translationY = dy
                child.scaleX = 0f
                child.scaleY = 0f
                child.rotation = -720f

                child.animate()
                    .translationX(0f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotation(0f)
                    .setDuration(600)
                    .setStartDelay((Math.random() * 200).toLong())
                    .setInterpolator(OvershootInterpolator(1.3f))
                    .start()
            }
            Handler(Looper.getMainLooper()).postDelayed({ isAnimating = false }, 800)
        }, 500)
    }

    private fun simulateParty() {
        if (isAnimating) return
        isAnimating = true
        val gl = findViewById<GridLayout>(R.id.glPartySimMarket)
        for (i in 0 until gl.childCount) {
            val child = gl.getChildAt(i)
            child.animate().translationY(200f).alpha(0f).setStartDelay(i * 50L).setDuration(300).start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            for (i in 0 until gl.childCount) {
                val child = gl.getChildAt(i)
                child.translationY = -200f
                child.animate().translationY(0f).alpha(1f).setStartDelay(i * 50L).setDuration(400).setInterpolator(OvershootInterpolator()).start()
            }
            isAnimating = false
        }, 1000)
    }

    private fun simulateIpucu() {
        if (isAnimating) return
        isAnimating = true
        val gl = findViewById<GridLayout>(R.id.glIpucuSimMarket)
        val sampleWord = "ELMA"
        val shuffled = sampleWord.toList().shuffled()
        
        // Simülasyon için 3x3 gridin ortasına kelimeyi yazalım
        for (i in 0 until gl.childCount) {
            val child = gl.getChildAt(i) as TextView
            child.animate().alpha(0f).scaleX(0f).scaleY(0f).setDuration(200).start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val positions = listOf(1, 3, 5, 7) // Artı şeklinde yerleşim
            positions.forEachIndexed { index, pos ->
                val child = gl.getChildAt(pos) as TextView
                child.text = shuffled[index].toString()
                child.setTextColor(Color.parseColor("#E67E22"))
                child.animate().alpha(1f).scaleX(1.2f).scaleY(1.2f).setDuration(300).withEndAction {
                    child.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }.start()
            }
            
            Handler(Looper.getMainLooper()).postDelayed({
                setupMiniGrid(gl, "I")
                isAnimating = false
            }, 1500)
        }, 300)
    }

    private fun butonEfektiVer(button: View, basarili: Boolean) {
        if (basarili) {
            button.animate().scaleX(1.03f).scaleY(1.03f).alpha(0.7f).setDuration(80).withEndAction {
                button.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(120)
                    .setInterpolator(OvershootInterpolator()).start()
            }.start()
        } else {
            button.animate().translationX(15f).setDuration(40).withEndAction {
                button.animate().translationX(-15f).setDuration(40).withEndAction {
                    button.animate().translationX(0f).setDuration(40).start()
                }.start()
            }.start()
        }
    }

    private fun jokerSayilariniGuncelle() {
        val ayarlar = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val aktifKullanici = ayarlar.getString("KULLANICI_ADI", "Oyuncu")
        val sharedPref = getSharedPreferences("WordCrushMarket_$aktifKullanici", Context.MODE_PRIVATE)

        findViewById<TextView>(R.id.tvMevcutBalik).text = "x${sharedPref.getInt("Joker_Balik", 0)}"
        findViewById<TextView>(R.id.tvMevcutIsinKilici).text = "x${sharedPref.getInt("Joker_IsinKilici", 0)}"
        findViewById<TextView>(R.id.tvMevcutKeskinNisanci).text = "x${sharedPref.getInt("Joker_KeskinNisanci", 0)}"
        findViewById<TextView>(R.id.tvMevcutDegistirme).text = "x${sharedPref.getInt("Joker_Degistirme", 0)}"
        findViewById<TextView>(R.id.tvMevcutKaristirma).text = "x${sharedPref.getInt("Joker_Karistirma", 0)}"
        findViewById<TextView>(R.id.tvMevcutParti).text = "x${sharedPref.getInt("Joker_Parti", 0)}"
        findViewById<TextView>(R.id.tvMevcutIpucu).text = "x${sharedPref.getInt("Joker_Ipucu", 0)}"
    }

    private fun satinAl(jokerKey: String, fiyat: Int, button: View) {
        if (mevcutAltin >= fiyat) {
            val ayarlar = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
            val aktifKullanici = ayarlar.getString("KULLANICI_ADI", "Oyuncu")
            val sharedPref = getSharedPreferences("WordCrushMarket_$aktifKullanici", Context.MODE_PRIVATE)

            mevcutAltin -= fiyat
            sharedPref.edit().putInt("KULLANICI_ALTIN", mevcutAltin).apply()
            findViewById<TextView>(R.id.tvAltin).text = "💰 Bakiye: $mevcutAltin Altın"

            val mevcutJokerSayisi = sharedPref.getInt(jokerKey, 0)
            sharedPref.edit().putInt(jokerKey, mevcutJokerSayisi + 1).apply()

            jokerSayilariniGuncelle()
            butonEfektiVer(button, true)
        } else {
            butonEfektiVer(button, false)
            Toast.makeText(this, "Yeterli altınınız yok!", Toast.LENGTH_SHORT).show()
        }
    }
}
