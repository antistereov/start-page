package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.widget.calendar.model.CalendarWidget
import io.github.antistereov.start.widgets.widget.chat.model.ChatWidget
import io.github.antistereov.start.widgets.widget.unsplash.model.UnsplashWidget

data class Widgets(
    var calendar: CalendarWidget = CalendarWidget(),
    var chat: ChatWidget = ChatWidget(),
    var unsplash: UnsplashWidget = UnsplashWidget(),
)