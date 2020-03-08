package com.temportalflux.recipebook

import android.content.Context
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.StringReader
import java.util.*

class Recipe(private var name: String, private var bIsDirty: Boolean = false) {

	private val ingredients: MutableList<String> = mutableListOf()
	private val steps: MutableList<String> = mutableListOf()

	fun getName(): String = this.name

	fun markDirty() {
		this.bIsDirty = true
	}

	fun isDirty(): Boolean = this.bIsDirty

	fun withContent(textFileContent:String):Recipe
	{
		deserialize(BufferedReader(StringReader(textFileContent)))
		return this
	}

	fun getFileName(): String {
		return this.name.trim()
			.replace(' ', '_')
			.replace(Regex("[`~!@#$%^&*()\\-\\[\\]{}:;\"'],\\.<>?/\\\\"), "")
			.toLowerCase(Locale.ENGLISH)
	}

	fun getFile(ctx: Context): File {
		return File(ctx.filesDir, this.getFileName())
	}

	fun writeToDisk(ctx: Context) {
		ctx.openFileOutput(this.getFileName(), Context.MODE_PRIVATE).bufferedWriter().use {
			this.serialize(it)
		}
	}

	fun readFromDisk(ctx: Context) {
		ctx.openFileInput(this.getFileName()).bufferedReader().use {
			this.deserialize(it)
		}
	}

	fun serialize(writer: BufferedWriter) {
		writer.write(this.name)
		writer.newLine()
	}

	fun deserialize(reader: BufferedReader) {
		this.name = reader.readLine()

		this.bIsDirty = false
	}

}