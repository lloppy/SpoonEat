package com.example.movieretrofit.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.movieretrofit.Firebase
import com.example.movieretrofit.R
import com.example.movieretrofit.data.Food
import com.example.movieretrofit.data.Nutrients
import com.example.movieretrofit.interfaces.AddFoodListener
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlin.math.roundToInt

class FoodAdapter(private val addFoodListener: AddFoodListener) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    var foodList = emptyList<Food>()

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.foods_list, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]

        Picasso.get()
            .load(food.image.toUri())
            .transform(RoundedCornersTransformation(10, 0))
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.itemView.findViewById<ImageView>(R.id.food_image))

        initNutrientsTv(holder, food)

        holder.itemView.findViewById<Button>(R.id.button_add_food).setOnClickListener {
            val edGrams = holder.itemView.findViewById<EditText>(R.id.ed_gram).text
            val grams: Float = if (edGrams.isEmpty()) 1f else {
                edGrams.toString().toFloat() / 100f
            }
            food.realNutrients.grams = grams
            Log.e("item", " in Food adapter grams is $grams")

            addFoodListener.onFoodReceived(food)

            val firebase = Firebase()
            firebase.sendCurrentMealDataToFirebase(food)
            Log.e("edamam", " in Food adapter food to FB is ${food}")
            Log.e("edamam", " in Food adapter food -fat- to FB is ${food.realNutrients.fat}")
        }
    }

    private fun initNutrientsTv(holder: FoodViewHolder, food: Food) {
        food.realNutrients = Nutrients(food.nutrients)
        val protein = "${food.realNutrients.protein.roundToInt()} г"
        val fat = "${food.realNutrients.fat.roundToInt()} г"
        val carbs = "${food.realNutrients.carb.roundToInt()} г"

        holder.itemView.findViewById<TextView>(R.id.food_protein).text = protein
        holder.itemView.findViewById<TextView>(R.id.food_fat).text = fat
        holder.itemView.findViewById<TextView>(R.id.food_carbs).text = carbs
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    fun setData(foods: List<Food>) {
        this.foodList = foods
        notifyDataSetChanged()
    }
}