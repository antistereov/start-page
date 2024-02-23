package io.github.antistereov.start.widgets.widget.unsplash.model

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "unsplash")
data class UnsplashWidget(
    val id: Long,
    var recentPhotos: MutableList<Photo> = mutableListOf()
)