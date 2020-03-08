package com.temportalflux.recipebook.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.temportalflux.recipebook.R
import org.json.JSONObject

fun getUserPrefs(ctx: ContextWrapper): SharedPreferences {
	return ctx.getSharedPreferences(
		ctx.getString(R.string.preference_file_key),
		Context.MODE_PRIVATE
	)
}

fun getGithubPrivateKey(ctx:ContextWrapper):String?
{
	return getUserPrefs(ctx)
		.getString(ctx.getString(R.string.githubPrivateKey), null)
}

fun hasGithubAuth(ctx:ContextWrapper):Boolean { return getGithubPrivateKey(ctx) != null }

class JsonHeaderRequest(
	method: Int, url: String,
	private val headers: MutableMap<String, String>,
	jsonRequest: JSONObject,
	listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener
) : JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
	override fun getHeaders(): MutableMap<String, String> {
		return this.headers
	}
}

fun createGithubQuery(
	ctx: ContextWrapper, query: String,
	listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener
): JsonHeaderRequest? {
	val privateKey = getGithubPrivateKey(ctx)
		?: return null
	return JsonHeaderRequest(
		Request.Method.POST, "https://api.github.com/graphql",
		mutableMapOf("Authorization" to "bearer $privateKey"),
		JSONObject().put("query", "query{$query}"),
		listener, errorListener
	)
}
