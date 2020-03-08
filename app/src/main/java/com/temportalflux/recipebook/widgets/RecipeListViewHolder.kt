package com.temportalflux.recipebook.widgets

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.Recipe

class RecipeListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val view_name:TextView = itemView.findViewById(R.id.text_name)

	fun setData(data: Recipe)
	{
		this.view_name.text = data.getName()
	}

}