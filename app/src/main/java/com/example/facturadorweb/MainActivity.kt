package com.example.facturadorweb

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import co.metalab.asyncawait.async
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import retrofit2.*
import kotlin.coroutines.coroutineContext


class MainActivity : AppCompatActivity() {

    var idUsuario : Int = 0
    var nombreUsuario: String = ""
    var apellidoUsuario: String = ""
    var estadoUsuario: Boolean = false
    var codigoRespuestaLogin: String? = ""

    val TAG_LOGS = "ChristianFigueredo"
    lateinit var service: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            .baseUrl("https://webapi-personas.conveyor.cloud/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create<ApiService>(ApiService::class.java)
        /*  PARA LA PETICION - FIN */

        btnLogin.setOnClickListener() {

            var Username: String = txtUsername.text.toString()
            var Password: String = txtPassword.text.toString()
            var Flag: Boolean = false

            if(!Username.isNullOrEmpty() && !Password.isNullOrEmpty())
            {
                async {
                    Flag = editPost(Username, Password)

                    if(Flag){

                        if(estadoUsuario)
                        {
                            if(codigoRespuestaLogin == "0000"){
                                val intent: Intent  = Intent(this@MainActivity, ScanActivity::class.java)
                                intent.putExtra("Nombre", nombreUsuario)
                                intent.putExtra("Apellido", apellidoUsuario)
                                intent.putExtra("IdUsuario", idUsuario)
                                startActivity(intent)
                            }else{
                                txtInfo.text = "Credentials error, please try again 2.0."
                            }
                        }else{
                            txtInfo.text = "El usuario no esta activo. 3.0."
                        }
                    } else {
                        txtInfo.text = "Error, please try again 1.0."
                    }
                }
            }else{
                txtInfo.text = "* Username and pasword are required 0.0."
            }
        }
    }


    suspend fun editPost(username: String, password: String) : Boolean {
        var requestLogin: RequestLogin? = RequestLogin(password, username)
        var responseLogin: ResponseLogin? = null
        var resultado : Boolean = false

        GlobalScope.async  (Dispatchers.IO) {
            var responseLogin = service.createSesion(requestLogin).await()
            idUsuario = responseLogin?.iD_USUARIO
            nombreUsuario = responseLogin?.nombre
            apellidoUsuario = responseLogin?.apellido
            estadoUsuario = responseLogin?.estado
            codigoRespuestaLogin = responseLogin?.respuestA_TRANSACCION?.codigo
            resultado = true
        }.await()
        return resultado
    }

}
