package com.example.rotacerta.model

import android.os.Parcelable
import com.example.rotacerta.utils.FirebaseHelper
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    //adiciona os campos do banco
    // var descricao : String =""
    var id : String? = null,
    var title : String? = null,
    var documentId: String? = null,
    var nomeCompleto: String? = null,
    var cpf: String? = null,
    var telefone: String? = null,
    var email: String? = null,
    var senha: String? = null,
    val turno: Int? = null


) : Parcelable {
    init{
        this.id = FirebaseHelper.getDataBase().push().key ?: ""
    }
}
