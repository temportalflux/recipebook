package com.temportalflux.recipebook.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.Recipe

class ViewRecipeActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_recipe_view)

		val recipe = this.intent.getParcelableExtra<Recipe>("recipe")
		if (recipe != null)
		{
			this.displayRecipe(recipe)
		}

	}

	private fun displayRecipe(recipe:Recipe)
	{
		Log.d("ViewRecipe", recipe.getName())
		findViewById<TextView>(R.id.label_recipeName).text = recipe.getName()
	}

}