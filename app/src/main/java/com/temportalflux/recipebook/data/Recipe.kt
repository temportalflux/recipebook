package com.temportalflux.recipebook.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.StringReader
import java.util.*

/*
private fun amountStringToNumber(amt: String): Float {
	// TODO: ?????
	// 1 - straight int
	// 1.4623 - straight float
	// 1 1/12 - mixed number
	return 0.0F
}
*/

class Recipe(private var name: String, private var bIsDirty: Boolean = false) : Parcelable {

	private var id: String? = null

	var source: String = ""
		private set(value) { field = value }
	var url: String = ""
		private set(value) { field = value }
	private var aliases: List<String> = listOf()
	private var tags: List<String> = listOf()

	var activeTime: String = ""
		private set(value) { field = value }
	var totalTime: String = ""
		private set(value) { field = value }
	var yield: String = ""
		private set(value) { field = value }

	var description: String = ""
		private set(value) { field = value }

	private var ingredients: List<IngredientCategory> = listOf()
	private var instructions: List<InstructionCategory> = listOf()

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

		this.ingredients = parcel.createTypedArray(IngredientCategory.CREATOR)?.toList() ?: listOf()
		this.instructions =
			parcel.createTypedArray(InstructionCategory.CREATOR)?.toList() ?: listOf()
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
		dest?.writeTypedArray(this.instructions.toTypedArray(), 0)
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

	fun getIngredients(): List<IngredientCategory> = this.ingredients

	fun getInstructions(): List<InstructionCategory> = this.instructions

	fun markDirty() {
		this.bIsDirty = true
	}

	fun isDirty(): Boolean = this.bIsDirty

	fun withId(id: String): Recipe {
		this.id = id
		return this
	}

	fun withContent(textFileContent: String): Recipe {
		BufferedReader(StringReader(textFileContent)).useLines {
			this.deserialize(it.iterator())
		}
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
		ctx.openFileInput(this.getFileName()).bufferedReader().useLines {
			this.deserialize(it.iterator())
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
		for (category in this.ingredients) {
			if (category.label != null) {
				writer.write("[${category.label}]")
				writer.newLine()
			}
			for (ingredient in category.ingredients) {
				writer.write("${ingredient.amount}|${ingredient.unit}|${ingredient.type}")
				writer.newLine()
			}
		}
		writer.newLine()

		// Instructions
		writer.write("# instructions")
		for (category in this.instructions) {
			if (category.label != null) {
				writer.write("[${category.label}]")
				writer.newLine()
			}
			for (instruction in category.getInstructions()) {
				writer.write(instruction)
				writer.newLine()
			}
		}
		writer.newLine()

	}

	private fun serializeField(writer: BufferedWriter, prefix: String, value: String) {
		writer.write(prefix)
		writer.write(value)
		writer.newLine()
	}

	private fun deserialize(reader: Iterator<String>) {
		this.deserializeHeader(reader)

		this.activeTime = this.deserializeField(reader, "activeTime|") ?: ""
		this.totalTime = this.deserializeField(reader, "totalTime|") ?: ""
		this.yield = this.deserializeField(reader, "yield|") ?: ""
		this.description = this.deserializeField(reader, "description|") ?: ""

		// Skip line between header and body
		reader.next()

		val ingredientList = this.deserializeList(
			reader, "# ingredients",
			{ line: String?, content: MutableList<Ingredient> ->
				IngredientCategory(
					line,
					content
				)
			},
			{ line: String ->
				val ingredientEntries = line.split('|')
				// TODO: Could error if file is improperly formatted (if there are < 3 delimiters)
				Ingredient(
					ingredientEntries[0],
					ingredientEntries[1],
					ingredientEntries[2]
				)
			}
		)
		if (ingredientList == null) {
			Log.e("Recipe", "Failed to fully deserialize recipe, invalid ingredients.")
			return
		}
		this.ingredients = ingredientList

		// Since `deserializeIngredients` reads the line after the block as well, we can immediately read the instructions

		val steps = this.deserializeList(
			reader, "# instructions",
			{ line: String?, content: MutableList<String> ->
				InstructionCategory(
					line,
					content
				)
			},
			{ line: String -> line }
		)
		if (steps == null) {
			Log.e("Recipe", "Failed to fully deserialize recipe, invalid instructions.")
			return
		}
		this.instructions = steps

		this.bIsDirty = false
	}

	private fun deserializeHeader(reader: Iterator<String>) {
		this.name = reader.next()
		this.source = this.deserializeField(reader, "source|") ?: ""
		this.url = this.deserializeField(reader, "url|") ?: ""
		this.aliases = (this.deserializeField(reader, "aliases|") ?: "").split(',')
		this.tags = (this.deserializeField(reader, "tags|") ?: "").split(',')
	}

	private fun deserializeField(reader: Iterator<String>, prefix: String): String? {
		return reader.next().substring(prefix.length)
	}

	private fun <TCategory, TItem> deserializeList(
		reader: Iterator<String>, expectedHeader: String,
		createCategory: (String?, MutableList<TItem>) -> TCategory,
		createItem: (String) -> TItem
	): List<TCategory>? {
		val header = reader.next()
		if (header != expectedHeader) return null

		val categories: MutableList<TCategory> = mutableListOf()
		var contentList: MutableList<TItem> = mutableListOf()

		var line: String?
		var category: String? = null
		val categoryRegex = Regex("\\[(.*)]")
		do {
			line = if (reader.hasNext()) reader.next() else null
			if (line != null && line.isNotEmpty()) {

				val categoryMatch = categoryRegex.matchEntire(line)
				if (categoryMatch != null) {
					// save off the previous ingredient category, assuming there are ingredients to save
					if (contentList.size > 0) {
						categories.add(createCategory(category, contentList))
						contentList = mutableListOf()
					}
					category = categoryMatch.destructured.component1()
					continue
				}

				contentList.add(createItem(line))
			}
		} while (line != null && line.isNotEmpty())
		// Save off the last remaining category, so long as there are ingredients to save
		if (contentList.size > 0) {
			categories.add(createCategory(category, contentList))
		}

		return categories
	}

}