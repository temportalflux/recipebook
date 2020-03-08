package com.temportalflux.recipebook.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.utils.*
import org.json.JSONArray
import android.widget.AdapterView.OnItemSelectedListener as OnItemSelectedListener1

class GithubAuthActivity : AppCompatActivity() {

	private lateinit var branchesAdapter: ArrayAdapter<String>
	private lateinit var requestQueue: RequestQueue

	private fun getFieldPrivateKey(): EditText {
		return this.findViewById(R.id.field_privateKey)
	}

	private fun getFieldRepository(): EditText {
		return this.findViewById(R.id.field_repository)
	}

	private fun getFieldBranch(): Spinner {
		return this.findViewById(R.id.field_branch)
	}

	private fun getLastKnownBranches(): MutableSet<String>? {
		return getUserPrefs(this).getStringSet(
			getString(R.string.pref_githubBranches),
			mutableSetOf()
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_githubauth)

		this.requestQueue = Volley.newRequestQueue(this)

		this.getFieldPrivateKey().setText(getGithubPrivateKeyValue(this))
		this.getFieldRepository().setText(getGithubRepositoryValue(this))

		this.branchesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
		this.branchesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		this.getFieldBranch().adapter = this.branchesAdapter
		this.getFieldBranch().onItemSelectedListener = object : OnItemSelectedListener1 {

			override fun onNothingSelected(parent: AdapterView<*>?) {
				if (parent == null) {
					return
				}
				with(getUserPrefs(parent.context).edit()) {
					remove(getString(R.string.pref_githubBranch))
					commit()
				}
			}

			override fun onItemSelected(
				parent: AdapterView<*>?,
				view: View?,
				position: Int,
				id: Long
			) {
				if (parent == null) {
					return
				}
				with(getUserPrefs(parent.context).edit()) {
					putString(
						getString(R.string.pref_githubBranch),
						(view as TextView).text.toString()
					)
					commit()
				}
			}

		}
		this.refreshBranchesAdapter()

		this.findViewById<Button>(R.id.btn_login).setOnClickListener {
			val privateKey = this.getFieldPrivateKey().text.toString()
			val repositoryName = this.getFieldRepository().text.toString()
			with(getUserPrefs(this).edit()) {
				putString(getString(R.string.pref_githubPrivateKey), privateKey)
				putString(getString(R.string.pref_githubRepository), repositoryName)
				// remove the branch entries in case authentication fails
				remove(getString(R.string.pref_githubUserName))
				remove(getString(R.string.pref_githubBranches))
				remove(getString(R.string.pref_githubBranch))
				commit()
			}

			findViewById<TextView>(R.id.label_error).visibility = View.GONE

			if (hasGithubAuth(this)) {
				this.authenticate()
			}
		}
	}

	private fun refreshBranchesAdapter() {
		this.branchesAdapter.clear()
		val branches = this.getLastKnownBranches()
		this.getFieldBranch().visibility = if (branches != null) View.VISIBLE else View.GONE
		var firstBranch: String? = null
		if (branches != null) {
			for (branch in branches.sorted()) {
				this.branchesAdapter.add(branch)
				if (firstBranch == null) firstBranch = branch
			}
		}
		this.branchesAdapter.notifyDataSetChanged()
		if (firstBranch != null) {
			with(getUserPrefs(this).edit()) {
				putString(getString(R.string.pref_githubBranch), firstBranch)
				commit()
			}
		}
	}

	private fun authenticate() {
		this.requestQueue.add(createGithubQuery(this, "viewer { login }",
			Response.Listener { response ->
				val user = response
					.getJSONObject("data")
					.getJSONObject("viewer")
					.getString("login")
				Log.i("Github", "Successfully authenticated as user \"$user\"")
				this.onAuthSuccessful(user)
			},
			Response.ErrorListener { error ->
				findViewById<TextView>(R.id.label_error).visibility = View.VISIBLE
				findViewById<TextView>(R.id.label_error).text = "Error authenticating user"
				Log.e("Github", error.toString())
			}
		))
	}

	private fun onAuthSuccessful(user: String) {
		with(getUserPrefs(this).edit()) {
			putString(getString(R.string.pref_githubUserName), user)
			commit()
		}

		val repositoryName = getGithubRepositoryValue(this)
		if (repositoryName == null) {
			findViewById<TextView>(R.id.label_error).visibility = View.VISIBLE
			findViewById<TextView>(R.id.label_error).text =
				"Error validating repository, no valid value"
			return
		}
		this.validateRepository(user, repositoryName,
			{ owner: String, repoName: String ->
				Log.i("Github", "Validated $owner:$repoName as a valid repository")
				onRepositoryValidated(owner, repoName)
			}, { owner: String, repoName: String, errors: JSONArray? ->
				findViewById<TextView>(R.id.label_error).visibility = View.VISIBLE
				findViewById<TextView>(R.id.label_error).text =
					"Error validating repository, does the repository exist?"
				if (errors == null) return@validateRepository
				if (errors.length() > 0 && errors.getJSONObject(0).getString("type") == "NOT_FOUND") {
					Log.e(
						"Github",
						"Failed to validate $owner:$repoName, ${errors.getJSONObject(0).getString("message")}"
					)
					return@validateRepository
				}
				Log.e(
					"Github",
					"Unknown error(s) when validating repository $owner:$repoName. $errors"
				)
			}
		)
	}

	private fun validateRepository(
		owner: String,
		repositoryName: String,
		onSuccess: (owner: String, repository: String) -> Unit,
		onFailure: (owner: String, repository: String, errors: JSONArray?) -> Unit
	) {
		this.requestQueue.add(createGithubQuery(this,
			"""
			repository(owner:"$owner", name:"$repositoryName") {
				id
			}
			""".trimIndent(),
			Response.Listener { response ->
				if (response.has("errors")) {
					onFailure(owner, repositoryName, response.getJSONArray("errors"))
					return@Listener
				}
				onSuccess(owner, repositoryName)
			},
			Response.ErrorListener { error ->
				Log.e("Github", error.toString())
				onFailure(owner, repositoryName, null)
			}
		))
	}

	private fun onRepositoryValidated(owner: String, repositoryName: String) {
		this.fetchRepositoryBranches(owner, repositoryName, 10,
			{
				Log.i("Github", "Found ${it.size} branches: $it")
				findViewById<TextView>(R.id.label_error).visibility = View.GONE
				with(getUserPrefs(this).edit()) {
					putStringSet(getString(R.string.pref_githubBranches), it.toSet())
					commit()
				}
				this.refreshBranchesAdapter()
			}, {
				findViewById<TextView>(R.id.label_error).visibility = View.VISIBLE
				findViewById<TextView>(R.id.label_error).text = "Error fetching repository branches"
				Log.e(
					"Github",
					"Unknown error(s) while fetching branches for $owner:$repositoryName. $it"
				)
			}
		)
	}

	/**
	 * Fetches the branches of a github repository.
	 * Asynchronously executed via `Volley`, returning the branches via `onSuccess`.
	 */
	@Suppress("SameParameterValue")
	private fun fetchRepositoryBranches(
		owner: String,
		repositoryName: String,
		amount: Int,
		onSuccess: (branches: List<String>) -> Unit,
		onFailure: (errors: JSONArray?) -> Unit
	) {
		this.requestQueue.add(createGithubQuery(this,
			"""
			repository(owner:"$owner", name:"$repositoryName") {
				refs(refPrefix:"refs/heads/", first:$amount) {
					edges {
						node {
							name
						}
					}
				}
			}
			""".trimIndent(),
			Response.Listener { response ->
				if (response.has("errors")) {
					onFailure(response.getJSONArray("errors"))
					return@Listener
				}
				val items = response
					.getJSONObject("data")
					.getJSONObject("repository")
					.getJSONObject("refs")
					.getJSONArray("edges")
				val branches = mutableListOf<String>()
				for (idx in 0 until items.length()) {
					branches.add(items.getJSONObject(idx).getJSONObject("node").getString("name"))
				}
				onSuccess(branches)
			},
			Response.ErrorListener { error ->
				Log.e("Github", error.toString())
				onFailure(null)
			}
		))
	}

}
