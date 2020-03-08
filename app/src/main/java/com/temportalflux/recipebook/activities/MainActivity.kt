package com.temportalflux.recipebook.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
class MainActivity : AppCompatActivity() {

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
		this.recipeListAdapter.readNamesFromDisk(this)
		this.recipeListAdapter.readContentsFromDisk(this)
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
					recipes.add(Recipe(recipeFileName, true).withContent(recipeContent))
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

	private fun onFetchedRecipes(list:List<Recipe>)
	{
		for (recipe in list)
		{
			Log.d("Main", recipe.getFileName())
			if (!recipe.getFile(this).exists())
			{
				recipe.writeToDisk(this)
			}
		}
		this.recipeListAdapter.readNamesFromDisk(this)
		this.recipeListAdapter.readContentsFromDisk(this)
	}

}
