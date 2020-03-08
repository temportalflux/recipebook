package com.temportalflux.recipebook.activities

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.temportalflux.recipebook.R
import com.temportalflux.recipebook.Recipe

class NewRecipeActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_recipe_new)

		this.markAlreadyExists(false)

		findViewById<EditText>(R.id.field_recipeName).setOnKeyListener { view: View, i: Int, keyEvent: KeyEvent ->
			this.markAlreadyExists(constructRecipe().getFile(this).exists())
			true
		}

		findViewById<Button>(R.id.btn_confirm).setOnClickListener {
			constructRecipe().writeToDisk(this)
			finish()
		}
	}

	private fun constructRecipe(): Recipe {
		return Recipe(
			name = findViewById<EditText>(R.id.field_recipeName).text.toString()
		)
	}

	private fun markAlreadyExists(bExists: Boolean) {
		val field = findViewById<EditText>(R.id.field_recipeName)
		field.setCompoundDrawablesRelativeWithIntrinsicBounds(
			0, 0, if (bExists) R.drawable.hazard_icon else 0, 0
		)
		field.tooltipText = if (bExists) getString(R.string.already_exists) else ""
		findViewById<Button>(R.id.btn_confirm).isEnabled = !bExists
	}

}