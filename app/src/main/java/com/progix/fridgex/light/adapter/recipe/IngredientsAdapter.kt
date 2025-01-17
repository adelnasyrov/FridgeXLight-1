package com.progix.fridgex.light.adapter.recipe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R


class IngredientsAdapter(
    var context: Context,
    var ingredientsList: ArrayList<Pair<String, String>>
) :
    RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_ingreds, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.prodName.text = ingredientsList[position].first.replaceFirstChar(Char::uppercase)
        holder.amount.text = ingredientsList[position].second
    }

    override fun getItemCount(): Int {
        return ingredientsList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var prodName: TextView = view.findViewById(R.id.name)
        val amount: TextView = view.findViewById(R.id.amount)

    }


}