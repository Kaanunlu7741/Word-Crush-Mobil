package tr.edu.kocaeli.wordcrush

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import tr.edu.kocaeli.wordcrush.model.GridManager
import tr.edu.kocaeli.wordcrush.model.Trie
import kotlin.math.abs

class OyunEkraniActivity : AppCompatActivity() {
    private lateinit var sozluk: Trie
    private lateinit var yonetici: GridManager
    private var boyut = 10
    private var kalanHamle = 25
    private var toplamSkor = 0
    private val oyuncuKelimeleri = mutableListOf<String>()
    private lateinit var tvSkor: TextView
    private lateinit var tvHamle: TextView
    private lateinit var tvSecilenKelime: TextView
    private lateinit var tvIpucu: TextView
    private lateinit var tvHedefSayaci: TextView
    private lateinit var gridLayout: GridLayout
    private val hucreGorselleri = Array(10) { arrayOfNulls<TextView>(10) }
    private val secilenGorseller = mutableListOf<TextView>()
    private val secilenKelime = StringBuilder()
    private var sonSatir = -1
    private var sonSutun = -1
    private var animasyonOynuyor = false
    private val oyunBaslangicZamani = System.currentTimeMillis()
    private var bulunanKelimeSayisi = 0
    private var enUzunBulunanKelime = ""
    private lateinit var btnOyunBalik: Button
    private lateinit var btnOyunIsinKilici: Button
    private lateinit var btnOyunKeskinNisanci: Button
    private lateinit var btnOyunDegistirme: Button
    private lateinit var btnOyunKaristirma: Button
    private lateinit var btnOyunParti: Button
    private lateinit var btnOyunIpucu: Button
    private var aktifJokerTipi: String = ""
    private var ilkDegistirmeHucresi: TextView? = null
    private lateinit var tvAltin: TextView
    private lateinit var btnOyunIciMarket: LinearLayout
    private var mevcutHedefSayisi = -1
    private var mevcutIpucuKelimesi: String? = null
    private var saniyeCinsindenSure = 0
    private var zamanlayiciHandler: Handler? = null
    private var zamanlayiciRunnable: Runnable? = null
    private var zamanIsliyor = false
    private lateinit var tvSure: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oyun_ekrani)
        tvSkor = findViewById(R.id.tvSkor)
        tvHamle = findViewById(R.id.tvHamle)
        tvHedefSayaci = findViewById(R.id.tvHedefSayaci)
        tvAltin = findViewById(R.id.tvAltin)
        btnOyunIciMarket = findViewById(R.id.btnOyunIciMarket)
        tvSecilenKelime = findViewById(R.id.tvSecilenKelime)
        tvIpucu = findViewById(R.id.tvIpucu)
        gridLayout = findViewById(R.id.gridLayout)
        tvSure = findViewById(R.id.tvSure)
        val btnAnaMenuyeDon = findViewById<Button>(R.id.btnAnaMenuyeDon)
        btnAnaMenuyeDon.setOnClickListener {
            btnAnaMenuyeDon.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction {
                    btnAnaMenuyeDon.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .withEndAction {
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Ana Menüye Dön")
                                .setMessage("Oyundan çıkmak istediğinize emin misiniz? Mevcut sonucunuz kaydedilecektir.")
                                .setPositiveButton("Evet") { _, _ ->
                                    skoruKaydetVeCik()
                                    val intent = Intent(this, MenuActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                }
                                .setNegativeButton("Hayır", null)
                                .show()
                        }.start()
                }.start()
        }
        boyut = intent.getIntExtra("SECILEN_BOYUT", 10)
        kalanHamle = intent.getIntExtra("SECILEN_HAMLE", 25)
        tvHamle.text = kalanHamle.toString()
        btnOyunIciMarket.setOnClickListener {
            btnOyunIciMarket.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .withEndAction {
                    btnOyunIciMarket.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .withEndAction {
                            val intent = Intent(this, MarketActivity::class.java)
                            startActivity(intent)
                        }
                        .start()
                }
                .start()
        }
        sozluguYukle()
        jokerleriYukleVeBagla()
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                androidx.appcompat.app.AlertDialog.Builder(this@OyunEkraniActivity)
                    .setTitle("Oyundan Çıkış")
                    .setMessage("Oyundan çıkmak istediğinize emin misiniz? Mevcut sonucunuz skor tablosuna yazılacaktır.")
                    .setPositiveButton("Evet") { _, _ ->
                        skoruKaydetVeCik()
                    }
                    .setNegativeButton("Hayır", null)
                    .show()
            }
        })
        sureyiSifirla()
        sureyiBaslat()
    }
    private fun sozluguYukle() {
        sozluk = Trie()
        try {
            val inputStream = assets.open("kelimeler.txt")
            sozluk.streamDenYukle(inputStream)
            yonetici = GridManager(boyut, sozluk)
            gridiCiz()
            suruklemeMekanizmasiniKur()
            kelimeListeleriniGuncelle()
            baslangicHedefAnimasyonu("HEDEF KELİME\nGİZLİ!")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Sözlük yüklenirken hata oluştu!", Toast.LENGTH_LONG).show()
        }
    }
    private fun baslangicHedefAnimasyonu(ekMetin: String = "HEDEF KELİME\nGİZLİ!", showCount: Boolean = true) {
        mevcutHedefSayisi = yonetici.kalanTohumKelimeler.size
        val hedefSayisi = yonetici.kalanTohumKelimeler.size
        tvHedefSayaci.text = "🎯 Kalan Hedef: $hedefSayisi"
        val root = findViewById<ViewGroup>(android.R.id.content)
        val devYazi = TextView(this)
        devYazi.text = if (showCount) "🎯\n$hedefSayisi $ekMetin" else "💡\n$ekMetin"
        devYazi.textSize = 32f
        devYazi.setTextColor(Color.WHITE)
        devYazi.gravity = Gravity.CENTER
        devYazi.setTypeface(null, android.graphics.Typeface.BOLD)
        devYazi.setShadowLayer(15f, 0f, 0f, Color.parseColor("#E67E22"))
        val frameParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        frameParams.gravity = Gravity.CENTER
        devYazi.layoutParams = frameParams
        root.addView(devYazi)
        devYazi.scaleX = 0f
        devYazi.scaleY = 0f
        devYazi.alpha = 0f
        animasyonOynuyor = true
        devYazi.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                Handler(Looper.getMainLooper()).postDelayed({
                    devYazi.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .translationY(-400f)
                        .setDuration(500)
                        .withEndAction {
                            root.removeView(devYazi)
                            animasyonOynuyor = false
                            tvHedefSayaci.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction {
                                tvHedefSayaci.animate().scaleX(1f).scaleY(1f).setDuration(150).setInterpolator(BounceInterpolator()).start()
                            }.start()
                        }
                        .start()
                }, 1200)
            }
            .start()
    }
    private fun gridiCiz() {
        gridLayout.rowCount = boyut
        gridLayout.columnCount = boyut
        gridLayout.removeAllViews()

        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val ekranGenisligi = displayMetrics.widthPixels
        val ekranYuksekligi = displayMetrics.heightPixels

        val marginDp = when {
            boyut <= 6 -> 6f
            boyut <= 8 -> 4f
            else -> 2f
        }
        val paddingDp = when {
            boyut <= 6 -> 8f
            boyut <= 8 -> 6f
            else -> 4f
        }

        // Genişlik bazlı hesaplama
        val maxGridGenisligi = ekranGenisligi - (40 * density).toInt()
        val toplamMarginPiksel = (marginDp * 2 * boyut * density).toInt()
        val toplamPaddingPiksel = (paddingDp * 2 * density).toInt()
        val hucreBoyutuGenislikBazli = (maxGridGenisligi - toplamPaddingPiksel - toplamMarginPiksel) / boyut

        // Yükseklik bazlı hesaplama (Ekranın yaklaşık %45'ini grid için ayırıyoruz)
        val maxGridYuksekligi = (ekranYuksekligi * 0.45).toInt()
        val hucreBoyutuYukseklikBazli = (maxGridYuksekligi - toplamPaddingPiksel - toplamMarginPiksel) / boyut

        // En küçük olanı seçerek gridin ekrana sığmasını sağlıyoruz
        val hucreBoyutu = minOf(hucreBoyutuGenislikBazli, hucreBoyutuYukseklikBazli)

        val marginPiksel = (marginDp * density).toInt()
        for (i in 0 until boyut) {
            for (j in 0 until boyut) {
                val textView = TextView(this)
                textView.gravity = Gravity.CENTER
                textView.tag = "$i,$j"
                textView.includeFontPadding = false

                val params = GridLayout.LayoutParams(
                    GridLayout.spec(i, GridLayout.CENTER),
                    GridLayout.spec(j, GridLayout.CENTER)
                )
                params.width = hucreBoyutu
                params.height = hucreBoyutu
                params.setMargins(marginPiksel, marginPiksel, marginPiksel, marginPiksel)
                
                // Hücrelerin tam ortalanması için yerçekimi ekleyelim
                params.setGravity(Gravity.CENTER)

                textView.layoutParams = params

                hucreGorselleri[i][j] = textView
                gridLayout.addView(textView)
            }
        }
        gridiGuncelle()
    }
    private fun getHucreRengi(puan: Int): String {
        return when (puan) {
            1 -> "#FFFFFF"  // Beyaz (Temel Harfler)
            2 -> "#A9DFBF"  // Yeşil (I, M, O, S, U)
            3 -> "#AED6F1"  // Mavi (B, D, Ü, Y)
            4 -> "#F9E79F"  // Sarı (C, Ç, Ş, Z)
            5 -> "#F5B041"  // Turuncu (G, H, P)
            7 -> "#EC7063"  // Kırmızı (F, Ö, V)
            8 -> "#AF7AC5"  // Mor (Ğ)
            10 -> "#F4D03F" // Altın (J)
            else -> "#FFFFFF"
        }
    }

    private fun gridiGuncelle() {
        for (i in 0 until boyut) {
            for (j in 0 until boyut) {
                val tv = hucreGorselleri[i][j]!!
                tv.animate().cancel()
                tv.scaleX = 1f
                tv.scaleY = 1f
                tv.rotation = 0f
                tv.rotationY = 0f
                tv.alpha = 1f
                tv.translationX = 0f
                tv.translationY = 0f
                val hucreVerisi = yonetici.grid[i][j]
                val harf = hucreVerisi.harf
                val puan = hucreVerisi.puan
                val simge = hucreVerisi.gucSimgesi

                // Yazı boyutunu hücre boyutuna göre dinamik ayarlayalım
                val tvParams = tv.layoutParams
                val hSize = if (tvParams != null && tvParams.width > 0) tvParams.width else 100
                tv.textSize = (hSize / resources.displayMetrics.density) * 0.5f

                val htmlMetin = "<b><font color='#2C3E50'>$harf</font></b><font color='#E67E22'>$simge</font>"
                tv.text = android.text.Html.fromHtml(htmlMetin, android.text.Html.FROM_HTML_MODE_COMPACT)
                
                val arkaplan = (ContextCompat.getDrawable(this, R.drawable.bg_hucre_modern)!!.mutate()) as GradientDrawable
                
                val hucreRengi = getHucreRengi(puan)
                
                arkaplan.setColor(Color.parseColor(hucreRengi))
                arkaplan.setStroke(2, Color.parseColor("#BDC3C7"))
                arkaplan.cornerRadius = 8f
                tv.background = arkaplan
            }
        }
    }
    private fun kelimeListeleriniGuncelle() {
        val hedefler = yonetici.kalanTohumKelimeler
        val yeniSayi = hedefler.size
        if (mevcutHedefSayisi != -1 && yeniSayi < mevcutHedefSayisi) {
            tvHedefSayaci.text = "🎯 Kalan Hedef: $yeniSayi"
            tvHedefSayaci.setTextColor(Color.parseColor("#2ECC71"))
            tvHedefSayaci.animate().scaleX(1.4f).scaleY(1.4f).setDuration(250).withEndAction {
                tvHedefSayaci.animate().scaleX(1f).scaleY(1f).setDuration(250).setInterpolator(BounceInterpolator()).withEndAction {
                    tvHedefSayaci.setTextColor(Color.parseColor("#F1C40F"))
                }.start()
            }.start()
        } else {
            tvHedefSayaci.text = "🎯 Kalan Hedef: $yeniSayi"
        }
        mevcutHedefSayisi = yeniSayi
        
        // Mevcut ipucu kelimesi tahtada hala var mı kontrol et
        mevcutIpucuKelimesi?.let { kelime ->
            if (!yonetici.tahtadaKelimeVarMi(kelime)) {
                tvIpucu.visibility = android.view.View.GONE
                mevcutIpucuKelimesi = null
            }
        }
        
        for ((index, kelime) in hedefler.withIndex()) {
            Log.d("WordCrushTest", "${index + 1}. $kelime")
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun suruklemeMekanizmasiniKur() {
        gridLayout.setOnTouchListener { _, event ->
            if (animasyonOynuyor || kalanHamle <= 0) return@setOnTouchListener true
            val x = event.x
            val y = event.y
            val dokunulanHucre = hucreyiBul(x, y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    secimiTemizle()
                    if (dokunulanHucre != null) {
                        if (aktifJokerTipi.isNotEmpty()) {
                            hedefliJokerUygula(dokunulanHucre)
                        } else {
                            hucreyiSec(dokunulanHucre)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (dokunulanHucre != null) hucreyiSec(dokunulanHucre)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (secilenGorseller.size >= 3) {
                        kelimeyiKontrolEt()
                    } else {
                        secimiTemizle()
                    }
                    true
                }
                else -> false
            }
        }
    }
    private fun hucreyiBul(x: Float, y: Float): TextView? {
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i) as TextView
            if (x >= child.left && x <= child.right && y >= child.top && y <= child.bottom) {
                return child
            }
        }
        return null
    }
    private fun hucreyiSec(textView: TextView) {
        if (secilenGorseller.size > 1) {
            val sondanBirOnceki = secilenGorseller[secilenGorseller.size - 2]
            if (textView == sondanBirOnceki) {
                val sonGorsel = secilenGorseller.removeAt(secilenGorseller.size - 1)
                sonGorsel.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                
                val oncekiKoorList = sonGorsel.tag.toString().split(",")
                val or = oncekiKoorList[0].toInt()
                val oc = oncekiKoorList[1].toInt()
                val oPuan = yonetici.grid[or][oc].puan
                
                val htmlMetin = "<b><font color='#2C3E50'>${yonetici.grid[or][oc].harf}</font></b><font color='#E67E22'>${yonetici.grid[or][oc].gucSimgesi}</font>"
                sonGorsel.text = android.text.Html.fromHtml(htmlMetin, android.text.Html.FROM_HTML_MODE_COMPACT)
                
                val normalArkaplan = (ContextCompat.getDrawable(this, R.drawable.bg_hucre_modern)!!.mutate()) as GradientDrawable
                normalArkaplan.setColor(Color.parseColor(getHucreRengi(oPuan)))
                normalArkaplan.setStroke(2, Color.parseColor("#BDC3C7"))
                normalArkaplan.cornerRadius = 8f
                sonGorsel.background = normalArkaplan
                
                secilenKelime.setLength(secilenKelime.length - 1)
                val oncekiKoor = sondanBirOnceki.tag.toString().split(",")
                sonSatir = oncekiKoor[0].toInt()
                sonSutun = oncekiKoor[1].toInt()
                tvSecilenKelime.text = secilenKelime.toString()
                return
            }
        }
        if (secilenGorseller.contains(textView)) return
        val koordinat = textView.tag.toString().split(",")
        val r = koordinat[0].toInt()
        val c = koordinat[1].toInt()
        if (secilenGorseller.isNotEmpty() && (abs(r - sonSatir) > 1 || abs(c - sonSutun) > 1)) {
            return
        }
        
        val gercekHarf = yonetici.grid[r][c].harf
        val puan = yonetici.grid[r][c].puan
        val baseColor = getHucreRengi(puan)
        
        val seciliArkaplan = (ContextCompat.getDrawable(this, R.drawable.bg_hucre_modern)!!.mutate()) as GradientDrawable
        // Ana rengi koru ama seçildiğini belli etmek için parlak bir sarı kenarlık ekle
        seciliArkaplan.setColor(Color.parseColor(baseColor))
        
        // Çok belirgin, parlak bir sarı/altın kenarlık (Seçim belli olsun diye)
        seciliArkaplan.setStroke(8, Color.parseColor("#F1C40F")) 
        seciliArkaplan.cornerRadius = 8f
        
        textView.background = seciliArkaplan
        // Biraz daha büyük bir büyüme efekti
        textView.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).start()
        
        // Yazı rengini KOYU tutalım (Beyaz kutularda görünmesi için)
        val htmlMetin = "<b><font color='#2C3E50'>$gercekHarf</font></b>"
        textView.text = android.text.Html.fromHtml(htmlMetin, android.text.Html.FROM_HTML_MODE_COMPACT)

        secilenGorseller.add(textView)
        secilenKelime.append(gercekHarf)
        sonSatir = r
        sonSutun = c
        tvSecilenKelime.text = secilenKelime.toString()
    }
    private fun kelimeyiKontrolEt() {
        val kelime = secilenKelime.toString()
        if (yonetici.dahaOnceBulunduMu(kelime)) {
            Toast.makeText(this, "'$kelime' zaten bulundu!", Toast.LENGTH_SHORT).show()
            secimiTemizle()
            return
        }
        if (sozluk.kelimeMi(kelime)) {
            kalanHamle--
            tvHamle.text = kalanHamle.toString()
            animasyonOynuyor = true
            yonetici.bulunanKelimeyiKaydet(kelime)
            val comboSonuc = yonetici.comboAnaliziYap(kelime)
            val comboPuan = comboSonuc["puan"] as Int
            val comboSayi = comboSonuc["sayi"] as Int
            val anaKelimeTohumMu = comboSonuc["anaKelimeTohumMu"] as Boolean
            @Suppress("UNCHECKED_CAST")
            val comboDetaylari = (comboSonuc["detaylar"] as Map<String, Int>).toMutableMap()
            val koordinatlar = mutableListOf<IntArray>()
            val kelimeKoorSet = mutableSetOf<String>()
            var kelimeHamPuan = 0
            for (tv in secilenGorseller) {
                val k = tv.tag.toString().split(",")
                val r = k[0].toInt()
                val c = k[1].toInt()
                koordinatlar.add(intArrayOf(r, c))
                kelimeKoorSet.add("$r,$c")
                kelimeHamPuan += yonetici.getHarfPuani(yonetici.grid[r][c].harf)
            }
            val kelimeNihaiPuan = if (anaKelimeTohumMu) kelimeHamPuan * 2 else kelimeHamPuan
            val tumPatlayacaklar = yonetici.patlayacakAlanlariHesapla(koordinatlar)
            var gucPatlamaPuani = 0
            for (p in tumPatlayacaklar) {
                if (!kelimeKoorSet.contains("${p[0]},${p[1]}")) {
                    gucPatlamaPuani += yonetici.getHarfPuani(yonetici.grid[p[0]][p[1]].harf)
                }
            }
            val turPuani = kelimeNihaiPuan + gucPatlamaPuani + comboPuan
            toplamSkor += turPuani
            tvSkor.text = toplamSkor.toString()
            tvIpucu.visibility = android.view.View.GONE
            mevcutIpucuKelimesi = null

            // KOMBO KELİMELERİNİ EKRANDA GÖSTERELİM
            val tumKelimeler = mutableListOf<Pair<String, Int>>()
            tumKelimeler.add(Pair(kelime, kelimeNihaiPuan)) // Ana kelime ve puanı
            comboDetaylari.forEach { (k, p) -> tumKelimeler.add(Pair(k, p)) } // Kombo ile gelenler ve puanları

            var bulunanKelimeSayisiTur = 0
            
            tumKelimeler.forEachIndexed { index, pair ->
                bulunanKelimeSayisiTur++
                val k = pair.first
                val p = pair.second
                Handler(Looper.getMainLooper()).postDelayed({
                    val isTarget = index == 0 && anaKelimeTohumMu
                    val baslik = if (isTarget) "🎯 HEDEF (x2 BONUS!)" 
                                else if (index == 0) "✨ KELİME!" 
                                else "🔥 ${index+1}x KOMBO!"
                    
                    baslangicHedefAnimasyonu("$baslik\n$k (+$p Puan)", showCount = false)
                }, index * 1800L)
            }

            var detayMetni = if (anaKelimeTohumMu) "🎯 Hedef Kelime (x2): +$kelimeNihaiPuan Puan" else "✨ Ekstra Kelime: +$kelimeNihaiPuan Puan"
            if (gucPatlamaPuani > 0) detayMetni += "\n💥 Güç Patlaması: +$gucPatlamaPuani Puan"
            if (comboSayi > 1 && comboPuan > 0) {
                val comboKelimelerStr = comboDetaylari.entries.joinToString(", ") { "${it.key} (+${it.value})" }
                detayMetni += "\n🔥 Combo (+$comboPuan Puan): $comboKelimelerStr"
            }
            Toast.makeText(this, "${comboSayi}x Combo!\n$kelime (+$turPuani Puan)\n$detayMetni", Toast.LENGTH_LONG).show()
            oyuncuKelimeleri.add(kelime)
            bulunanKelimeSayisi += bulunanKelimeSayisiTur
            if (kelime.length > enUzunBulunanKelime.length) {
                enUzunBulunanKelime = kelime
            }
            val sonHarfKoor = koordinatlar.last()
            patlamaAnimasyonu(tumPatlayacaklar, sonHarfKoor, kelime.length, kelime.last(), bulunanKelimeSayisiTur)
        } else {
            animasyonOynuyor = true
            kalanHamle--
            tvHamle.text = kalanHamle.toString()
            Toast.makeText(this, "Sözlükte yok: $kelime", Toast.LENGTH_SHORT).show()
            hataliSecimAnimasyonu()
        }
    }
    private fun patlamaAnimasyonu(tumPatlayacaklar: Set<IntArray>, sonHarfKoor: IntArray, uzunluk: Int, sonHarf: Char, comboSayisi: Int = 1) {
        animasyonOynuyor = true
        val handler = Handler(Looper.getMainLooper())
        val renkGecikmesi = 80L
        val patlamaGecikmesi = 60L
        for ((index, tv) in secilenGorseller.withIndex()) {
            handler.postDelayed({
                val colorAnimation = android.animation.ValueAnimator.ofObject(
                    android.animation.ArgbEvaluator(),
                    Color.parseColor("#FFD54F"),
                    Color.parseColor("#1E8449")
                )
                colorAnimation.duration = 150
                colorAnimation.addUpdateListener { animator ->
                    val arkaplan = tv.background as GradientDrawable
                    arkaplan.setColor(animator.animatedValue as Int)
                }
                colorAnimation.start()
            }, index * renkGecikmesi)
        }
        val renkDalgasiBitisSuresi = (secilenGorseller.size * renkGecikmesi) + 200
        handler.postDelayed({
            val kelimeKoorSet = secilenGorseller.map { it.tag.toString() }.toSet()
            for ((index, tv) in secilenGorseller.withIndex()) {
                val koor = tv.tag.toString().split(",")
                val r = koor[0].toInt()
                val c = koor[1].toInt()
                handler.postDelayed({
                    tv.animate().cancel()
                    if (uzunluk >= 4 && r == sonHarfKoor[0] && c == sonHarfKoor[1]) {
                        tv.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).withEndAction {
                            tv.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                        }.start()
                    } else {
                        tv.animate()
                            .scaleX(0f)
                            .scaleY(0f)
                            .rotation(180f)
                            .setDuration(200)
                            .start()
                    }
                }, index * patlamaGecikmesi)
            }
            for (p in tumPatlayacaklar) {
                val key = "${p[0]},${p[1]}"
                if (!kelimeKoorSet.contains(key)) {
                    val tv = hucreGorselleri[p[0]][p[1]]!!
                    tv.animate().cancel()
                    tv.animate().scaleX(0f).scaleY(0f).rotation(180f).setDuration(250).start()
                }
            }
            val patlamaBitisSuresi = (secilenGorseller.size * patlamaGecikmesi) + 250
            handler.postDelayed({
                performPatlamaVeDusme(tumPatlayacaklar, sonHarfKoor, uzunluk, sonHarf, comboSayisi)
            }, patlamaBitisSuresi)
        }, renkDalgasiBitisSuresi)
    }
    private fun performPatlamaVeDusme(tumPatlayacaklar: Set<IntArray>, sonHarfKoor: IntArray, uzunluk: Int, sonHarf: Char, comboSayisi: Int = 1) {
        val kaymalar = yonetici.yercekimiUygula(tumPatlayacaklar, sonHarfKoor, uzunluk, sonHarf)
        Handler(Looper.getMainLooper()).postDelayed({
            secimiTemizle()
            gridiGuncelle()
            val cellFullHeight = hucreGorselleri[0][0]!!.height + (hucreGorselleri[0][0]!!.layoutParams as GridLayout.LayoutParams).topMargin + (hucreGorselleri[0][0]!!.layoutParams as GridLayout.LayoutParams).bottomMargin
            for (i in 0 until boyut) {
                for (j in 0 until boyut) {
                    val tv = hucreGorselleri[i][j]!!
                    val kayma = kaymalar[i][j]
                    if (kayma == 0) continue
                    if (kayma < 50) {
                        tv.translationY = -(kayma * cellFullHeight).toFloat()
                        tv.alpha = 1f
                        tv.scaleX = 1f
                        tv.scaleY = 1f
                        tv.rotation = 0f
                    } else {
                        tv.translationY = -1000f
                        tv.alpha = 0f
                        tv.scaleX = 0.5f
                        tv.scaleY = 0.5f
                        tv.rotation = (Math.random() * 10 - 5).toFloat()
                    }
                    val gecikme = ((boyut - i) * 30 + j * 20).toLong()
                    tv.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotation(0f)
                        .setInterpolator(OvershootInterpolator(1.2f))
                        .setDuration(550)
                        .setStartDelay(gecikme)
                        .start()
                }
            }
            kelimeListeleriniGuncelle()
            
            // Combo animasyonlarının bitmesini bekleyelim
            // Her animasyon ~1800ms sürüyor + son animasyonun bitişi için ek süre
            val comboBeklemeSuresi = (comboSayisi * 1800L) + 500L
            
            Handler(Looper.getMainLooper()).postDelayed({
                animasyonOynuyor = false
                oyunDurumunuKontrolEt()
            }, maxOf(650L, comboBeklemeSuresi))
        }, 50)
    }
    private fun hataliSecimAnimasyonu() {
        for (tv in secilenGorseller) {
            val hataArkaplan = GradientDrawable()
            hataArkaplan.setColor(Color.parseColor("#FF9696"))
            hataArkaplan.setStroke(4, Color.RED)
            hataArkaplan.cornerRadius = 8f
            tv.background = hataArkaplan
            tv.animate()
                .translationX(15f)
                .setDuration(50)
                .withEndAction {
                    tv.animate().translationX(-15f).setDuration(50).withEndAction {
                        tv.animate().translationX(0f).setDuration(50).start()
                    }.start()
                }.start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            secimiTemizle()
            animasyonOynuyor = false
            oyunDurumunuKontrolEt()
        }, 400)
    }
    private fun secimiTemizle() {
        for (tv in secilenGorseller) {
            val koor = tv.tag.toString().split(",")
            val r = koor[0].toInt()
            val c = koor[1].toInt()
            val puan = yonetici.grid[r][c].puan
            
            val htmlMetin = "<b><font color='#2C3E50'>${yonetici.grid[r][c].harf}</font></b><font color='#E67E22'>${yonetici.grid[r][c].gucSimgesi}</font>"
            tv.text = android.text.Html.fromHtml(htmlMetin, android.text.Html.FROM_HTML_MODE_COMPACT)
            
            val normalArkaplan = (ContextCompat.getDrawable(this, R.drawable.bg_hucre_modern)!!.mutate()) as GradientDrawable
            normalArkaplan.setColor(Color.parseColor(getHucreRengi(puan)))
            normalArkaplan.setStroke(2, Color.parseColor("#BDC3C7"))
            normalArkaplan.cornerRadius = 8f
            tv.background = normalArkaplan
            tv.animate().cancel()
            tv.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(150)
                .start()
        }
        secilenGorseller.clear()
        secilenKelime.setLength(0)
        sonSatir = -1
        sonSutun = -1
        tvSecilenKelime.text = "Kelime seçin..."
    }
    private fun oyunDurumunuKontrolEt() {
        if (kalanHamle <= 0) {
            Toast.makeText(this, "Oyun Bitti! Toplam Skorunuz: $toplamSkor", Toast.LENGTH_LONG).show()
            skoruKaydetVeCik()
        } else if (yonetici.tohumKelimelerTukendiMi()) {
            Toast.makeText(this, "Harika! Tahtadaki tüm hedef kelimeleri buldun.", Toast.LENGTH_LONG).show()
            yonetici.gridiYenile()
            gridiGuncelle()
            kelimeListeleriniGuncelle()
            baslangicHedefAnimasyonu("YENİ HEDEF\nBELİRLENDİ!")
        } else if (!yonetici.hamleVarMi()) {
            Toast.makeText(this, "Hamle Kalmadı! Harfler karıştırılıyor...", Toast.LENGTH_SHORT).show()
            shuffleVeAnimasyonOynat()
        }
    }
    private fun skoruKaydetVeCik() {
        sureyiDurdur()
        val ayarlar = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val aktifKullanici = ayarlar.getString("KULLANICI_ADI", "Oyuncu") ?: "Oyuncu"
        val skorManager = tr.edu.kocaeli.wordcrush.util.SkorManager(this)
        val oyunNo = skorManager.gecmisOyunlariGetir().size + 1
        val gecenSureMs = System.currentTimeMillis() - oyunBaslangicZamani
        val toplamSaniye = (gecenSureMs / 1000).toInt()
        val kayit = tr.edu.kocaeli.wordcrush.util.OyunGecmisi(
            oyunNumarasi = oyunNo,
            kullaniciAdi = aktifKullanici,
            tarih = skorManager.bugununTarihi(),
            gridBoyutu = "${boyut}x${boyut}",
            puan = toplamSkor,
            bulunanKelimeler = if (oyuncuKelimeleri.isEmpty()) "-" else oyuncuKelimeleri.joinToString(", "),
            enUzunKelime = if (enUzunBulunanKelime.isEmpty()) "-" else enUzunBulunanKelime,
            sureSaniye = toplamSaniye
        )
        skorManager.oyunKaydet(kayit)
        finish()
    }
    private fun jokerleriYukleVeBagla() {
        btnOyunBalik = findViewById(R.id.btnOyunBalik)
        btnOyunIsinKilici = findViewById(R.id.btnOyunIsinKilici)
        btnOyunKeskinNisanci = findViewById(R.id.btnOyunKeskinNisanci)
        btnOyunDegistirme = findViewById(R.id.btnOyunDegistirme)
        btnOyunKaristirma = findViewById(R.id.btnOyunKaristirma)
        btnOyunParti = findViewById(R.id.btnOyunParti)
        btnOyunIpucu = findViewById(R.id.btnOyunIpucu)
        jokerSayilariniGuncelle()
        btnOyunBalik.setOnClickListener { jokerKullan("Joker_Balik") }
        btnOyunIsinKilici.setOnClickListener { jokerKullan("Joker_IsinKilici") }
        btnOyunKeskinNisanci.setOnClickListener { jokerKullan("Joker_KeskinNisanci") }
        btnOyunDegistirme.setOnClickListener { jokerKullan("Joker_Degistirme") }
        btnOyunKaristirma.setOnClickListener { jokerKullan("Joker_Karistirma") }
        btnOyunParti.setOnClickListener { jokerKullan("Joker_Parti") }
        btnOyunIpucu.setOnClickListener { jokerKullan("Joker_Ipucu") }
    }
    private fun jokerSayilariniGuncelle() {
        val ayarlar = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val aktifKullanici = ayarlar.getString("KULLANICI_ADI", "Oyuncu")
        val sharedPref = getSharedPreferences("WordCrushMarket_$aktifKullanici", Context.MODE_PRIVATE)
        val mevcutAltin = sharedPref.getInt("KULLANICI_ALTIN", 10000)
        if (::tvAltin.isInitialized) {
            tvAltin.text = "$mevcutAltin 🪙"
        }
        btnOyunBalik.text = "🐟 x${sharedPref.getInt("Joker_Balik", 0)}"
        btnOyunIsinKilici.text = "⚔️ x${sharedPref.getInt("Joker_IsinKilici", 0)}"
        btnOyunKeskinNisanci.text = "🎯 x${sharedPref.getInt("Joker_KeskinNisanci", 0)}"
        btnOyunDegistirme.text = "🔄 x${sharedPref.getInt("Joker_Degistirme", 0)}"
        btnOyunKaristirma.text = "🔀 x${sharedPref.getInt("Joker_Karistirma", 0)}"
        btnOyunParti.text = "🎉 x${sharedPref.getInt("Joker_Parti", 0)}"
        btnOyunIpucu.text = "💡 x${sharedPref.getInt("Joker_Ipucu", 0)}"
        jokerGorselleriniGuncelle()
    }
    private fun jokerGorselleriniGuncelle() {
        val jokerButonlari = mapOf(
            "Joker_Balik" to btnOyunBalik,
            "Joker_IsinKilici" to btnOyunIsinKilici,
            "Joker_KeskinNisanci" to btnOyunKeskinNisanci,
            "Joker_Degistirme" to btnOyunDegistirme,
            "Joker_Karistirma" to btnOyunKaristirma,
            "Joker_Parti" to btnOyunParti
        )
        jokerButonlari.forEach { (key, btn) ->
            if (aktifJokerTipi == key) {
                val selectedBg = when(key) {
                    "Joker_Balik", "Joker_Degistirme" -> R.drawable.btn_yesil_selected
                    "Joker_IsinKilici", "Joker_Karistirma" -> R.drawable.btn_mor_selected
                    else -> R.drawable.btn_turuncu_selected
                }
                btn.setBackgroundResource(selectedBg)
                btn.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(250)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            } else {
                val defaultBg = when(key) {
                    "Joker_Balik", "Joker_Degistirme" -> R.drawable.btn_modern_yesil
                    "Joker_IsinKilici", "Joker_Karistirma" -> R.drawable.btn_modern_mor
                    else -> R.drawable.btn_modern_turuncu
                }
                btn.setBackgroundResource(defaultBg)
                btn.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
        }
    }
    private fun jokerKullan(jokerKey: String) {
        if (animasyonOynuyor) return
        val ayarlar = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val aktifKullanici = ayarlar.getString("KULLANICI_ADI", "Oyuncu")
        val sharedPref = getSharedPreferences("WordCrushMarket_$aktifKullanici", Context.MODE_PRIVATE)
        val sayi = sharedPref.getInt(jokerKey, 0)
        if (sayi > 0) {
            if (jokerKey == "Joker_Balik" || jokerKey == "Joker_Karistirma" || jokerKey == "Joker_Parti" || jokerKey == "Joker_Ipucu") {
                jokerSayisiniAzalt(jokerKey)
                anindaJokerCalistir(jokerKey)
            } else {
                if (aktifJokerTipi == jokerKey) {
                    aktifJokerTipi = ""
                    ilkDegistirmeHucresi = null
                    gridiGuncelle()
                } else {
                    aktifJokerTipi = jokerKey
                    ilkDegistirmeHucresi = null
                }
                jokerGorselleriniGuncelle()
            }
        } else {
            Toast.makeText(this, "Bu jokerden kalmadı! Marketten satın alabilirsiniz.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun jokerSayisiniAzalt(jokerKey: String) {
        val ayarlar = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val aktifKullanici = ayarlar.getString("KULLANICI_ADI", "Oyuncu")
        val sharedPref = getSharedPreferences("WordCrushMarket_$aktifKullanici", Context.MODE_PRIVATE)
        val sayi = sharedPref.getInt(jokerKey, 0)
        if (sayi > 0) {
            sharedPref.edit().putInt(jokerKey, sayi - 1).apply()
            jokerSayilariniGuncelle()
        }
    }
    private fun anindaJokerCalistir(jokerKey: String) {
        if (animasyonOynuyor) return
        animasyonOynuyor = true
        when (jokerKey) {
            "Joker_Balik" -> {
                val patlayanlar = yonetici.jokerBalikUygula()
                patlamaAnimasyonu(patlayanlar, intArrayOf(-1, -1), 0, ' ')
                Handler(Looper.getMainLooper()).postDelayed({
                    baslangicHedefAnimasyonu("KALAN\nHEDEFLER!")
                }, 800)
            }
            "Joker_Karistirma" -> {
                shuffleVeAnimasyonOynat()
            }
            "Joker_Parti" -> {
                val patlayanlar = yonetici.jokerPartiUygula()
                partiJokeriAnimasyonu(patlayanlar)
            }
            "Joker_Ipucu" -> {
                val kalanHedefler = yonetici.kalanTohumKelimeler
                if (kalanHedefler.isNotEmpty()) {
                    val hedefKelime = kalanHedefler.random()
                    mevcutIpucuKelimesi = hedefKelime
                    val karisikKelime = hedefKelime.toList().shuffled().joinToString("")
                    baslangicHedefAnimasyonu("İPUCU\n$karisikKelime", showCount = false)
                    tvIpucu.text = "💡 İpucu: $karisikKelime"
                    tvIpucu.visibility = android.view.View.VISIBLE
                    tvIpucu.alpha = 0f
                    tvIpucu.animate().alpha(1f).setDuration(500).setStartDelay(1500).start()
                } else {
                    Toast.makeText(this, "Kalan hedef yok!", Toast.LENGTH_SHORT).show()
                    animasyonOynuyor = false
                }
            }
        }
    }
    private fun hedefliJokerUygula(tv: TextView) {
        if (animasyonOynuyor) return
        val koor = tv.tag.toString().split(",")
        val r = koor[0].toInt()
        val c = koor[1].toInt()
        when (aktifJokerTipi) {
            "Joker_IsinKilici" -> {
                animasyonOynuyor = true
                jokerSayisiniAzalt(aktifJokerTipi)
                aktifJokerTipi = ""
                jokerGorselleriniGuncelle()
                isinKiliciAnimasyonu(r, c)
            }
            "Joker_KeskinNisanci" -> {
                animasyonOynuyor = true
                jokerSayisiniAzalt(aktifJokerTipi)
                aktifJokerTipi = ""
                jokerGorselleriniGuncelle()
                keskinNisanciAnimasyonu(r, c)
            }
            "Joker_Degistirme" -> {
                if (ilkDegistirmeHucresi == null) {
                    ilkDegistirmeHucresi = tv
                    val arkaplan = tv.background as GradientDrawable
                    arkaplan.setStroke(6, Color.parseColor("#8E44AD"))
                    val ilkKoor = ilkDegistirmeHucresi!!.tag.toString().split(",")
                    val r1 = ilkKoor[0].toInt()
                    val c1 = ilkKoor[1].toInt()
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            if (dr == 0 && dc == 0) continue
                            val nr = r1 + dr
                            val nc = c1 + dc
                            if (nr in 0 until boyut && nc in 0 until boyut) {
                                val komsuTv = hucreGorselleri[nr][nc]!!
                                val kPuan = yonetici.grid[nr][nc].puan
                                
                                val komsuArkaplan = GradientDrawable()
                                komsuArkaplan.setColor(Color.parseColor(getHucreRengi(kPuan)))
                                // Seçilebilirlik vurgusu için sadece kenarlığı değiştirelim veya hafif bir overlay ekleyelim
                                // Burada kullanıcı deneyimi için hafifçe rengi açabiliriz veya sadece kalın kenarlık yapabiliriz
                                komsuArkaplan.setStroke(4, Color.parseColor("#D2B4DE"))
                                komsuArkaplan.cornerRadius = 8f
                                komsuTv.background = komsuArkaplan
                                komsuTv.animate()
                                    .scaleX(1.1f)
                                    .scaleY(1.1f)
                                    .setDuration(200)
                                    .setInterpolator(OvershootInterpolator())
                                    .start()
                            }
                        }
                    }
                } else {
                    val ilkKoor = ilkDegistirmeHucresi!!.tag.toString().split(",")
                    val r1 = ilkKoor[0].toInt()
                    val c1 = ilkKoor[1].toInt()
                    if (tv != ilkDegistirmeHucresi && abs(r - r1) <= 1 && abs(c - c1) <= 1) {
                        animasyonOynuyor = true
                        jokerSayisiniAzalt(aktifJokerTipi)
                        aktifJokerTipi = ""
                        jokerGorselleriniGuncelle()
                        val tv1 = hucreGorselleri[r1][c1]!!
                        val tv2 = hucreGorselleri[r][c]!!
                        gridiGuncelle()
                        val root = findViewById<ViewGroup>(android.R.id.content)
                        val rootKoor = IntArray(2)
                        root.getLocationOnScreen(rootKoor)
                        val koor1 = IntArray(2)
                        tv1.getLocationOnScreen(koor1)
                        val startX1 = (koor1[0] - rootKoor[0]).toFloat()
                        val startY1 = (koor1[1] - rootKoor[1]).toFloat()
                        val koor2 = IntArray(2)
                        tv2.getLocationOnScreen(koor2)
                        val startX2 = (koor2[0] - rootKoor[0]).toFloat()
                        val startY2 = (koor2[1] - rootKoor[1]).toFloat()
                        tv1.alpha = 0f
                        tv2.alpha = 0f
                        val kopya1 = TextView(this)
                        kopya1.text = tv1.text
                        kopya1.background = tv1.background?.constantState?.newDrawable()
                        kopya1.layoutParams = FrameLayout.LayoutParams(tv1.width, tv1.height)
                        kopya1.gravity = Gravity.CENTER
                        kopya1.elevation = 100f
                        root.addView(kopya1)
                        val kopya2 = TextView(this)
                        kopya2.text = tv2.text
                        kopya2.background = tv2.background?.constantState?.newDrawable()
                        kopya2.layoutParams = FrameLayout.LayoutParams(tv2.width, tv2.height)
                        kopya2.gravity = Gravity.CENTER
                        kopya2.elevation = 100f
                        root.addView(kopya2)
                        val midX = (startX1 + startX2) / 2f
                        val midY = (startY1 + startY2) / 2f
                        val dx = startX2 - startX1
                        val dy = startY2 - startY1
                        val ctrlX1 = midX - dy * 0.5f
                        val ctrlY1 = midY + dx * 0.5f
                        val ctrlX2 = midX + dy * 0.5f
                        val ctrlY2 = midY - dx * 0.5f
                        val path1 = android.graphics.Path()
                        path1.moveTo(startX1, startY1)
                        path1.quadTo(ctrlX1, ctrlY1, startX2, startY2)
                        val path2 = android.graphics.Path()
                        path2.moveTo(startX2, startY2)
                        path2.quadTo(ctrlX2, ctrlY2, startX1, startY1)
                        val anim1 = android.animation.ObjectAnimator.ofFloat(kopya1, android.view.View.X, android.view.View.Y, path1)
                        val anim2 = android.animation.ObjectAnimator.ofFloat(kopya2, android.view.View.X, android.view.View.Y, path2)
                        val rotate1 = android.animation.ObjectAnimator.ofFloat(kopya1, "rotation", 0f, 360f)
                        val rotate2 = android.animation.ObjectAnimator.ofFloat(kopya2, "rotation", 0f, 360f)
                        val animatorSet = android.animation.AnimatorSet()
                        animatorSet.playTogether(anim1, anim2, rotate1, rotate2)
                        animatorSet.duration = 450
                        animatorSet.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                        animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                root.removeView(kopya1)
                                root.removeView(kopya2)
                                yonetici.jokerDegistirmeUygula(r1, c1, r, c)
                                gridiGuncelle()
                                kelimeListeleriniGuncelle()
                                ilkDegistirmeHucresi = null
                                Handler(Looper.getMainLooper()).postDelayed({
                                    animasyonOynuyor = false
                                    oyunDurumunuKontrolEt()
                                    baslangicHedefAnimasyonu("KALAN\nHEDEFLER!")
                                }, 150)
                            }
                        })
                        animatorSet.start()
                    } else {
                        gridiGuncelle()
                        ilkDegistirmeHucresi = null
                        aktifJokerTipi = ""
                        jokerGorselleriniGuncelle()
                    }
                }
            }
        }
    }
    override fun onPause() {
        super.onPause()
        sureyiDurdur()
    }
    override fun onResume() {
        super.onResume()
        sureyiBaslat()
        jokerSayilariniGuncelle()
    }
    private fun shuffleVeAnimasyonOynat() {
        animasyonOynuyor = true
        val gridCenterX = gridLayout.width / 2f
        val gridCenterY = gridLayout.height / 2f
        for (i in 0 until boyut) {
            for (j in 0 until boyut) {
                val tv = hucreGorselleri[i][j]!!
                tv.animate().cancel()
                val tvCenterX = tv.x + tv.width / 2f
                val tvCenterY = tv.y + tv.height / 2f
                val moveX = gridCenterX - tvCenterX
                val moveY = gridCenterY - tvCenterY
                tv.animate()
                    .translationX(moveX)
                    .translationY(moveY)
                    .scaleX(0f)
                    .scaleY(0f)
                    .rotation(720f)
                    .setDuration(450)
                    .setInterpolator(android.view.animation.AccelerateInterpolator())
                    .start()
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            yonetici.jokerKaristirmaUygula()
            gridiGuncelle()
            kelimeListeleriniGuncelle()
            for (i in 0 until boyut) {
                for (j in 0 until boyut) {
                    val tv = hucreGorselleri[i][j]!!
                    val tvCenterX = tv.x + tv.width / 2f
                    val tvCenterY = tv.y + tv.height / 2f
                    val moveX = gridCenterX - tvCenterX
                    val moveY = gridCenterY - tvCenterY
                    tv.translationX = moveX
                    tv.translationY = moveY
                    tv.scaleX = 0f
                    tv.scaleY = 0f
                    tv.rotation = -720f
                    val gecikme = (Math.random() * 200).toLong()
                    tv.animate()
                        .translationX(0f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotation(0f)
                        .setDuration(600)
                        .setStartDelay(gecikme)
                        .setInterpolator(OvershootInterpolator(1.3f))
                        .start()
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                baslangicHedefAnimasyonu("YENİ HEDEF\nBELİRLENDİ!")
            }, 750)
        }, 500)
    }
    private fun partiJokeriAnimasyonu(tumPatlayacaklar: Set<IntArray>) {
        animasyonOynuyor = true
        val root = findViewById<ViewGroup>(android.R.id.content)
        val density = resources.displayMetrics.density
        fun dpToPx(dp: Int): Int = (dp * density).toInt()
        val shredder = FrameLayout(this)
        val sGenislik = dpToPx(200)
        val sYukseklik = dpToPx(90)
        val shredderParams = FrameLayout.LayoutParams(sGenislik, sYukseklik)
        shredderParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        shredderParams.bottomMargin = dpToPx(110)
        val bg = GradientDrawable()
        bg.setColor(Color.parseColor("#95A5A6"))
        bg.cornerRadius = dpToPx(12).toFloat()
        bg.setStroke(dpToPx(4), Color.parseColor("#7F8C8D"))
        shredder.background = bg
        val slotGenislik = dpToPx(160)
        val slotYukseklik = dpToPx(10)
        val slotParams = FrameLayout.LayoutParams(slotGenislik, slotYukseklik)
        slotParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        slotParams.topMargin = dpToPx(20)
        val slot = android.view.View(this)
        slot.setBackgroundColor(Color.parseColor("#111111"))
        slot.layoutParams = slotParams
        shredder.addView(slot)
        root.addView(shredder, shredderParams)
        shredder.translationY = dpToPx(300).toFloat()
        shredder.elevation = 50f
        shredder.animate()
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()
        root.post {
            val rootKoor = IntArray(2)
            root.getLocationOnScreen(rootKoor)
            val hedefX = root.width / 2f
            val hedefY = root.height - dpToPx(110) - sYukseklik + dpToPx(20).toFloat()
            var harfGecikmesi = 400L
            for (p in tumPatlayacaklar) {
                val gercekTv = hucreGorselleri[p[0]][p[1]]!!
                gercekTv.animate().cancel()
                val koor = IntArray(2)
                gercekTv.getLocationOnScreen(koor)
                val kopyaTv = TextView(this)
                kopyaTv.text = gercekTv.text
                kopyaTv.textSize = gercekTv.textSize / resources.displayMetrics.scaledDensity
                kopyaTv.setTypeface(null, android.graphics.Typeface.BOLD)
                kopyaTv.background = gercekTv.background?.constantState?.newDrawable()
                kopyaTv.gravity = Gravity.CENTER
                val kopyaParams = FrameLayout.LayoutParams(gercekTv.width, gercekTv.height)
                kopyaTv.layoutParams = kopyaParams
                kopyaTv.x = (koor[0] - rootKoor[0]).toFloat()
                kopyaTv.y = (koor[1] - rootKoor[1]).toFloat()
                kopyaTv.elevation = 60f
                root.addView(kopyaTv)
                gercekTv.alpha = 0f
                val gidecegiX = hedefX - (gercekTv.width / 2f)
                val gidecegiY = hedefY - (gercekTv.height / 2f)
                kopyaTv.animate()
                    .x(gidecegiX)
                    .y(gidecegiY)
                    .rotation((Math.random() * 720).toFloat())
                    .scaleX(0.1f)
                    .scaleY(0.1f)
                    .alpha(0f)
                    .setDuration(350)
                    .setStartDelay(harfGecikmesi)
                    .setInterpolator(android.view.animation.AccelerateInterpolator())
                    .withEndAction {
                        root.removeView(kopyaTv)
                        for (i in 0..3) {
                            val kirpinti = android.view.View(this@OyunEkraniActivity)
                            kirpinti.setBackgroundColor(Color.WHITE)
                            val kParams = FrameLayout.LayoutParams(dpToPx(4), dpToPx(16))
                            kirpinti.layoutParams = kParams
                            kirpinti.x = (root.width / 2f) - (slotGenislik / 2f) + dpToPx(10) + (Math.random() * (slotGenislik - dpToPx(20))).toFloat()
                            kirpinti.y = hedefY + dpToPx(20)
                            kirpinti.elevation = 49f
                            root.addView(kirpinti)
                            kirpinti.animate()
                                .translationYBy(dpToPx(300).toFloat())
                                .alpha(0f)
                                .setDuration(500)
                                .withEndAction { root.removeView(kirpinti) }
                                .start()
                        }
                    }
                    .start()
                harfGecikmesi += 70L
            }
            val tamBitisSuresi = harfGecikmesi + 350L
            Handler(Looper.getMainLooper()).postDelayed({
                shredder.animate()
                    .translationY(dpToPx(300).toFloat())
                    .setDuration(400)
                    .withEndAction {
                        root.removeView(shredder)
                        yonetici.gridiYenile()
                        gridiGuncelle()
                        kelimeListeleriniGuncelle()
                        for (i in 0 until boyut) {
                            for (j in 0 until boyut) {
                                val tv = hucreGorselleri[i][j]!!
                                tv.translationY = -1200f
                                tv.alpha = 0f
                                tv.scaleX = 0.5f
                                tv.scaleY = 0.5f
                                tv.rotation = (Math.random() * 15 - 7.5).toFloat()
                                val gecikme = ((boyut - i) * 35 + j * 25).toLong()
                                tv.animate()
                                    .translationY(0f)
                                    .alpha(1f)
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .rotation(0f)
                                    .setInterpolator(OvershootInterpolator(1.1f))
                                    .setDuration(600)
                                    .setStartDelay(gecikme)
                                    .start()
                            }
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            baslangicHedefAnimasyonu("YENİ HEDEFLER\nBELİRLENDİ!")
                            animasyonOynuyor = false
                        }, 900)
                    }
                    .start()
            }, tamBitisSuresi)
        }
    }
    private fun keskinNisanciAnimasyonu(r: Int, c: Int) {
        animasyonOynuyor = true
        val root = findViewById<ViewGroup>(android.R.id.content)
        val density = resources.displayMetrics.density
        fun dpToPx(dp: Int): Int = (dp * density).toInt()
        val hedefTv = hucreGorselleri[r][c]!!
        val koor = IntArray(2)
        hedefTv.getLocationOnScreen(koor)
        val crosshairSize = dpToPx(60)
        val crosshairView = FrameLayout(this)
        crosshairView.layoutParams = FrameLayout.LayoutParams(crosshairSize, crosshairSize)
        val anaRenk = Color.parseColor("#FF0033")
        val cizgiKalinligi = dpToPx(2)
        val cizgiUzunlugu = dpToPx(15)
        val circle = android.view.View(this)
        val circleBg = GradientDrawable()
        circleBg.shape = GradientDrawable.OVAL
        circleBg.setStroke(dpToPx(2), Color.parseColor("#66FF0033"))
        circleBg.setColor(Color.TRANSPARENT)
        circle.background = circleBg
        crosshairView.addView(circle, FrameLayout.LayoutParams(crosshairSize, crosshairSize))
        val dotSize = dpToPx(4)
        val dot = android.view.View(this)
        val dotBg = GradientDrawable()
        dotBg.shape = GradientDrawable.OVAL
        dotBg.setColor(anaRenk)
        dot.background = dotBg
        val dotParams = FrameLayout.LayoutParams(dotSize, dotSize)
        dotParams.gravity = Gravity.CENTER
        crosshairView.addView(dot, dotParams)
        val topView = android.view.View(this)
        topView.setBackgroundColor(anaRenk)
        val topParams = FrameLayout.LayoutParams(cizgiKalinligi, cizgiUzunlugu)
        topParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        crosshairView.addView(topView, topParams)
        val bottomView = android.view.View(this)
        bottomView.setBackgroundColor(anaRenk)
        val bottomParams = FrameLayout.LayoutParams(cizgiKalinligi, cizgiUzunlugu)
        bottomParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        crosshairView.addView(bottomView, bottomParams)
        val leftView = android.view.View(this)
        leftView.setBackgroundColor(anaRenk)
        val leftParams = FrameLayout.LayoutParams(cizgiUzunlugu, cizgiKalinligi)
        leftParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        crosshairView.addView(leftView, leftParams)
        val rightView = android.view.View(this)
        rightView.setBackgroundColor(anaRenk)
        val rightParams = FrameLayout.LayoutParams(cizgiUzunlugu, cizgiKalinligi)
        rightParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        crosshairView.addView(rightView, rightParams)
        val targetX = koor[0].toFloat() + (hedefTv.width / 2f) - (crosshairSize / 2f)
        val targetY = koor[1].toFloat() + (hedefTv.height / 2f) - (crosshairSize / 2f)
        crosshairView.x = targetX
        crosshairView.y = targetY
        crosshairView.elevation = 1000f
        crosshairView.scaleX = 3.5f
        crosshairView.scaleY = 3.5f
        crosshairView.alpha = 0f
        crosshairView.rotation = 180f
        root.addView(crosshairView)
        crosshairView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .rotation(0f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator(1.2f))
            .withEndAction {
                crosshairView.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction {
                        crosshairView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .withEndAction {
                                crosshairView.animate()
                                    .scaleX(1.5f)
                                    .scaleY(1.5f)
                                    .alpha(0f)
                                    .setDuration(150)
                                    .withEndAction { root.removeView(crosshairView) }
                                    .start()
                                hedefTv.animate().scaleX(0f).scaleY(0f).setDuration(150).start()
                                performPatlamaVeDusme(setOf(intArrayOf(r, c)), intArrayOf(-1, -1), 0, ' ')
                            }.start()
                    }.start()
            }
            .start()
    }
    private fun sureyiBaslat() {
        if (zamanIsliyor) return
        zamanIsliyor = true
        zamanlayiciHandler = Handler(Looper.getMainLooper())
        zamanlayiciRunnable = object : Runnable {
            override fun run() {
                if (zamanIsliyor) {
                    saniyeCinsindenSure++
                    sureEkraniGuncelle()
                    zamanlayiciHandler?.postDelayed(this, 1000)
                }
            }
        }
        zamanlayiciHandler?.post(zamanlayiciRunnable!!)
    }
    private fun sureEkraniGuncelle() {
        val dakika = saniyeCinsindenSure / 60
        val saniye = saniyeCinsindenSure % 60
        val formatliSure = String.format("%02d:%02d", dakika, saniye)
        tvSure.text = formatliSure
    }
    private fun sureyiDurdur() {
        zamanIsliyor = false
        zamanlayiciRunnable?.let { zamanlayiciHandler?.removeCallbacks(it) }
    }
    private fun sureyiSifirla() {
        sureyiDurdur()
        saniyeCinsindenSure = 0
        sureEkraniGuncelle()
    }
    private fun isinKiliciAnimasyonu(r: Int, c: Int) {
        animasyonOynuyor = true
        val root = findViewById<ViewGroup>(android.R.id.content)
        val density = resources.displayMetrics.density
        fun dpToPx(dp: Int): Int = (dp * density).toInt()
        val hedefTv = hucreGorselleri[r][c]!!
        val tvKoor = IntArray(2)
        hedefTv.getLocationOnScreen(tvKoor)
        val rootKoor = IntArray(2)
        root.getLocationOnScreen(rootKoor)
        val targetCenterX = (tvKoor[0] - rootKoor[0]).toFloat() + (hedefTv.width / 2f)
        val targetCenterY = (tvKoor[1] - rootKoor[1]).toFloat() + (hedefTv.height / 2f)
        val gridKoor = IntArray(2)
        gridLayout.getLocationOnScreen(gridKoor)
        val gridSol = (gridKoor[0] - rootKoor[0]).toFloat()
        val gridUst = (gridKoor[1] - rootKoor[1]).toFloat()
        val gridGenislik = gridLayout.width
        val gridYukseklik = gridLayout.height
        val hiltColor = Color.parseColor("#2C3E50")
        val neonColor = Color.parseColor("#FF0000")
        val bladeWidth = dpToPx(5)
        val hiltLength = dpToPx(20)
        val hiltWidth = dpToPx(8)
        fun createHilt(isVertical: Boolean): android.view.View {
            val v = android.view.View(this)
            v.layoutParams = FrameLayout.LayoutParams(
                if (isVertical) hiltWidth else hiltLength,
                if (isVertical) hiltLength else hiltWidth
            )
            val bg = GradientDrawable()
            bg.setColor(hiltColor)
            bg.setStroke(dpToPx(1), Color.BLACK)
            bg.cornerRadius = dpToPx(2).toFloat()
            v.background = bg
            v.elevation = 2100f
            return v
        }
        fun createLaser(isVertical: Boolean): android.view.View {
            val v = android.view.View(this)
            v.setBackgroundColor(neonColor)
            v.elevation = 2000f
            val w = if (isVertical) bladeWidth else gridGenislik
            val h = if (isVertical) gridYukseklik else bladeWidth
            v.layoutParams = FrameLayout.LayoutParams(w, h)
            return v
        }
        val hiltTop = createHilt(true)
        val hiltBottom = createHilt(true)
        val hiltLeft = createHilt(false)
        val hiltRight = createHilt(false)
        hiltTop.x = targetCenterX - (hiltWidth / 2f)
        hiltTop.y = targetCenterY - (hedefTv.height / 2f) - hiltLength
        hiltBottom.x = targetCenterX - (hiltWidth / 2f)
        hiltBottom.y = targetCenterY + (hedefTv.height / 2f)
        hiltLeft.x = targetCenterX - (hedefTv.width / 2f) - hiltLength
        hiltLeft.y = targetCenterY - (hiltWidth / 2f)
        hiltRight.x = targetCenterX + (hedefTv.width / 2f)
        hiltRight.y = targetCenterY - (hiltWidth / 2f)
        val hBlade = createLaser(false)
        val vBlade = createLaser(true)
        hBlade.x = gridSol
        hBlade.y = targetCenterY - (bladeWidth / 2f)
        hBlade.scaleX = 0f
        hBlade.pivotX = targetCenterX - gridSol
        vBlade.x = targetCenterX - (bladeWidth / 2f)
        vBlade.y = gridUst
        vBlade.scaleY = 0f
        vBlade.pivotY = targetCenterY - gridUst
        val allViews = listOf(hiltTop, hiltBottom, hiltLeft, hiltRight, hBlade, vBlade)
        allViews.forEach { root.addView(it) }
        listOf(hiltTop, hiltBottom, hiltLeft, hiltRight).forEach {
            it.alpha = 0f
            it.scaleX = 0f
            it.scaleY = 0f
            it.animate().alpha(1f).scaleX(1f).scaleY(1f).rotation(360f).setDuration(300).start()
        }
        val patlayanlarSet = mutableSetOf<String>()
        Handler(Looper.getMainLooper()).postDelayed({
            val sweepDuration = 400L
            hBlade.animate().scaleX(1f).setDuration(sweepDuration).start()
            vBlade.animate().scaleY(1f).setDuration(sweepDuration)
                .withEndAction {
                    Handler(Looper.getMainLooper()).postDelayed({
                        allViews.forEach {
                            it.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f).setDuration(150).withEndAction { root.removeView(it) }.start()
                        }
                        val patlayacaklar = mutableSetOf<IntArray>()
                        for (i in 0 until boyut) {
                            patlayacaklar.add(intArrayOf(r, i))
                            patlayacaklar.add(intArrayOf(i, c))
                        }
                        performPatlamaVeDusme(patlayacaklar, intArrayOf(-1, -1), 0, ' ')
                        Handler(Looper.getMainLooper()).postDelayed({
                            baslangicHedefAnimasyonu("KALAN\nHEDEFLER!")
                        }, 600)
                    }, 100)
                }.start()
            for (i in 0 until boyut) {
                if (i == r && !patlayanlarSet.contains("$r,$c")) {
                    hedefTv.animate().scaleX(0f).scaleY(0f).setDuration(150).start()
                    patlayanlarSet.add("$r,$c")
                }
                val mesafeR = abs(i - r)
                if (mesafeR > 0) {
                    val delay = (mesafeR * (sweepDuration / (boyut / 2)))
                    Handler(Looper.getMainLooper()).postDelayed({
                        val tv = hucreGorselleri[i][c]
                        if (tv != null && !patlayanlarSet.contains("$i,$c")) {
                            tv.animate().scaleX(0f).scaleY(0f).setDuration(150).start()
                            patlayanlarSet.add("$i,$c")
                        }
                    }, delay)
                }
                val mesafeC = abs(i - c)
                if (mesafeC > 0) {
                    val delay = (mesafeC * (sweepDuration / (boyut / 2)))
                    Handler(Looper.getMainLooper()).postDelayed({
                        val tv = hucreGorselleri[r][i]
                        if (tv != null && !patlayanlarSet.contains("$r,$i")) {
                            tv.animate().scaleX(0f).scaleY(0f).setDuration(150).start()
                            patlayanlarSet.add("$r,$i")
                        }
                    }, delay)
                }
            }
        }, 350)
    }
}
