package com.temportalflux.recipebook.data

import android.os.Parcel
import android.os.Parcelable

class Ingredient(
	val amount: String,
	val unit: String,
	val type: String
) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readString() ?: ""
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(this.amount)
		parcel.writeString(this.unit)
		parcel.writeString(this.type)
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