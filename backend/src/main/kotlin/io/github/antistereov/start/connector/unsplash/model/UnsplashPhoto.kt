package io.github.antistereov.start.connector.unsplash.model

data class UnsplashPhoto(
    val id: String,
    val urls: Urls,
    val width: Int,
    val height: Int,
) {
    data class Urls(
        val raw: String,
        val full: String,
        val regular: String,
        val small: String,
        val thumb: String,
    )
}