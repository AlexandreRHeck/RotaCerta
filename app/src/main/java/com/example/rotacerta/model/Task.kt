package com.example.rotacerta.model

import android.os.Parcelable
import com.example.rotacerta.utils.FirebaseHelper
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    //adiciona os campos do banco
    var id : String = "",
    var title : String = "",
    var nomeCompleto : String = "",
    // var descricao : String =""
) : Parcelable {
    init{
        this.id = FirebaseHelper.getDataBase().push().key ?: ""
    }
}
