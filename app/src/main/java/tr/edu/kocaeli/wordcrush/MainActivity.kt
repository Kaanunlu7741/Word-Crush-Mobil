package tr.edu.kocaeli.wordcrush
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("WordCrushAyarlar", Context.MODE_PRIVATE)
        val kayitliKullanici = sharedPref.getString("KULLANICI_ADI", null)

        if (kayitliKullanici != null) {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
            Toast.makeText(this, "Tekrar Hoş Geldin, $kayitliKullanici!", Toast.LENGTH_SHORT).show()
        }

        setContentView(R.layout.activity_main)

        val etKullaniciAdi = findViewById<EditText>(R.id.etKullaniciAdi)
        val btnGiris = findViewById<Button>(R.id.btnGiris)
        val llHeader = findViewById<android.view.View>(R.id.llHeader)
        val llInputContainer = findViewById<android.view.View>(R.id.llInputContainer)

        llHeader.alpha = 0f
        llHeader.translationY = -50f
        llHeader.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        llInputContainer.alpha = 0f
        llInputContainer.translationY = 50f
        llInputContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        btnGiris.setOnClickListener {
            btnGiris.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .withEndAction {
                    btnGiris.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .withEndAction {
                            val kullaniciAdi = etKullaniciAdi.text.toString().trim()

                            if (kullaniciAdi.isNotEmpty()) {
                                sharedPref.edit().putString("KULLANICI_ADI", kullaniciAdi).apply()

                                Toast.makeText(this, "Kayıt Başarılı! Hoş geldin, $kullaniciAdi!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, MenuActivity::class.java)
                                startActivity(intent)
                                finish() }
                            else {
                                Toast.makeText(this, "Lütfen bir kullanıcı adı girin!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .start()
                }
                .start()
        }
    }
}