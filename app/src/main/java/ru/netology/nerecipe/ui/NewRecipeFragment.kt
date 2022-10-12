package ru.netology.nerecipe.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapters.ingredients.IngredientsAdapter
import ru.netology.nerecipe.adapters.steps.StepsAdapter
import ru.netology.nerecipe.databinding.FragmentRecipeNewBinding
import ru.netology.nerecipe.dto.Categories
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.viewModel.RecipeViewModel
import ru.netology.nerecipe.utils.hideKeyboard


class NewRecipeFragment : Fragment() {

    private val Fragment.packageManager
        get() = activity?.packageManager

    private val viewModel by activityViewModels<RecipeViewModel>()

    // region ResultLaunchers
    private val pickStepImgActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data!!
                requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.newStepImg.value = uri.toString()
            }
        }

    private val pickRecipeImgActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data!!
                requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.newRecipeImg.value = uri.toString()
            }
        }
    // endregion ResultLaunchers


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentRecipeNewBinding.inflate(layoutInflater, container, false).also { binding ->
            with(binding) {

                val currentNewRecipe = MutableLiveData(
                    Recipe(
                        id = 0,
                        title = "",
                        recipeImgPath = "",
                        time = 0,
                        ingredients = mutableListOf(),
                        steps = mutableMapOf(),
                        tags = mutableSetOf()
                    )
                )

                currentNewRecipe.observe(viewLifecycleOwner) { recipe ->
                    render(recipe)
                }

                // region Categories
                val categories = mutableSetOf<Categories>()

                val popupMenu by lazy {
                    PopupMenu(context, binding.addCategoryImageButton).apply {
                        inflate(R.menu.recipe_categories_menu)
                        for (i in 0 until Categories.values().size) {
                            menu.add(0, i, 0, Categories.values()[i].categoryName)
                        }
                        setOnMenuItemClickListener { option ->
                            for (i in 0 until Categories.values().size) {
                                if (option.itemId == i) {
                                    categories.add(Categories.values()[i])
                                    currentNewRecipe.value =
                                        currentNewRecipe.value?.copy(tags = categories)
                                    return@setOnMenuItemClickListener true
                                }
                            }
                            false
                        }
                    }
                }

                addCategoryImageButton.setOnClickListener {
                    popupMenu.show()
                }
                // endregion Categories

                // region Ingredients
                var ingredients = mutableListOf<String>()

                newRecipeAddIngredientButton.setOnClickListener {
                    if (!newRecipeIngredientsEditText.text.isNullOrEmpty()) {
                        ingredients.add(newRecipeIngredientsEditText.text.toString())
                        currentNewRecipe.value =
                            currentNewRecipe.value?.copy(ingredients = ingredients)
                        newRecipeIngredientsEditText.hideKeyboard()
                        newRecipeIngredientsEditText.text.clear()
                    }
                }

                viewModel.newRecipe.observe(viewLifecycleOwner) { recipe ->
                    ingredients = recipe.ingredients
                    currentNewRecipe.value =
                        currentNewRecipe.value?.copy(ingredients = recipe.ingredients)
                }
                // endregion Ingredients

                // region RecipeImage
                var recipeImgPath = ""

                viewModel.newRecipeImg.observe(viewLifecycleOwner) { path ->
                    recipeImgPath = path
                    currentNewRecipe.value =
                        currentNewRecipe.value?.copy(recipeImgPath = recipeImgPath)
                }

                newRecipeAddRecipeImageMaterialButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    }
                    val imgPickIntent = Intent.createChooser(intent, "Select Image from...")
                    pickRecipeImgActivityResultLauncher.launch(imgPickIntent)
                }
                // endregion RecipeImage

                // region Steps
                var steps = mutableMapOf<String, String>()
                var stepImgPath = ""

                newRecipeAddImageStepImageButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    }
                    val imgPickIntent = Intent.createChooser(intent, "Select Image from...")
                    pickStepImgActivityResultLauncher.launch(imgPickIntent)
                }

                viewModel.newStepImg.observe(viewLifecycleOwner) { path ->
                    stepImgPath = path
                }

                newRecipeAddStepImageButton.setOnClickListener {
                    if (!newRecipeStepsEditText.text.isNullOrEmpty()) {
                        steps[newRecipeStepsEditText.text.toString()] = stepImgPath.ifEmpty { "" }
                        currentNewRecipe.value =
                            currentNewRecipe.value?.copy(steps = steps)
                        stepImgPath = ""
                        newRecipeStepsEditText.hideKeyboard()
                        newRecipeStepsEditText.text.clear()
                    }
                }

                viewModel.newRecipe.observe(viewLifecycleOwner) { recipe ->
                    steps = recipe.steps
                    currentNewRecipe.value = currentNewRecipe.value?.copy(steps = recipe.steps)
                }
                // endregion RecipeImage


                //Save
                newRecipeSaveButton.setOnClickListener {
                    if (recipeImgPath.isEmpty()) {
                        recipeImgPath = DEFAULT_IMAGE_PATH
                        currentNewRecipe.value =
                            currentNewRecipe.value?.copy(recipeImgPath = recipeImgPath)
                    }
                    if (
                        !newRecipeNameEditText.text.isNullOrBlank() &&
                        !newRecipeTimeEditText.text.isNullOrBlank() &&
                        ingredients.isNotEmpty() &&
                        steps.isNotEmpty() &&
                        categories.isNotEmpty()
                    ) {
                        currentNewRecipe.value = currentNewRecipe.value?.copy(
                            title = newRecipeNameEditText.text.toString(),
                            time = newRecipeTimeEditText.text.toString().toInt()
                        )
                        currentNewRecipe.value?.let { recipe ->
                            viewModel.onCreateRecipeSaveButtonClicked(
                                recipe
                            )
                            findNavController().popBackStack()
                        }
                    } else if (newRecipeNameEditText.text.isNullOrBlank()) {
                        newRecipeNameEditText.requestFocus()
                        newRecipeNameEditText.error =
                            resources.getString(R.string.error_empty_name)
                    } else if (newRecipeTimeEditText.text.isNullOrBlank()) {
                        newRecipeTimeEditText.requestFocus()
                        newRecipeTimeEditText.error =
                            resources.getString(R.string.error_empty_time)
                    } else if (ingredients.isEmpty()) {
                        newRecipeIngredientsEditText.requestFocus()
                        newRecipeIngredientsEditText.error =
                            resources.getString(R.string.error_empty_ingredients)
                    } else if (steps.isEmpty()) {
                        newRecipeStepsEditText.requestFocus()
                        newRecipeStepsEditText.error =
                            resources.getString(R.string.error_empty_steps)
                    } else if (categories.isEmpty()) {
                        newRecipeCategoriesWordTextView.requestFocus()
                        newRecipeCategoriesWordTextView.error =
                            resources.getString(R.string.error_empty_categories)
                    }
                }
            }

        }.root
    }

    private fun FragmentRecipeNewBinding.render(recipe: Recipe) {

        newRecipeImageView.setImageURI(Uri.parse(recipe.recipeImgPath))

        setTags(newRecipeChipGroup.context, recipe.tags, this)

        val ingredientsAdapter = IngredientsAdapter(recipe, CALLER_NEW_RECIPE, viewModel)
        newRecipeIngredientsList.adapter = ingredientsAdapter
        ingredientsAdapter.submitList(recipe.ingredients)

        val stepsAdapter = StepsAdapter(recipe, CALLER_NEW_RECIPE, viewModel)
        newRecipeStepsList.adapter = stepsAdapter
        stepsAdapter.submitList(recipe.steps.keys.toList())
    }


    companion object {
        private const val DEFAULT_IMAGE_PATH =
            "android.resource://ru.netology.nerecipe/drawable/ic_launcher_foreground"
        const val CALLER_NEW_RECIPE = "Caller: newRecipe"
    }
}

private fun setTags(
    context: Context,
    categories: MutableSet<Categories>,
    binding: FragmentRecipeNewBinding
) {
    val chipGroup = binding.newRecipeChipGroup

    chipGroup.removeAllViews()

    categories.forEach { category ->
        val tagName = category.categoryName
        val chip = Chip(context)
        chip.text = tagName
        chip.isCloseIconVisible = true

        chip.setOnCloseIconClickListener {
            categories.remove(category)
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
    }
}




