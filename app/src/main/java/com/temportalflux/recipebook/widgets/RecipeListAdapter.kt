package com.temportalflux.recipebook.widgets

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.Recipe

class RecipeListAdapter : RecyclerView.Adapter<RecipeListViewHolder>() {

	private val recipes:MutableMap<String, Recipe> = mutableMapOf()
	private var sortedRecipeFilenameList:MutableList<String> = mutableListOf()

	// Return the size of your dataset (invoked by the layout manager)
	override fun getItemCount() = this.recipes.size

	// Create new views (invoked by the layout manager)
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeListViewHolder {
		// create a new view
		val itemView =
			LayoutInflater.from(parent.context).inflate(R.layout.recipe_list_item, parent, false)

		// set the view's size, margins, paddings and layout parameters
		// ...

		return RecipeListViewHolder(itemView)
	}

	// Replace the contents of a view (invoked by the layout manager)
	override fun onBindViewHolder(holder: RecipeListViewHolder, position: Int) {
		val recipe:Recipe? = this.recipes[this.sortedRecipeFilenameList[position]]
		if (recipe != null)
		{
			holder.setData(recipe)
		}
	}

	fun readNamesFromDisk(ctx: Context) {
		this.sortedRecipeFilenameList = ctx.fileList().toMutableList()
		Log.i("RecipeList", "Reading file names from disk. Found ${this.sortedRecipeFilenameList.size} recipe files.")
	}

	fun readContentsFromDisk(ctx: Context) {
		Log.i("RecipeList", "Reading the contents of ${this.sortedRecipeFilenameList.size} recipe files from disk.")
		for (recipeFileName in this.sortedRecipeFilenameList) {
			if (!this.recipes.containsKey(recipeFileName))
			{
				this.recipes[recipeFileName] = Recipe(recipeFileName, true)
			}
			val recipe = this.recipes[recipeFileName]
			if (recipe != null && recipe.isDirty())
			{
				// TODO: This is probably synchronous. Can it be asynchronous?
				recipe.readFromDisk(ctx)
			}
		}
		sort()
		this.notifyDataSetChanged()
	}

	fun sort()
	{
		this.sortedRecipeFilenameList.sortBy { recipeFileName -> this.recipes[recipeFileName]!!.getName() }
	}

}