package com.temportalflux.recipebook.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.temportalflux.recipebook.R

import kotlinx.android.synthetic.main.activity_recipe_list.toolbar

class RecipeListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)
        setSupportActionBar(toolbar)
    }

}
