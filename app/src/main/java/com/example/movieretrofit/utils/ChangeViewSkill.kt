package com.example.movieretrofit.utils

import android.content.Context
import android.util.Log
import com.example.movieretrofit.Firebase
import com.example.movieretrofit.data.Food
import com.example.movieretrofit.data.Nutrients
import com.example.movieretrofit.model.restFoodApi
import com.justai.aimybox.Aimybox
import com.justai.aimybox.api.aimybox.AimyboxRequest
import com.justai.aimybox.api.aimybox.AimyboxResponse
import com.justai.aimybox.core.CustomSkill
import com.justai.aimybox.model.Response

class ChangeViewSkill(private val context: Context) : CustomSkill<AimyboxRequest, AimyboxResponse> {

    lateinit var queryRequest: String
    private lateinit var firebase: Firebase
   // lateinit var foodViewModel: FoodViewModel

    override fun canHandle(response: AimyboxResponse) = response.action == "changeView"

    override suspend fun onRequest(request: AimyboxRequest, aimybox: Aimybox): AimyboxRequest {
        queryRequest = request.query

       // foodViewModel = ViewModelProvider(context)[FoodViewModel::class.java]

        Log.e("aimybox", "request query is $queryRequest")
        val foodApiService = restFoodApi
        val allFood = foodApiService.getAllFood(query = queryRequest)

        if (allFood.isSuccessful && allFood.body()!!.searchResults[0].results.isNotEmpty()) {
            val nameFood = allFood.body()!!.searchResults[0].results[0].name
            val imageFood = allFood.body()!!.searchResults[0].results[0].image
            val contentFood = allFood.body()!!.searchResults[0].results[0].content
            val id = allFood.body()!!.searchResults[0].results[0].id
        //    val nutrientsFood = Nutrients().updateWithNutrition(foodViewModel.getRecipeNutrients(id!!))

            val food = Food(nameFood, imageFood, contentFood, id, Nutrients())

            sendToFirebase(food)
        } else {
            Log.e("aimybox", "foodApiService error")
        }
        return request
    }

    private fun sendToFirebase(food: Food) {
        firebase = Firebase()
        firebase.sendCurrentMealDataToFirebase(food)
        //HomeFragment().updateNutrients()
    }

    override suspend fun onResponse(
        response: AimyboxResponse,
        aimybox: Aimybox,
        defaultHandler: suspend (Response) -> Unit
    ) {

        Log.e("aimybox", "onResponse query request is $queryRequest")
    }
}