package mx.tecnm.cdhidalgo.tiendaregalos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.tiendaregalos.adaptadores.AdaptadorArtesania
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.FavoritosManager
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Producto
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Usuario

class FavoritosActivity : AppCompatActivity() {

    private lateinit var rvFavoritos: RecyclerView
    private lateinit var btnRegresar: ImageButton
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
        btnRegresar = findViewById(R.id.btn_regresar_favoritos)

        rvFavoritos.layoutManager = LinearLayoutManager(this)

        val lista = ArrayList<Producto>()
        lista.addAll(FavoritosManager.obtenerFavoritos())

        val adaptador = AdaptadorArtesania(lista)
        rvFavoritos.adapter = adaptador

        // Click: ver detalle
        adaptador.onProductoClick = { producto ->
            val intent = Intent(this, DetalleProducto::class.java)
            intent.putExtra("usuario", usuario)
            intent.putExtra("producto", producto)
            startActivity(intent)
        }

        // Long click: agregar al carrito
        adaptador.onProductoLongClick = { producto ->
            val agregado = CarritoManager.agregarProducto(producto, 1)
            if (agregado) {
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Sin stock suficiente", Toast.LENGTH_SHORT).show()
            }
        }

        // Si desde aquí quitas un favorito con el corazón, sácalo de la lista
        adaptador.onFavoritoCambiado = { producto, esFavorito ->
            if (!esFavorito) {
                lista.remove(producto)
                adaptador.notifyDataSetChanged()
            }
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }
}
