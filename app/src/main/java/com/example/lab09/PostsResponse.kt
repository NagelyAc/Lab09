package com.example.lab09

import com.google.gson.annotations.SerializedName

// Contenedor que maneja la respuesta completa de dummyjson/posts
data class PostsResponse(
    @SerializedName("posts") val posts: List<PostModel>,
    @SerializedName("total") val total: Int,
    @SerializedName("skip") val skip: Int,
    @SerializedName("limit") val limit: Int
)