package com.temportalflux.recipebook.activities.view_recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class CustomRecyclerAdapter<TData, TItemView:CustomRecyclerItemView<TData>>(
	private val layoutId:Int,
	private val createItemView: (view: View)->TItemView
) : RecyclerView.Adapter<TItemView>() {

	private var data: List<TData> = listOf()

	fun setData(categories: List<TData>) {
		this.data = categories
		this.notifyDataSetChanged()
	}

	override fun getItemCount(): Int = this.data.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TItemView {
		return createItemView(
			LayoutInflater.from(parent.context).inflate(
				this.layoutId,
				parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: TItemView, position: Int) {
		holder.setData(this.data[position])
	}

}