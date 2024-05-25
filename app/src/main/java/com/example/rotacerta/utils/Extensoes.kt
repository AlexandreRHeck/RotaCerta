package com.example.rotacerta.utils

import android.app.Activity
import android.widget.Toast

fun Activity.exibirMensagem(mensagem: String){

    // qualqer activity vai ter acesso

    Toast.makeText(this,
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}

