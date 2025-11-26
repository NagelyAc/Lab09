package com.example.lab09

import com.google.gson.annotations.SerializedName

data class ReactionsModel(
    @SerializedName("likes") val likes: Int,
    @SerializedName("dislikes") val dislikes: Int
)