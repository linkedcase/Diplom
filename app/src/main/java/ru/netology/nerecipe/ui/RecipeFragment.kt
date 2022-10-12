package ru.netology.nerecipe.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapters.ingredients.IngredientsAdapter
import ru.netology.nerecipe.adapters.steps.StepsAdapter
import ru.netology.nerecipe.databinding.FragmentRecipeBinding
import ru.netology.nerecipe.dto.Categories
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.viewModel.RecipeViewModel


class RecipeFragment : Fragment() {
    private val args by navArgs<RecipeFragmentArgs>()

    private val viewModel by activityViewModels<RecipeViewModel>()

    private lateinit var recipe: Recipe

    // region Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipe_options_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editRecipeOption -> {
                viewModel.onEditMenuOptionClicked(args.recipeId)
                viewModel.navigateToEditRecipeScreenEvent.observe(this) {
                    val direction = RecipesFeedFragmentDirections.toEditRecipeFragment()
                    findNavController().navigate(direction)
                }
                true
            }
            R.id.deleteRecipeOption -> {
                findNavController().popBackStack()
                viewModel.onDeleteMenuOptionClicked(args.recipeId)
                true
            }
            else -> false
        }
    }
    // endregion Menu


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentRecipeBinding.inflate(layoutInflater, container, false).also { binding ->
            with(binding) {

                setHasOptionsMenu(true)

                viewModel.data.value?.let { listOfRecipes ->
                    recipe = listOfRecipes.first { recipe -> recipe.id == args.recipeId }
                    render(recipe)
                }

                viewModel.data.observe(viewLifecycleOwner) { listOfRecipes ->
                    if (!listOfRecipes.any { recipe -> recipe.id == args.recipeId }) {
                        return@observe
                    }
                    if (listOfRecipes.isNullOrEmpty()) {
                        return@observe
                    }
                    recipe = listOfRecipes.first { recipe -> recipe.id == args.recipeId }
                    render(recipe)
                }

                setTag(recipeChipGroup.context, recipe.tags, binding)

                faveRecipeMaterialButton.setOnClickListener {
                    viewModel.onFaveButtonClicked(recipe)
                }

            }
        }.root
    }

    private fun FragmentRecipeBinding.render(recipe: Recipe) {
        recipeNameTextView.text = recipe.title
        recipeImageView.setImageURI(Uri.parse(recipe.recipeImgPath))
        recipeTimeTextView.text = recipe.time.recipeFormat()

        faveRecipeMaterialButton.isChecked = recipe.isFave

        val ingredientsAdapter = IngredientsAdapter(recipe, CALLER_RECIPE, viewModel)
        recipeIngredientsList.adapter = ingredientsAdapter
        ingredientsAdapter.submitList(recipe.ingredients)

        val stepsAdapter = StepsAdapter(recipe, CALLER_RECIPE, viewModel)
        recipeStepsList.adapter = stepsAdapter
        stepsAdapter.submitList(recipe.steps.keys.toList())
    }

    private fun Int.recipeFormat(): String {
        val minutes = this
        val hours = this / 60
        val hoursMinutes = this % 60

        if (minutes <= 0) {
            return resources.getString(R.string.time_format_instantly)
        }

        val hoursText = when {
            hours == 0 -> ""
            hours == 1 || hours % 20 == 1 -> "$hours " + resources.getString(R.string.time_format_hour)
            hours == 2 || hours % 20 == 2 ||
                    hours == 3 || hours % 20 == 3 ||
                    hours == 4 || hours % 20 == 4 -> "$hours " + resources.getString(R.string.time_format_hour_a)
            else -> "$hours" + R.string.time_format_hours.toString()
        }
        val minutesText = when {
            hoursMinutes == 0 -> ""
            hoursMinutes in 11..20 -> "$hoursMinutes " + resources.getString(R.string.time_format_minutes)
            hoursMinutes == 1 || hoursMinutes % 10 == 1 -> "$hoursMinutes " + resources.getString(R.string.time_format_minute)
            hoursMinutes == 2 || hoursMinutes % 10 == 2 ||
                    hoursMinutes == 3 || hoursMinutes % 10 == 3 ||
                    hoursMinutes == 4 || hoursMinutes % 10 == 4 -> "$hoursMinutes " + resources.getString(
                R.string.time_format_minute_a
            )
            else -> "$hoursMinutes " + resources.getString(R.string.time_format_minutes)
        }

        return "$hoursText $minutesText"
    }

    private fun setTag(
        context: Context,
        categories: MutableSet<Categories>,
        binding: FragmentRecipeBinding
    ) {
        val chipGroup = binding.recipeChipGroup
        categories.forEach { category ->
            val tagName = category.categoryName
            val chip = Chip(context)
            chip.text = tagName
            chipGroup.addView(chip)
        }
    }

    companion object {
        const val CALLER_RECIPE = "Caller: recipe"
    }
}

