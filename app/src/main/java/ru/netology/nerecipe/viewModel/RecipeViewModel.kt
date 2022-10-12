package ru.netology.nerecipe.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.adapters.filters.FilterInteractionListener
import ru.netology.nerecipe.adapters.ingredients.IngredientInteractionListener
import ru.netology.nerecipe.adapters.steps.StepInteractionListener
import ru.netology.nerecipe.adapters.recipe.RecipeInteractionListener
import ru.netology.nerecipe.data.impl.RecipeRepositoryImpl
import ru.netology.nerecipe.db.AppDb
import ru.netology.nerecipe.dto.Categories
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.ui.EditRecipeFragment
import ru.netology.nerecipe.ui.NewRecipeFragment
import ru.netology.nerecipe.utils.SingleLiveEvent

class RecipeViewModel(application: Application) :
    RecipeInteractionListener,
    StepInteractionListener,
    IngredientInteractionListener,
    FilterInteractionListener,
    AndroidViewModel(application) {

    private val repository = RecipeRepositoryImpl(
        dao = AppDb.getInstance(
            context = application
        ).recipeDao
    )

    val data by repository::data

    // Navigation
    val navigateToNewRecipeScreenEvent = SingleLiveEvent<Recipe>()
    val navigateToRecipeScreenEvent = SingleLiveEvent<Recipe>()
    val navigateToEditRecipeScreenEvent = SingleLiveEvent<Boolean>()
    val navigateToFaveRecipesScreenEvent = SingleLiveEvent<Boolean>()
    val navigateToAllRecipesScreenEvent = SingleLiveEvent<Boolean>()

    // New recipe
    val newRecipe = MutableLiveData<Recipe>()
    val newRecipeImg = MutableLiveData<String>()
    val newStepImg = MutableLiveData<String>()

    // Edit recipe
    val editingRecipe = MutableLiveData<Recipe>()
    val editRecipeImg = MutableLiveData<String>()
    val editStepImg = MutableLiveData<String>()

    // Filter
    val listOfFilters = MutableLiveData<MutableList<Categories>>(mutableListOf())
    val filteredRecipes = MutableLiveData<List<Recipe>>(mutableListOf())
    val filterRecipes = SingleLiveEvent<Boolean>()
    override val filterCheckboxUpdate = MutableLiveData<Boolean>(false)


    fun onCreateRecipeSaveButtonClicked(recipe: Recipe) {
        repository.create(recipe)
    }

    fun onEditRecipeSaveButtonClicked(recipe: Recipe) {
        repository.update(recipe)
    }

    fun onAddButtonClicked() {
        newRecipe.value = Recipe(
            id = 0,
            title = "",
            recipeImgPath = "",
            time = 0,
            ingredients = mutableListOf(),
            steps = mutableMapOf(),
            tags = mutableSetOf()
        )
        newRecipeImg.value = ""
        newStepImg.value = ""

        navigateToNewRecipeScreenEvent.call()
    }

    fun onApplyFiltersButtonClicked() {
        filteredRecipes.value = data.value
        val recipes = if (!listOfFilters.value.isNullOrEmpty()) {
            filteredRecipes.value?.filter { recipe ->
                recipe.tags.any { category ->
                    category in listOfFilters.value?.toList() ?: mutableListOf()
                }
            }
        } else {
            data.value
        }

        filteredRecipes.value = recipes ?: data.value
        filterRecipes.call()
    }

    fun onResetFiltersButtonClicked() {
        filteredRecipes.value = data.value
        listOfFilters.value = mutableListOf()
        filterRecipes.call()
    }


    // region RecipeInteractionAdapter
    override fun onFaveButtonClicked(recipe: Recipe) {
        repository.fave(recipe.id)
    }

    override fun onDeleteMenuOptionClicked(recipeId: Long) {
        repository.delete(recipeId)
    }

    override fun onEditMenuOptionClicked(recipeId: Long) {
        editingRecipe.value = repository.getById(recipeId)
        navigateToEditRecipeScreenEvent.value = true
    }

    override fun onRecipeClicked(recipe: Recipe) {
        navigateToRecipeScreenEvent.value = recipe
    }
    // endregion RecipeInteractionAdapter

    // region StepsInteractionAdapter
    override fun onDeleteStepButtonClicked(recipe: Recipe, stepKey: String, caller: String) {
        val newRecipeSteps = recipe.steps
            .filterNot { step ->
                step.key == stepKey
            }
            .toMutableMap()

        if (caller == NewRecipeFragment.CALLER_NEW_RECIPE) {
            newRecipe.value = recipe.copy(steps = newRecipeSteps)
        } else if (caller == EditRecipeFragment.CALLER_EDIT_RECIPE) {
            editingRecipe.value = recipe.copy(steps = newRecipeSteps)
        }
    }
    // endregion StepsInteractionAdapter

    // region IngredientsInteractionAdapter
    override fun onDeleteIngredientButtonClicked(
        recipe: Recipe,
        ingredient: String,
        caller: String
    ) {
        val newRecipeIngredients = recipe.ingredients.filterNot { it == ingredient }.toMutableList()

        if (caller == NewRecipeFragment.CALLER_NEW_RECIPE) {
            newRecipe.value = recipe.copy(ingredients = newRecipeIngredients)
        } else if (caller == EditRecipeFragment.CALLER_EDIT_RECIPE) {
            editingRecipe.value = recipe.copy(ingredients = newRecipeIngredients)
        }
    }
    // endregion IngredientsInteractionAdapter

    // region FilterInteractionAdapter
    override fun onCheckClicked(category: Categories) {
        val filters = listOfFilters.value
        filters?.add(category)
        listOfFilters.value = filters ?: mutableListOf()
    }

    override fun onUncheckClicked(category: Categories) {
        val filters = listOfFilters.value
        filters?.remove(category)
        listOfFilters.value = filters ?: mutableListOf()
    }
    // endregion FilterInteractionAdapter

}
