package com.temportalflux.recipebook.data

import android.os.Parcel
import android.os.Parcelable

class IngredientCategory(
	val label: String?,
	val ingredients: List<Ingredient>
) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString(),
		parcel.createTypedArray(Ingredient.CREATOR)?.toList() ?: listOf()
	)

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(this.label)
		dest.writeTypedArray(this.ingredients.toTypedArray(), 0)
	}

	override fun describeContents(): Int = 0

	companion object CREATOR : Parcelable.Creator<IngredientCategory> {
		override fun createFromParcel(parcel: Parcel): IngredientCategory {
			return IngredientCategory(parcel)
		}

		override fun newArray(size: Int): Array<IngredientCategory?> {
			return arrayOfNulls(size)
		}
	}

}