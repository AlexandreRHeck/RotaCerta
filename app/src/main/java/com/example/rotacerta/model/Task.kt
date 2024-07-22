package com.example.rotacerta.model

import android.os.Parcelable
import com.example.rotacerta.utils.FirebaseHelper
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    //adiciona os campos do banco
    // var descricao : String =""
    var id : String = "",
    var title : String = "",
    var documentId: String = "",
    var nomeCompleto: String = "",
    var cpf: String = "",
    var telefone: String = "",
    var email: String = "",
    var senha: String = ""


) : Parcelable {
    init{
        this.id = FirebaseHelper.getDataBase().push().key ?: ""
    }
}
