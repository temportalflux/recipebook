package com.temportalflux.recipebook.activities.view_recipe

import android.view.View
import android.widget.TextView
import com.temportalflux.recipebook.data.Ingredient
import com.temportalflux.recipebook.data.IngredientCategory
import com.temportalflux.recipebook.R

class IngredientCategoryItemView(itemView: View) :
	CategoryItemView<IngredientCategory, Ingredient, IngredientItemView>(
		itemView,
		R.id.IngredientCategory_IngredientList,
		R.layout.ingredient,
		{ view: View -> IngredientItemView(view) }
	) {
	override fun setData(datum: IngredientCategory) {
		this.itemView.findViewById<TextView>(R.id.IngredientCategory_Title).text = datum.label
		this.getAdapter().setData(datum.ingredients)
	}
}