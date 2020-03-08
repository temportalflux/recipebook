package com.temportalflux.recipebook.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.utils.getGithubBranchValue
import com.temportalflux.recipebook.widgets.RecipeListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.temportalflux.recipebook.Recipe
import com.temportalflux.recipebook.utils.createGithubQuery
import com.temportalflux.recipebook.utils.getGithubRepositoryValue
import com.temportalflux.recipebook.utils.getUserPrefs

// https://codelabs.developers.google.com/codelabs/build-your-first-android-app-kotlin/index.html#7
// https://www.androidauthority.com/android-app-development-complete-beginners-658469/
// https://developer.android.com/guide/topics/ui/layout/recyclerview
// http://www.androidtutorialshub.com/android-recyclerview-tutorial/

interface OnItemGestureListener {
	fun onItemPress(view: View, position: Int)
	fun onLongItemPress(view: View, position: Int)
}

class MainActivity : AppCompatActivity(), OnItemGestureListener {

	inner class GestureClickListener(
		private val parentView: RecyclerView,
		private val listener: OnItemGestureListener
	) :
		RecyclerView.OnItemTouchListener {

		private val gestureDetector: GestureDetector =
			GestureDetector(parentView.context, object : GestureDetector.OnGestureListener {

				override fun onShowPress(e: MotionEvent?) {}

				override fun onLongPress(e: MotionEvent?) {
					if (e == null) {
						return
					}
					val child = parentView.findChildViewUnder(e.x, e.y)
					if (child != null) {
						listener.onLongItemPress(
							child,
							parentView.getChildAdapterPosition(child)
						)
					}
				}

				override fun onSingleTapUp(e: MotionEvent?): Boolean = true

				override fun onDown(e: MotionEvent?): Boolean = false

				override fun onFling(
					e1: MotionEvent?,
					e2: MotionEvent?,
					velocityX: Float,
					velocityY: Float
				): Boolean = false

				override fun onScroll(
					e1: MotionEvent?,
					e2: MotionEvent?,
					distanceX: Float,
					distanceY: Float
				): Boolean = false
			})

		override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

		override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
			val child = parentView.findChildViewUnder(e.x, e.y)
			if (child != null && gestureDetector.onTouchEvent(e)) {
				listener.onItemPress(
					child,
					parentView.getChildAdapterPosition(child)
				)
			}
			return false
		}

		override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

	}

	private lateinit var requestQueue: RequestQueue
	private lateinit var recipeListView: RecyclerView
	private lateinit var recipeListAdapter: RecipeListAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.setContentView(R.layout.activity_main)
		this.setSupportActionBar(this.toolbar)
		this.requestQueue = Volley.newRequestQueue(this)

		this.fab_addRecipe.setOnClickListener {
			this.startActivity(Intent(this, NewRecipeActivity::class.java))
		}

		this.recipeListView = findViewById(R.id.recipelist)
		this.recipeListView.layoutManager = LinearLayoutManager(this.applicationContext)
		this.recipeListView.itemAnimator = DefaultItemAnimator()
		this.recipeListAdapter = RecipeListAdapter()
		this.recipeListView.adapter = this.recipeListAdapter
		this.recipeListView.addOnItemTouchListener(GestureClickListener(this.recipeListView, this))
	}

	override fun onItemPress(view: View, position: Int) {
		val recipe = this.recipeListAdapter.getDataAt(position)
		if (recipe != null)
		{
			startActivity(Intent(this, ViewRecipeActivity::class.java).putExtra("recipe", recipe))
		}
	}

	override fun onLongItemPress(view: View, position: Int) {
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return when (item.itemId) {
			R.id.action_settings -> {
				startActivity(Intent(this, GithubAuthActivity::class.java).putExtra("keep", false))
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onResume() {
		super.onResume()
		this.refreshEntries()
	}

	private fun refreshEntries() {
		Log.d("Main", getGithubBranchValue(this) ?: "no branch")
		fetchFiles()
	}

	private fun fetchFiles() {
		val owner =
			getUserPrefs(this).getString(getString(R.string.pref_githubUserName), null) ?: return
		val repositoryName = getGithubRepositoryValue(this) ?: return
		val branch = getGithubBranchValue(this) ?: return
		this.requestQueue.add(createGithubQuery(this,
			"""
			repository(owner:"$owner", name:"$repositoryName") {
				ref(qualifiedName:"$branch") {
					target {
						... on Commit {
							oid
							tree {
								entries {
									name
									oid
									object {
										... on Blob {
											text
										}
									}
								}
							}
						}
					}
				}
			}
			""".trimIndent(),
			Response.Listener { response ->
				val commitData = response
					.getJSONObject("data")
					.getJSONObject("repository")
					.getJSONObject("ref")
					.getJSONObject("target")
				val commitSha = commitData.getString("oid")
				val recipeDataArray = commitData
					.getJSONObject("tree")
					.getJSONArray("entries")
				val recipes: MutableList<Recipe> = mutableListOf()
				for (idx in 0 until recipeDataArray.length()) {
					val recipeDatum = recipeDataArray.getJSONObject(idx)
					val recipeFileName = recipeDatum.getString("name")
					val recipeContent = recipeDatum
						.getJSONObject("object")
						.getString("text")
					recipes.add(
						// these recipes are never dirty, because they always have fresh content from the remote repository
						Recipe(recipeFileName, false)
							.withId(recipeDatum.getString("oid"))
							.withContent(recipeContent)
					)
				}
				Log.d("Main", "Found ${recipes.size} recipes at commit $commitSha")
				with(getUserPrefs(this).edit()) {
					putString(getString(R.string.pref_githubCommitSha), commitSha)
					commit()
				}
				findViewById<TextView>(R.id.label_commitSha).text = commitSha
				onFetchedRecipes(recipes)
			},
			Response.ErrorListener { error ->
				Log.e("Github", error.toString())
			}
		))
	}

	private fun onFetchedRecipes(list: List<Recipe>) {
		for (recipe in list) {
			if (!recipe.getFile(this).exists()) {
				recipe.writeToDisk(this)
			}
		}
		this.recipeListAdapter.setRecipes(list)
		this.recipeListAdapter.deleteUnlistedFiles(this)
		this.recipeListAdapter.notifyDataSetChanged()
	}

}
