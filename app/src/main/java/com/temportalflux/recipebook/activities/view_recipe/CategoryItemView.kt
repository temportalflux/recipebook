package com.temportalflux.recipebook.activities.view_recipe

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class CategoryItemView<TDataCategory, TDataItem, TSubItemView:CustomRecyclerItemView<TDataItem>>(
	itemView: View,
	private val listViewId:Int,
	private val itemLayoutId:Int,
	private val createSubItemView:(view: View) -> TSubItemView
) : CustomRecyclerItemView<TDataCategory>(itemView) {

	private val recyclerList: RecyclerView = findRecyclerList(itemView)

	private fun findRecyclerList(itemView: View): RecyclerView {
		val recycler: RecyclerView = itemView.findViewById(this.listViewId)
		recycler.layoutManager = LinearLayoutManager(itemView.context)
		recycler.itemAnimator = DefaultItemAnimator()
		recycler.adapter = CustomRecyclerAdapter(this.itemLayoutId, this.createSubItemView)
		return recycler
	}

	protected fun getAdapter():CustomRecyclerAdapter<TDataItem, TSubItemView>
	{
		@Suppress("UNCHECKED_CAST")
		return this.recyclerList.adapter as CustomRecyclerAdapter<TDataItem, TSubItemView>
	}

}