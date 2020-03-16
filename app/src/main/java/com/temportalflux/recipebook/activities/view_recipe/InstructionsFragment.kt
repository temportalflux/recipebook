package com.temportalflux.recipebook.activities.view_recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.data.Recipe

class InstructionsFragment(private val recipe: Recipe?) : Fragment() {

	private lateinit var categoryRecycler: RecyclerView

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val contentView = inflater.inflate(R.layout.recipe_view_directions, container, false)

		contentView.findViewById<TextView>(R.id.RecipeView_Direction_ActiveTime).text = recipe?.activeTime ?: ""
		contentView.findViewById<TextView>(R.id.RecipeView_Direction_TotalTime).text = recipe?.totalTime ?: ""

		this.categoryRecycler = contentView.findViewById(R.id.RecipeView_Instructions_CategoryList)
		this.categoryRecycler.layoutManager = LinearLayoutManager(contentView.context)
		this.categoryRecycler.itemAnimator = DefaultItemAnimator()
		val categoryAdapter = CustomRecyclerAdapter(R.layout.instruction_category) { view: View ->
			InstructionCategoryItemView(view)
		}
		this.categoryRecycler.adapter = categoryAdapter

		categoryAdapter.setData(this.recipe?.getInstructions() ?: listOf())

		return contentView
	}

}