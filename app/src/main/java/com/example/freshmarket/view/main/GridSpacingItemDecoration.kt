package com.example.freshmarket.view.main

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % 2

        if (column == 0) {
            // Левый столбец
            outRect.left = 0
            outRect.right = spacing / 2
        } else {
            // Правый столбец
            outRect.left = spacing / 2
            outRect.right = 0
        }
        outRect.top = spacing
        outRect.bottom = spacing
    }
}
