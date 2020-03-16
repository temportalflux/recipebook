package com.temportalflux.recipebook.activities.view_recipe

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.data.Recipe

class ViewRecipeActivity : FragmentActivity() {

	private var recipe: Recipe? = null
	private lateinit var pager:ViewPager2

    private inner class ViewRecipePagerAdapter(fa: FragmentActivity, private val recipe: Recipe?) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment
        {
	        return when(position)
	        {
		        0 -> IngredientsFragment(this.recipe)
		        1 -> InstructionsFragment(this.recipe)
		        2 -> MetadataFragment(this.recipe)
		        else -> Fragment()
	        }
        }
    }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_recipe_view)

		this.recipe = this.intent.getParcelableExtra("recipe")
		Log.d("ViewRecipe", this.recipe?.getIngredients()?.joinToString {
			"${it.label}[${it.ingredients.joinToString { ingredient ->
				"${ingredient.amount} ${ingredient.unit} ${ingredient.type}"
			}}]"
		} ?: "null")
		this.findViewById<TextView>(R.id.label_recipeName).text = this.recipe?.getName() ?: getString(R.string.placeholder_unknown)

		this.pager = findViewById(R.id.recipe_view_pager)
		this.pager.adapter = ViewRecipePagerAdapter(this, this.recipe)

		findViewById<ImageButton>(R.id.btn_edit_recipe).setOnClickListener {
			Log.d("ViewRecipe", "Edit recipe!")
		}
	}

}