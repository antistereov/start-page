package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.widget.caldav.model.CalDavWidget
import io.github.antistereov.start.widgets.widget.chat.model.ChatWidget
import io.github.antistereov.start.widgets.widget.unsplash.model.UnsplashWidget

data class Widgets(
    var calDav: CalDavWidget = CalDavWidget(),
    var chat: ChatWidget = ChatWidget(),
    var unsplash: UnsplashWidget = UnsplashWidget(),
)