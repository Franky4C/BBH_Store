package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdaptadorArtesania
import mx.tecnm.cdhidalgo.bbhstore.dataclass.FavoritosManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class FavoritosActivity : AppCompatActivity() {

    private lateinit var rvFavoritos: RecyclerView
    private lateinit var txtSinFavoritos: TextView
    private lateinit var btnRegresar: ImageButton

    private lateinit var adaptador: AdaptadorArtesania
    private val listaFavoritos = ArrayList<Producto>()
    private var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favoritos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usuario = intent.getParcelableExtra("usuario")

        rvFavoritos = findViewById(R.id.rv_favoritos)
        txtSinFavoritos = findViewById(R.id.txt_favoritos_vacio)
        btnRegresar = findViewById(R.id.btn_regresar_favoritos)

        rvFavoritos.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorArtesania(listaFavoritos)
        rvFavoritos.adapter = adaptador

        btnRegresar.setOnClickListener { finish() }

        adaptador.onProductoClick = { producto ->
            val intent = Intent(this, DetalleProducto::class.java)
            intent.putExtra("usuario", usuario)
            intent.putExtra("producto", producto)
            startActivity(intent)
        }

        cargarFavoritosEnUI()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritosEnUI()
    }

    private fun cargarFavoritosEnUI() {
        listaFavoritos.clear()
        listaFavoritos.addAll(FavoritosManager.obtenerFavoritos())
        adaptador.notifyDataSetChanged()

        txtSinFavoritos.visibility =
            if (listaFavoritos.isEmpty()) TextView.VISIBLE else TextView.GONE
    }
}
