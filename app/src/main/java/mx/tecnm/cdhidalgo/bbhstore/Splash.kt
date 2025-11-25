package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Splash : AppCompatActivity() {
    private lateinit var logo: ImageView
    private lateinit var animacion: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        logo = findViewById(R.id.logo_splash)
        animacion = AnimationUtils.loadAnimation(this,R.anim.anim_splash)

        logo.startAnimation(animacion)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                val intent = Intent(this, Login::class.java)

                // 2. Comentamos o eliminamos las líneas de la animación de transición
                /*
                val trans = ActivityOptions.makeSceneTransitionAnimation(
                    this, Pair(logo, "logo_trans"))
                startActivity(intent, trans.toBundle())
                */

                // 3. Usamos la forma más simple y segura de iniciar la actividad
                startActivity(intent)

                // 4. Cerramos la pantalla Splash para que el usuario no pueda volver a ella
                finish()
            },4000
        )
    }
}