package com.temportalflux.recipebook

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.StringReader
import java.util.*

private fun amountStringToNumber(amt: String): Float {
	// TODO: ?????
	// 1 - straight int
	// 1.4623 - straight float
	// 1 1/12 - mixed number
	return 0.0F
}

class Ingredient(
	val amount: String,
	val unit: String,
	val type: String,
	val category: String?
) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readString()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(this.amount)
		parcel.writeString(this.unit)
		parcel.writeString(this.type)
		parcel.writeString(this.category)
	}

	override fun describeContents(): Int = 0

	companion object CREATOR : Parcelable.Creator<Ingredient> {
		override fun createFromParcel(parcel: Parcel): Ingredient {
			return Ingredient(parcel)
		}

		override fun newArray(size: Int): Array<Ingredient?> {
			return arrayOfNulls(size)
		}
	}

}

class Recipe(private var name: String, private var bIsDirty: Boolean = false) : Parcelable {

	private var id: String? = null

	private var source: String = ""
	private var url: String = ""
	private var aliases: List<String> = listOf()
	private var tags: List<String> = listOf()

	private var activeTime: String = ""
	private var totalTime: String = ""
	private var yield: String = ""

	private var description: String = ""

	private var ingredients: List<Ingredient> = listOf()
	private var instructions: List<String> = listOf()

	constructor(parcel: Parcel) : this(parcel.readString()!!, false) {
		this.id = parcel.readString()
		this.source = parcel.readString() ?: ""
		this.url = parcel.readString() ?: ""

		val aliasList = mutableListOf<String>()
		parcel.readStringList(aliasList)
		this.aliases = aliasList

		val tagList = mutableListOf<String>()
		parcel.readStringList(tagList)
		this.tags = tagList

		this.activeTime = parcel.readString() ?: ""
		this.totalTime = parcel.readString() ?: ""
		this.yield = parcel.readString() ?: ""
		this.description = parcel.readString() ?: ""

		this.ingredients = parcel.createTypedArray(Ingredient.CREATOR)?.toList() ?: listOf()

		val instructionList = mutableListOf<String>()
		parcel.readStringList(instructionList)
		this.instructions = instructionList

	}

	override fun describeContents(): Int = 0

	override fun writeToParcel(dest: Parcel?, flags: Int) {
		dest?.writeString(this.name)
		dest?.writeString(this.id)
		dest?.writeString(this.source)
		dest?.writeString(this.url)
		dest?.writeStringList(this.aliases)
		dest?.writeStringList(this.tags)
		dest?.writeString(this.activeTime)
		dest?.writeString(this.totalTime)
		dest?.writeString(this.yield)
		dest?.writeString(this.description)
		dest?.writeTypedArray(this.ingredients.toTypedArray(), 0)
		dest?.writeStringList(this.instructions)
	}

	companion object CREATOR : Parcelable.Creator<Recipe> {
		override fun createFromParcel(parcel: Parcel): Recipe {
			return Recipe(parcel)
		}

		override fun newArray(size: Int): Array<Recipe?> {
			return arrayOfNulls(size)
		}
	}

	fun getName(): String = this.name

	fun markDirty() {
		this.bIsDirty = true
	}

	fun isDirty(): Boolean = this.bIsDirty

	fun withId(id: String): Recipe {
		this.id = id
		return this
	}

	fun withContent(textFileContent: String): Recipe {
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

	fun deleteFile(ctx: Context) {
		this.getFile(ctx).delete()
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

	private fun serialize(writer: BufferedWriter) {
		// Meta header
		this.serializeField(writer, "", this.name)
		this.serializeField(writer, "source|", this.source)
		this.serializeField(writer, "url|", this.url)
		this.serializeField(writer, "aliases|", this.aliases.joinToString(",", transform = { it }))
		this.serializeField(writer, "tags|", this.tags.joinToString(",", transform = { it }))
		// Other info (still header)
		this.serializeField(writer, "activeTime|", this.activeTime)
		this.serializeField(writer, "totalTime|", this.totalTime)
		this.serializeField(writer, "yield|", this.yield)
		this.serializeField(writer, "description|", this.description)
		// end header
		writer.newLine()

		// Ingredients
		writer.write("# ingredients")
		var category: String? = null
		for (ingredient in this.ingredients) {
			if (category != ingredient.category) {
				category = ingredient.category
				writer.write("[$category]")
				writer.newLine()
			}
			writer.write("${ingredient.amount}|${ingredient.unit}|${ingredient.type}")
			writer.newLine()
		}
		writer.newLine()

		// Instructions
		// TODO: Serialize instruction categories
		writer.write("# instructions")
		for (step in this.instructions) {
			writer.write(step)
			writer.newLine()
		}
		writer.newLine()

	}

	private fun serializeField(writer: BufferedWriter, prefix: String, value: String) {
		writer.write(prefix)
		writer.write(value)
		writer.newLine()
	}

	private fun deserialize(reader: BufferedReader) {
		this.deserializeHeader(reader)

		this.activeTime = this.deserializeField(reader, "activeTime|") ?: ""
		this.totalTime = this.deserializeField(reader, "totalTime|") ?: ""
		this.yield = this.deserializeField(reader, "yield|") ?: ""
		this.description = this.deserializeField(reader, "description|") ?: ""

		// Skip line between header and body
		reader.readLine()

		val ingredientList = this.deserializeIngredients(reader)
		if (ingredientList == null) {
			Log.e("Recipe", "Failed to fully deserialize recipe, invalid ingredients.")
			return
		}
		this.ingredients = ingredientList

		// Since `deserializeIngredients` reads the line after the block as well, we can immediately read the instructions

		// TODO: instructions dont handle categories
		val steps = this.deserializeInstructions(reader)
		if (steps == null) {
			Log.e("Recipe", "Failed to fully deserialize recipe, invalid instructions.")
			return
		}
		this.instructions = steps

		this.bIsDirty = false
	}

	private fun deserializeHeader(reader: BufferedReader) {
		this.name = reader.readLine()
		this.source = this.deserializeField(reader, "source|") ?: ""
		this.url = this.deserializeField(reader, "url|") ?: ""
		this.aliases = (this.deserializeField(reader, "aliases|") ?: "").split(',')
		this.tags = (this.deserializeField(reader, "tags|") ?: "").split(',')
	}

	private fun deserializeField(reader: BufferedReader, prefix: String): String? {
		//if (reader.skip(prefix.length.toLong()) != prefix.length.toLong()) return ""
		return reader.readLine()
	}

	private fun deserializeIngredients(reader: BufferedReader): List<Ingredient>? {
		val header = reader.readLine()
		if (header != "# ingredients") return null

		val ingredients: MutableList<Ingredient> = mutableListOf()
		var ingredientString: String
		var category: String? = null
		val categoryRegex = Regex("\\[(.*)]")
		do {
			ingredientString = reader.readLine()
			if (ingredientString.isNotEmpty()) {
				val categoryMatch = categoryRegex.matchEntire(ingredientString)
				if (categoryMatch != null) {
					category = categoryMatch.destructured.component1()
					continue
				}
				val ingredientEntries = ingredientString.split('|')
				// TODO: Could error if file is improperly formatted (if there are < 3 delimiters)
				ingredients.add(
					Ingredient(
						ingredientEntries[0],
						ingredientEntries[1],
						ingredientEntries[2],
						category
					)
				)
			}
		} while (ingredientString.isNotEmpty())

		return ingredients
	}

	private fun deserializeInstructions(reader: BufferedReader): List<String>? {
		val header = reader.readLine()
		if (header != "# instructions") return null
		return reader.readLines()
	}

}