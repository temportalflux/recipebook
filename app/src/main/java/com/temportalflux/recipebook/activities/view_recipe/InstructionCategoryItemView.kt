package com.temportalflux.recipebook.activities.view_recipe

import android.util.Log
import android.view.View
import android.widget.TextView
import com.temportalflux.recipebook.data.InstructionCategory
import com.temportalflux.recipebook.R

class InstructionCategoryItemView(itemView: View) :
	CategoryItemView<InstructionCategory, String, InstructionItemView>(
		itemView,
		R.id.InstructionCategory_List,
		R.layout.instruction,
		{ view: View -> InstructionItemView(view) }
	) {
	override fun setData(datum: InstructionCategory) {
		Log.d(datum.label, datum.getInstructions().joinToString { it })
		this.itemView.findViewById<TextView>(R.id.InstructionCategory_Title).text = datum.label
		this.getAdapter().setData(datum.getInstructions())
	}
}