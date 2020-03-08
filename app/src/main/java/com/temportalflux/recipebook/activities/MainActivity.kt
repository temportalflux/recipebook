package com.temportalflux.recipebook.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.Recipe
import com.temportalflux.recipebook.utils.createGithubQuery
import com.temportalflux.recipebook.utils.getGithubPrivateKey
import com.temportalflux.recipebook.utils.hasGithubAuth
import com.temportalflux.recipebook.widgets.RecipeListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

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

		this.requestQueue = Volley.newRequestQueue(this)

		this.setContentView(R.layout.activity_main)
		this.setSupportActionBar(this.toolbar)

		this.fab_addRecipe.setOnClickListener {
			this.startActivity(Intent(this, NewRecipeActivity::class.java))
		}

		this.recipeListView = findViewById(R.id.recipelist)
		this.recipeListView.layoutManager = LinearLayoutManager(this.applicationContext)
		this.recipeListView.itemAnimator = DefaultItemAnimator()
		this.recipeListAdapter = RecipeListAdapter()
		this.recipeListView.adapter = this.recipeListAdapter

		this.recipeListAdapter.readNamesFromDisk(this)
		this.recipeListAdapter.readContentsFromDisk(this)

		if (hasGithubAuth(this)) {
			this.authenticate()
		}
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
		this.recipeListAdapter.readNamesFromDisk(this)
		this.recipeListAdapter.readContentsFromDisk(this)
	}

	fun authenticate() {
		this.requestQueue.add(createGithubQuery(this, "viewer { login }",
			Response.Listener { response ->
				val userName =
					response.getJSONObject("data").getJSONObject("viewer").getString("login")
				Log.d("Github", "Successfully authenticated as user \"$userName\"")
			},
			Response.ErrorListener { error -> Log.d("Github", error.toString()) }
		))
	}

}
