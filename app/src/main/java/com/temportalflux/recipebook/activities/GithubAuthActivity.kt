package com.temportalflux.recipebook.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.utils.getGithubPrivateKey
import com.temportalflux.recipebook.utils.getUserPrefs

class GithubAuthActivity : AppCompatActivity() {

	private fun getPasswordInput(): EditText {
		return this.findViewById(R.id.password_input)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_githubauth)

		this.getPasswordInput().setText(
			getGithubPrivateKey(
				this
			)
		)

		this.findViewById<Button>(R.id.btn_login).setOnClickListener {
			val privateKey = getPasswordInput().text.toString()
			with(getUserPrefs(this).edit()) {
				putString(getString(R.string.githubPrivateKey), privateKey)
				commit()
			}
		}
	}

}