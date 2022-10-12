package ru.netology.nerecipe.dto

import kotlinx.serialization.Serializable
import ru.netology.nerecipe.MyApp
import ru.netology.nerecipe.R

@Serializable
data class Recipe(
    val id: Long,
    val title: String,
    val recipeImgPath: String,
    val time: Int,
    val ingredients: MutableList<String>,
    val steps: MutableMap<String, String>,
    val tags: MutableSet<Categories>,
    val isFave: Boolean = false
) {
}


enum class Categories(val categoryName: String) {
    EUROPEAN(MyApp.appContext.getString(R.string.category_European)),
    ASIAN(MyApp.appContext.getString(R.string.category_Asian)),
    PANASIAN(MyApp.appContext.getString(R.string.category_PanAsian)),
    ORIENTAL(MyApp.appContext.getString(R.string.category_Oriental)),
    AMERICAN(MyApp.appContext.getString(R.string.category_American)),
    RUSSIAN(MyApp.appContext.getString(R.string.category_Russian)),
    MEDITERRANEAN(MyApp.appContext.getString(R.string.category_Mediterranean));

    companion object {
        fun from(search: String): Categories {
            return requireNotNull(
                values().find { it.categoryName == search }
            ) { "No this category $search" }
        }
    }
}
