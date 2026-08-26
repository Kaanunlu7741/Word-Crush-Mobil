package tr.edu.kocaeli.wordcrush
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val btnKullaniciAdi = findViewById<Button>(R.id.btnKullaniciAdi)
        val btnYeniOyun = findViewById<Button>(R.id.btnYeniOyun)
        val btnSkorTablosu = findViewById<Button>(R.id.btnSkorTablosu)
        val btnMarket = findViewById<Button>(R.id.btnMarket)
        val btnNasilOynanir = findViewById<Button>(R.id.btnNasilOynanir)
        val llMenuHeader = findViewById<android.view.View>(R.id.llMenuHeader)

        llMenuHeader.alpha = 0f
        llMenuHeader.translationY = -50f
        llMenuHeader.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        val buttons = listOf(btnYeniOyun, btnSkorTablosu, btnMarket, btnNasilOynanir)
        buttons.forEachIndexed { index, btn ->
            btn.alpha = 0f
            btn.translationX = -100f
            btn.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(200L + (index * 150L))
                .setDuration(600)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .start()
        }
        val sharedPref = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val kullaniciAdi = sharedPref.getString("KULLANICI_ADI", "Oyuncu")
        btnKullaniciAdi.text = "👤 $kullaniciAdi"

        fun applyFastPopEffect(button: Button, onEnd: () -> Unit) {
            button.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(70)
                .withEndAction {
                    button.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(70)
                        .start()
                    onEnd()
                }
                .start()
        }

        btnKullaniciAdi.setOnClickListener {
            applyFastPopEffect(btnKullaniciAdi) {
                Toast.makeText(this, "Çıkış Yapılıyor...", Toast.LENGTH_SHORT).show()
                sharedPref.edit().remove("KULLANICI_ADI").apply()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        btnYeniOyun.setOnClickListener {
            applyFastPopEffect(btnYeniOyun) {
                val intent = Intent(this, SecimEkraniActivity::class.java)
                startActivity(intent)
            }
        }
        btnSkorTablosu.setOnClickListener {
            applyFastPopEffect(btnSkorTablosu) {
                val intent = Intent(this, SkorTablosuActivity::class.java)
                startActivity(intent)
            }
        }
        btnMarket.setOnClickListener {
            applyFastPopEffect(btnMarket) {
                val intent = Intent(this, MarketActivity::class.java)
                startActivity(intent)
            }
        }
        btnNasilOynanir.setOnClickListener {
            applyFastPopEffect(btnNasilOynanir) {
                val intent = Intent(this, HowToPlayActivity::class.java)
                startActivity(intent)
            }
        }
    }

}
