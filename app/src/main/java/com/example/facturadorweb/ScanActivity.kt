package com.example.facturadorweb

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import co.metalab.asyncawait.async
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit


class ScanActivity : AppCompatActivity() {

    var idUsuario: Int = 0;
    var codigoRespuestaLogin: String? = ""
    var mensajeRespuestaLogin: String? = ""

    /* PETICION - INICIO */
    val TAG_LOGS = "ChristianFigueredo"
    lateinit var service: ApiService
    /* PETICION - FIN */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        /*  PARA LA PETICION - INICIO */
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client =  OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .build()
        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://webapi-facturacion.conveyor.cloud/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create<ApiService>(ApiService::class.java)
        /*  PARA LA PETICION - FIN */

        val objetoIntent: Intent = intent
        var Nombre = objetoIntent.getStringExtra("Nombre")
        var Apellido = objetoIntent.getStringExtra("Apellido")
        var IdUsuario = objetoIntent.getIntExtra("IdUsuario", 0)
        idUsuario = IdUsuario
        txtSaludo.text = "Hola $Nombre $Apellido bienvenido al ScanActivity su Id es $IdUsuario"

        btnCerrarSesion.setOnClickListener(){
            val intent: Intent  = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnScan.setOnClickListener(){
            val scanner = IntentIntegrator(this)

            scanner.initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    var Flag: Boolean = false
                    async {
                        Flag = addProduct(result.contents.toInt(), idUsuario)
                    }
                    if(Flag){
                        if(codigoRespuestaLogin == "0000") {
                            Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                        }else{
                            println("OCURRIO UN ERROR. " + codigoRespuestaLogin + " " + mensajeRespuestaLogin)
                            Toast.makeText(this, "OCURRIO UN ERROR. " + codigoRespuestaLogin + " " + mensajeRespuestaLogin, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(this, "OCURRIO UN ERROR.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    suspend fun addProduct(CodBarras: Int, IdUsuario: Int) : Boolean {
        var requestProducto: RequestProduct? = RequestProduct(CodBarras, IdUsuario)
        var responseProducto: RESP_TRANSACCION? = null
        var resultado : Boolean = false

        GlobalScope.async  (Dispatchers.IO) {
            var responseProducto = service.sendProduct(requestProducto).await()
            codigoRespuestaLogin = responseProducto?.codigo
            mensajeRespuestaLogin = responseProducto?.mensaje
            resultado = true
        }.await()
        return resultado
    }
}
