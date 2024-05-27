package com.example.rotacerta.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth


class FirebaseHelper {

    companion object{

        fun getDataBase() = FirebaseDatabase.getInstance().reference //Retorna Referencia do banco
        fun getAuth() = FirebaseAuth.getInstance()
        fun getIdUser() =  getAuth().uid

        // fun isAutenticated = getAuth().currentUser != null

    }
}