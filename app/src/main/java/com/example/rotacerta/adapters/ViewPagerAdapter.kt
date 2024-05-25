package com.example.rotacerta.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rotacerta.fragments.CadastrarFragment
import com.example.rotacerta.fragments.HomeFragment
import com.example.rotacerta.fragments.ProcurarFragment
import com.example.rotacerta.fragments.RotaFragment

class ViewPagerAdapter(
    private val abas : List<String>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager,lifecycle) {
    override fun getItemCount(): Int {
    return abas.size //istOf(0 -> "CONVERSAS",1 ->"CONTATOS" 2 -> "Outras abas nas posicoes em sequencias")
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            1 -> return CadastrarFragment() //aba do fragnmnt
            2 -> return ProcurarFragment()
            3 -> return RotaFragment()
        }
        return  HomeFragment()//homeFragment() // esse fragent de retorn eh posicao 0
    }


}