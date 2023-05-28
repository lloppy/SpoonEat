package com.example.movieretrofit.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieretrofit.adapter.FoodAdapter
import com.example.movieretrofit.adapter.FoodTextInputEditTextAdapter
import com.example.movieretrofit.data.Food
import com.example.movieretrofit.databinding.FragmentFoodListBinding
import com.example.movieretrofit.fragments.ui.SearchFragment
import com.example.movieretrofit.interfaces.AddFoodListener
import com.example.movieretrofit.interfaces.FoodClickListener
import com.example.movieretrofit.model.FoodViewModel
import com.example.retrofittraining.Utils.inputCheck
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class FoodListFragment : Fragment(), FoodClickListener, AddFoodListener {
    lateinit var foodViewModel: FoodViewModel
    var binding: FragmentFoodListBinding? = null

    val adapterTextInput = FoodTextInputEditTextAdapter(this)
    private val adapter = FoodAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFoodListBinding.inflate(inflater, container, false)
        this.binding = binding

        //Adapter for food
        val recyclerView = binding.recyclerFood
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //Adapter for foodTextInputList
        val recyclerViewTextInput = binding.recyclerwatchlist
        recyclerViewTextInput.adapter = adapterTextInput
        recyclerViewTextInput.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.tvFood.addTextChangedListener(simpleTextWatcher)

        //ViewModel
        foodViewModel = ViewModelProvider(this)[FoodViewModel::class.java]

        binding.textInputLayout.setEndIconOnClickListener {
            lifecycleScope.launch {

                binding.progressBar.visibility = View.VISIBLE

                val tvFoodEditText = binding.tvFood.text.toString()
                if (inputCheck(tvFoodEditText)) {
                    val recipes = foodViewModel.getRecipes(tvFoodEditText)
                    Log.d("FOOD", "$recipes")
                    adapter.setData(recipes)
                    binding.recyclerFood.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE

                    if (recipes.isNotEmpty()) {
                        binding.recyclerFood.visibility = View.VISIBLE
                        binding.listsearch.visibility = View.GONE
                        binding.errorlist.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                    } else binding.errorlist.visibility = View.VISIBLE
                    Log.d("FOOD", "$recipes")
                    adapter.setData(recipes)
                } else {
                    binding.errorlist.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerFood.visibility = View.GONE
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    // TextWatcher
    private val simpleTextWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding!!.listsearch.visibility = View.VISIBLE
        }

        override fun afterTextChanged(s: Editable?) {
            lifecycleScope.launch {
                flow {
                    try {
                        val text = s.toString()
                        delay(100)
                        if (adapterTextInput.foodList.isEmpty()) delay(300)
                        binding!!.progressBar.visibility = View.VISIBLE
                        val food = foodViewModel.getRecipes(text)
                        emit(adapterTextInput.setData(food.takeLast(food.size - 1)))
                        binding!!.progressBar.visibility = View.GONE
                        Log.d("textWatcher", text)
                        if (s!!.isEmpty()) binding!!.listsearch.visibility = View.GONE
                    } catch (_: Exception) {
                    }
                }.collect()
            }
        }
    }

    // Recycler onItemClick
    override fun onFoodClickListener(food: String) {
        lifecycleScope.launch {
            val tvFoodEditText = binding!!.tvFood.text.toString()
            if (inputCheck(tvFoodEditText)) {
                binding!!.tvFood.setText(food)
                val recipes = foodViewModel.getRecipes(food)
                val selectedRecipe = recipes.take(1)

                adapter.setData(selectedRecipe)
                binding!!.recyclerFood.visibility = View.VISIBLE
                binding!!.progressBar.visibility = View.GONE
                binding!!.listsearch.visibility = View.GONE

                if (recipes.isNotEmpty()) {
                    binding!!.recyclerFood.visibility = View.VISIBLE
                    binding!!.progressBar.visibility = View.GONE
                } else binding!!.errorlist.visibility = View.VISIBLE
                Log.d("FOOD", "$recipes")
                adapter.setData(selectedRecipe)
            } else {
                binding!!.errorlist.visibility = View.VISIBLE
                binding!!.progressBar.visibility = View.GONE
                binding!!.recyclerFood.visibility = View.GONE
            }
        }
    }

    override fun onFoodReceived(food: Food) {
        val searchFragment = parentFragment?.requireParentFragment() as SearchFragment
        searchFragment.handleNutrientsData(food)
    }
}