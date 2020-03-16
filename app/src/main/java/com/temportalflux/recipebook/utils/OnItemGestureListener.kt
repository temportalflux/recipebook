package com.temportalflux.recipebook.utils

import android.view.View

interface OnItemGestureListener {
	fun onItemPress(view: View, position: Int)
	fun onLongItemPress(view: View, position: Int)
}