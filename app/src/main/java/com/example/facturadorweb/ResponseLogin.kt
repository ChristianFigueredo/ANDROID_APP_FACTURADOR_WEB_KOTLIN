package com.example.facturadorweb

class ResponseLogin (
    var iD_USUARIO: Int,
    var nombre: String,
    var apellido: String,
    var perfil: String,
    var estado: Boolean,
    var respuestA_TRANSACCION: RESP_TRANSACCION
)