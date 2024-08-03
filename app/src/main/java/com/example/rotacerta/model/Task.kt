package com.example.rotacerta.model

import android.os.Parcelable
import com.example.rotacerta.utils.FirebaseHelper
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    //adiciona os campos do banco
    var documentId: String = "",
    //DADOS RESPONSAVEL
    var email: String? = null,
    var cpf: String? = null,
    var nomeCompleto: String? = null,
    var rua: String? = null,
    var numero: String? = null,
    var cep: String? = null,
    var cidade: String? = null,
    var estado: String? = null,
    var ddd: String? = null,
    var telefone: String? = null ,
    //dados do aluno
    var nomeCompletoAluno: String? = null,
    var escola: String? = null,
    var turno: String? = null,
    var pontoReferencia: String? = null,
    var observacoes: String? = null,
    //dados para criar rotas
    var latitude: Double? = null,
    var longitude: Double? = null
) : Parcelable
