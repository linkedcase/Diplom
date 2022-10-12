package ru.netology.nerecipe.adapters.ingredients

import ru.netology.nerecipe.dto.Recipe

interface IngredientInteractionListener {

    fun onDeleteIngredientButtonClicked(recipe: Recipe, ingredient: String, caller: String)

}