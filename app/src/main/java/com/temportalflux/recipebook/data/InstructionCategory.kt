package com.temportalflux.recipebook.data

import android.os.Parcel
import android.os.Parcelable

class InstructionCategory(
	val label: String?,
	private val instructions: MutableList<String>
) : Parcelable {

	fun getInstructions():List<String> = this.instructions.toList()

	constructor(parcel: Parcel) : this(parcel.readString(), mutableListOf())
	{
		parcel.readStringList(this.instructions)
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(this.label)
		dest.writeStringArray(this.instructions.toTypedArray())
	}

	override fun describeContents(): Int = 0

	companion object CREATOR : Parcelable.Creator<InstructionCategory> {

		override fun createFromParcel(parcel: Parcel): InstructionCategory {
			return InstructionCategory(parcel)
		}

		override fun newArray(size: Int): Array<InstructionCategory?> {
			return arrayOfNulls(size)
		}
	}

}