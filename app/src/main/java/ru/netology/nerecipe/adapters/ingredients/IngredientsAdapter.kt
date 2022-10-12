package ru.netology.nerecipe.adapters.ingredients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.databinding.IngredientBinding
import ru.netology.nerecipe.dto.Recipe
import ru.netology.nerecipe.ui.EditRecipeFragment
import ru.netology.nerecipe.ui.NewRecipeFragment
import ru.netology.nerecipe.ui.RecipeFragment


class IngredientsAdapter(

    private val recipe: Recipe,
    private val ingredientsFragment: String,
    private val interactionListener: IngredientInteractionListener

) : ListAdapter<String, IngredientsAdapter.IngredientViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IngredientBinding.inflate(inflater, parent, false)
        return IngredientViewHolder(binding, ingredientsFragment, interactionListener)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position), recipe)
    }


    class IngredientViewHolder(
        private val binding: IngredientBinding,
        private val ingredientsFragment: String,
        private val interactionListener: IngredientInteractionListener

    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var ingredient: String
        private lateinit var recipe: Recipe
        private val caller = when (ingredientsFragment) {
            NewRecipeFragment.CALLER_NEW_RECIPE -> NewRecipeFragment.CALLER_NEW_RECIPE
            EditRecipeFragment.CALLER_EDIT_RECIPE -> EditRecipeFragment.CALLER_EDIT_RECIPE
            else -> RecipeFragment.CALLER_RECIPE
        }

        init {
            binding.deleteIngredientMaterialButton.setOnClickListener {
                interactionListener.onDeleteIngredientButtonClicked(recipe, ingredient, caller)
            }
        }

        fun bind(ingredient: String, recipe: Recipe) {
            this.recipe = recipe
            this.ingredient = ingredient
            with(binding) {
                ingredientTextView.text = ingredient
                deleteIngredientMaterialButton.visibility = View.GONE
                if (ingredientsFragment != RecipeFragment.CALLER_RECIPE) {
                    deleteIngredientMaterialButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return newItem == oldItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return newItem == oldItem
        }
    }

}