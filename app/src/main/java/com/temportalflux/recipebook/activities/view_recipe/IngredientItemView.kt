package com.temportalflux.recipebook.activities.view_recipe

import android.view.View
import android.widget.TextView
import com.temportalflux.recipebook.data.Ingredient
import com.temportalflux.recipebook.R

class IngredientItemView(itemView: View) : CustomRecyclerItemView<Ingredient>(itemView) {

	override fun setData(datum: Ingredient) {
		itemView.findViewById<TextView>(R.id.ingredient_amount).text = datum.amount
		itemView.findViewById<TextView>(R.id.ingredient_unit).text = datum.unit
		itemView.findViewById<TextView>(R.id.ingredient_name).text = datum.type
	}

}