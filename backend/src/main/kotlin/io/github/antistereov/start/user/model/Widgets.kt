package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.widget.unsplash.model.UnsplashWidget

data class Widgets(
    var calDavId: String? = null,
    var chatId: String? = null,
    var unsplash: UnsplashWidget = UnsplashWidget(),
)