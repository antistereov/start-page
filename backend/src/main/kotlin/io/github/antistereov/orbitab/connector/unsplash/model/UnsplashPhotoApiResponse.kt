package io.github.antistereov.orbitab.connector.unsplash.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.antistereov.orbitab.connector.unsplash.exception.UnsplashInvalidParameterException

data class UnsplashPhotoApiResponse(
    val id: String,
    val urls: Urls,
    @JsonProperty("liked_by_user")
    val likedByUser: Boolean,
    val user: User,
    val color: String,
    val links: Links,
    val width: Int,
    val height: Int,
) {
    data class Urls(
        val raw: String,
    )

    data class Links(
        val html: String,
    )

    data class User(
        val name: String,
        val links: Links,
    ) {
        data class Links(
            val html: String
        )
    }

    fun toUnsplashPhoto(screenWidth: Int, screenHeight: Int, quality: Int): UnsplashPhoto {
        if (quality <= 0 || quality > 100) {
            throw UnsplashInvalidParameterException("Invalid quality parameter $quality " +
                    "- it must be a value between 0 and 100")
        }

        val width = calculateMinimumPictureWidth(
            pictureWidth = this.width,
            pictureHeight = this.height,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )
        return UnsplashPhoto(
            id = id,
            url = "${urls.raw}?q=${quality}&w=${width}",
            link = links.html,
            user = UnsplashPhoto.User(name = user.name, link = user.links.html),
            color = color,
            likedByUser = likedByUser,
        )
    }

    private fun calculateMinimumPictureWidth(pictureWidth: Int, pictureHeight: Int, screenWidth: Int, screenHeight: Int): Int {

        val screenAspectRatio = screenWidth.toDouble() / screenHeight
        val pictureAspectRatio = pictureWidth.toDouble() / pictureHeight

        return if (screenAspectRatio > pictureAspectRatio) {
            screenWidth
        } else {
            (screenHeight * pictureAspectRatio).toInt()
        }
    }
}


