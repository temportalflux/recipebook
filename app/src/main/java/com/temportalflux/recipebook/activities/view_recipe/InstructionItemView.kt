package com.temportalflux.recipebook.activities.view_recipe

import android.view.View
import android.widget.TextView
import com.temportalflux.recipebook.R

class InstructionItemView(itemView: View) : CustomRecyclerItemView<String>(itemView) {

	override fun setData(datum: String) {
		itemView.findViewById<TextView>(R.id.instruction_number).text = "${this.layoutPosition}"
		itemView.findViewById<TextView>(R.id.instruction_content).text = datum
	}

}