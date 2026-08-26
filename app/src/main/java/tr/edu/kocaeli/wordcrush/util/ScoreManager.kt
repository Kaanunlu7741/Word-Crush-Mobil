package tr.edu.kocaeli.wordcrush.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Proje raporunda istenen bilgileri tutan kalıbımız
// OyunGecmisi.kt içindeki data class'ı bununla değiştir:
data class OyunGecmisi(
    val oyunNumarasi: Int,
    val kullaniciAdi: String,
    val tarih: String,
    val gridBoyutu: String,
    val puan: Int,
    val bulunanKelimeler: String, // 🚨 ARTIK SAYI DEĞİL, KELİMELERİN KENDİSİNİ TUTUYORUZ
    val enUzunKelime: String,
    val sureSaniye: Int
) {
    fun formatliSureGetir(): String {
        val dakika = sureSaniye / 60
        val saniye = sureSaniye % 60
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", dakika, saniye)
    }
}

class SkorManager(context: Context) {
    private val prefs = context.getSharedPreferences("WordCrushSkorlar", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Yeni biten oyunu listeye ekleyip JSON olarak kaydeder
    fun oyunKaydet(yeniOyun: OyunGecmisi) {
        val gecmisListesi = gecmisOyunlariGetir().toMutableList()
        gecmisListesi.add(0, yeniOyun) // En son oynanan oyun en üste gelsin

        val json = gson.toJson(gecmisListesi)
        prefs.edit().putString("GECMIS_OYUNLAR", json).apply()
    }

    // Kayıtlı oyunları JSON'dan tekrar listeye dönüştürüp getirir
    fun gecmisOyunlariGetir(): List<OyunGecmisi> {
        val json = prefs.getString("GECMIS_OYUNLAR", null) ?: return emptyList()
        val type = object : TypeToken<List<OyunGecmisi>>() {}.type
        return gson.fromJson(json, type)
    }

    // Bugünün tarihini formatlı olarak verir
    fun bugununTarihi(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}