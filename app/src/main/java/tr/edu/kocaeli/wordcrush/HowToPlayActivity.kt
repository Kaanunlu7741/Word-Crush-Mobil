package tr.edu.kocaeli.wordcrush
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
class HowToPlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_play)

        findViewById<Button>(R.id.btnHTPGeri).setOnClickListener { finish() }
        val content = findViewById<LinearLayout>(R.id.llHTPContent)
        for (i in 0 until content.childCount) {
            val child = content.getChildAt(i)
            child.alpha = 0f
            child.translationY = 100f
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(100L * i)
                .setDuration(500)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
    }
}