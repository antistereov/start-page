package io.github.antistereov.start.widgets.widget.unsplash.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "unsplash")
data class UnsplashWidget(
    @Id var id: String? = null,
    var recentPhotos: MutableList<Photo> = mutableListOf()
)