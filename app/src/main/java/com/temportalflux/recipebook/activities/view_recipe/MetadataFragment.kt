package com.temportalflux.recipebook.activities.view_recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.data.Recipe

class MetadataFragment(private val recipe: Recipe?) : Fragment()
{
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		val contentView = inflater.inflate(R.layout.recipe_view_metadata, container, false)

		contentView.findViewById<TextView>(R.id.RecipeView_Metadata_Source).text = recipe?.source ?: ""
		contentView.findViewById<TextView>(R.id.RecipeView_Metadata_URL).text = recipe?.url ?: ""

		return contentView
	}
}