package com.example.gani.trekapp

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.trek_list_item.view.*

class TreksListAdapter(val items : ArrayList<Pair<Int, String>>, val context: Context) : RecyclerView.Adapter<ViewHolder>(){

    var onItemClick: ((String) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder as MyViewHolder).listItem.text = items.get(position).second
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.trek_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition].second)
            }
        }

        // Holds the TextView that will add each animal to
        val listItem = view.trek_item
    }


}
