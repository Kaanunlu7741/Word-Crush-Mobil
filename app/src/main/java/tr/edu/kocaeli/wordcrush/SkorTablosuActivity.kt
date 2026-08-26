package tr.edu.kocaeli.wordcrush

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import tr.edu.kocaeli.wordcrush.util.SkorManager

class SkorTablosuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skor_tablosu)

        val skorManager = SkorManager(this)
        val gecmisOyunlar = skorManager.gecmisOyunlariGetir()

        val tvOzetToplamOyun = findViewById<TextView>(R.id.tvOzetToplamOyun)
        val tvOzetEnYuksek = findViewById<TextView>(R.id.tvOzetEnYuksek)
        val tvOzetOrtalama = findViewById<TextView>(R.id.tvOzetOrtalama)
        val tvOzetToplamKelime = findViewById<TextView>(R.id.tvOzetToplamKelime)
        val tvOzetEnUzun = findViewById<TextView>(R.id.tvOzetEnUzun)
        val tvOzetToplamSure = findViewById<TextView>(R.id.tvOzetToplamSure)
        val llOyunListesi = findViewById<LinearLayout>(R.id.llOyunListesi)

        if (gecmisOyunlar.isEmpty()) {
            val uyari = TextView(this)
            uyari.text = "Henüz oynanmış bir oyun yok."
            uyari.setTextColor(Color.WHITE)
            llOyunListesi.addView(uyari)
            return
        }

        var toplamPuan = 0
        var enYuksek = 0
        var bizzatBulunanKelimeSayisi = 0
        var enUzun = ""
        var toplamSureSaniye = 0

        for (oyun in gecmisOyunlar) {
            toplamPuan += oyun.puan
            toplamSureSaniye += oyun.sureSaniye

            if (oyun.bulunanKelimeler != "-" && oyun.bulunanKelimeler.isNotBlank()) {
                bizzatBulunanKelimeSayisi += oyun.bulunanKelimeler.split(", ").size
            }

            if (oyun.puan > enYuksek) enYuksek = oyun.puan
            if (oyun.enUzunKelime.length > enUzun.length && oyun.enUzunKelime != "-") {
                enUzun = oyun.enUzunKelime
            }
        }
        val ortalama = if (gecmisOyunlar.isNotEmpty()) toplamPuan / gecmisOyunlar.size else 0
        val ozetDakika = toplamSureSaniye / 60
        val ozetSaniye = toplamSureSaniye % 60
        val formatliToplamSure = String.format("%02d:%02d", ozetDakika, ozetSaniye)

        tvOzetToplamOyun.text = "Toplam Oyun: ${gecmisOyunlar.size}"
        tvOzetEnYuksek.text = "En Yüksek Puan: $enYuksek"
        tvOzetOrtalama.text = "Ortalama Puan: $ortalama"
        tvOzetToplamKelime.text = "Bulunan Kelimeler: $bizzatBulunanKelimeSayisi"
        tvOzetEnUzun.text = "En Uzun Kelime: ${if (enUzun.isEmpty()) "-" else enUzun}"
        tvOzetToplamSure.text = "Toplam Süre: ⏱️ $formatliToplamSure"

        for (oyun in gecmisOyunlar) {
            val kart = TextView(this)
            val oOyundakiKelimeSayisi = if (oyun.bulunanKelimeler != "-" && oyun.bulunanKelimeler.isNotBlank()) {
                oyun.bulunanKelimeler.split(", ").size
            } else {
                0
            }
            val kartMetni = """
                Oyun ${oyun.oyunNumarasi} | Oyuncu: 👤 ${oyun.kullaniciAdi}
                Tarih: ${oyun.tarih} | Grid: ${oyun.gridBoyutu}
                Puan: ${oyun.puan} | Kelime Sayısı: $oOyundakiKelimeSayisi
                Kelimeler: ${oyun.bulunanKelimeler}
                En Uzun: "${oyun.enUzunKelime}"
                Süre: ⏱️ ${oyun.formatliSureGetir()}
            """.trimIndent()

            kart.text = kartMetni
            kart.setTextColor(Color.WHITE)
            kart.setBackgroundColor(Color.parseColor("#34495E"))
            kart.setPadding(32, 32, 32, 32)
            kart.textSize = 15f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            kart.layoutParams = params

            llOyunListesi.addView(kart)
        }
    }
}