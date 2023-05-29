package com.example.movieretrofit.data

data class FoodList(
    val hints: List<FoodWithoutMeasures>,
    val text: String
)

data class FoodWithoutMeasures(
    val food: Food
)
