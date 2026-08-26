package tr.edu.kocaeli.wordcrush

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity

class SecimEkraniActivity : AppCompatActivity() {

    private var secilenBoyut = 8
    private var secilenHamle = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secim_ekrani)

        val btnBoyut6 = findViewById<Button>(R.id.btnBoyut6)
        val btnBoyut8 = findViewById<Button>(R.id.btnBoyut8)
        val btnBoyut10 = findViewById<Button>(R.id.btnBoyut10)
        val boyutButonlari = listOf(btnBoyut6, btnBoyut8, btnBoyut10)

        val btnHamle15 = findViewById<Button>(R.id.btnHamle15)
        val btnHamle20 = findViewById<Button>(R.id.btnHamle20)
        val btnHamle25 = findViewById<Button>(R.id.btnHamle25)
        val hamleButonlari = listOf(btnHamle15, btnHamle20, btnHamle25)

        val btnOyunuBaslat = findViewById<Button>(R.id.btnOyunuBaslat)
        val btnAnaMenuyeDon = findViewById<Button>(R.id.btnAnaMenuyeDon)

        btnBoyut6.setOnClickListener {
            secilenBoyut = 6
            butonSecimGorseliniGuncelle(btnBoyut6, boyutButonlari, R.drawable.btn_modern_turuncu)
            gridOnizlemeOlustur(secilenBoyut)
        }
        btnBoyut8.setOnClickListener {
            secilenBoyut = 8
            butonSecimGorseliniGuncelle(btnBoyut8, boyutButonlari, R.drawable.btn_modern_turuncu)
            gridOnizlemeOlustur(secilenBoyut)
        }
        btnBoyut10.setOnClickListener {
            secilenBoyut = 10
            butonSecimGorseliniGuncelle(btnBoyut10, boyutButonlari, R.drawable.btn_modern_turuncu)
            gridOnizlemeOlustur(secilenBoyut)
        }

        btnHamle15.setOnClickListener {
            secilenHamle = 15
            butonSecimGorseliniGuncelle(btnHamle15, hamleButonlari, R.drawable.btn_modern_mor)
        }
        btnHamle20.setOnClickListener {
            secilenHamle = 20
            butonSecimGorseliniGuncelle(btnHamle20, hamleButonlari, R.drawable.btn_modern_mor)
        }
        btnHamle25.setOnClickListener {
            secilenHamle = 25
            butonSecimGorseliniGuncelle(btnHamle25, hamleButonlari, R.drawable.btn_modern_mor)
        }

        btnOyunuBaslat.setOnClickListener {
            applyPopEffect(btnOyunuBaslat) {
                val intent = Intent(this, OyunEkraniActivity::class.java)
                intent.putExtra("SECILEN_BOYUT", secilenBoyut)
                intent.putExtra("SECILEN_HAMLE", secilenHamle)
                startActivity(intent)
                finish()
            }
        }

        btnAnaMenuyeDon.setOnClickListener {
            applyPopEffect(btnAnaMenuyeDon) {
                finish()
            }
        }

        val gridOnizlemeLayout = findViewById<GridLayout>(R.id.glGridOnizleme)
        gridOnizlemeLayout.post {
            gridOnizlemeOlustur(secilenBoyut)
        }
    }

    private fun applyPopEffect(view: View, onEnd: () -> Unit) {
        view.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction { onEnd() }
                    .start()
            }
            .start()
    }

    private fun butonSecimGorseliniGuncelle(secilenBtn: Button, tumButonlar: List<Button>, aktifArkaplan: Int) {
        for (btn in tumButonlar) {
            if (btn == secilenBtn) {
                btn.setBackgroundResource(aktifArkaplan)
            } else {
                btn.setBackgroundResource(R.drawable.bg_panel_modern)
            }
        }
    }

    private fun gridOnizlemeOlustur(boyut: Int) {
        val gridLayout = findViewById<GridLayout>(R.id.glGridOnizleme)
        gridLayout.removeAllViews()

        gridLayout.rowCount = boyut
        gridLayout.columnCount = boyut

        val density = resources.displayMetrics.density

        val cellSizeDp = when (boyut) {
            6 -> 30f
            8 -> 22f
            10 -> 16f
            else -> 20f
        }
        val cellSizePx = (cellSizeDp * density).toInt()
        val marginPx = (1f * density).toInt()

        for (i in 0 until boyut) {
            for (j in 0 until boyut) {
                val cell = View(this)

                val bg = GradientDrawable()
                bg.setColor(Color.parseColor("#FFFFFF"))
                bg.cornerRadius = 3f * density
                cell.background = bg

                val params = GridLayout.LayoutParams(
                    GridLayout.spec(i, GridLayout.CENTER),
                    GridLayout.spec(j, GridLayout.CENTER)
                )
                params.width = cellSizePx
                params.height = cellSizePx
                params.setMargins(marginPx, marginPx, marginPx, marginPx)

                gridLayout.addView(cell, params)
            }
        }

        gridLayout.requestLayout()

        gridLayout.alpha = 0f
        gridLayout.scaleX = 0.8f
        gridLayout.scaleY = 0.8f
        gridLayout.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(OvershootInterpolator())
            .start()
    }
}
