/*package com.example.rotacerta.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rotacerta.databinding.ItemCadastradosAdapterBinding
import com.example.rotacerta.model.Task

class TaskAdapter(
    private val taskList: List<Task>,
    val taskSelected : (Task, Int) -> Unit
): RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    companion object {
        val SELECT_REMOVE : Int = 1
        val SELECT_EDIT : Int = 2

    }
    //BindViewHolder ao criar o viewHolderon -> cria a vizualizacao
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.MyViewHolder {
        return MyViewHolder(
            ItemCadastradosAdapterBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    // ao criar o viewHolder
    override fun onBindViewHolder(holder: TaskAdapter.MyViewHolder, position: Int) {
        val task = taskList[position]

        holder.biding.textViewcardNome.text = task.title
        // holder.biding.textViewcardDescricao.text = task.descricao

        holder.biding.btnCardDelite.setOnClickListener{taskSelected(task,SELECT_REMOVE)}
        holder.biding.btnCardEdit.setOnClickListener{taskSelected(task,SELECT_EDIT)}
    }

    // getItemCount() -> Recupera a quantidade de itens
    override fun getItemCount() = taskList.size

    inner class MyViewHolder(val biding:ItemCadastradosAdapterBinding):
        RecyclerView.ViewHolder(biding.root)
}
*/