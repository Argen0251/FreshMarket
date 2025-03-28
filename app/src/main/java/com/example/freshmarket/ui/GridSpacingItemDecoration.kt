package com.example.freshmarket.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class GridSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // позиция элемента
        val column = position % 2 // для 2 столбцов

        if (column == 0) { // левый столбец
            outRect.left = 0
            outRect.right = spacing / 2
        } else { // правый столбец
            outRect.left = spacing / 2
            outRect.right = 0
        }
        // Если нужен отступ сверху (например, для всех элементов) – можно добавить:
        outRect.top = spacing
        // Если отступ снизу нужен – можно добавить:
        outRect.bottom = spacing
    }
}