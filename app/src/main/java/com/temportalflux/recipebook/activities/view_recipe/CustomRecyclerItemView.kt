package com.temportalflux.recipebook.activities.view_recipe

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class CustomRecyclerItemView<TData>(itemView: View) : RecyclerView.ViewHolder(itemView) {

	abstract fun setData(datum: TData)

}