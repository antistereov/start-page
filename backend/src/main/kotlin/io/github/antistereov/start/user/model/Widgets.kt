package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.widget.caldav.model.CalDavWidgetData
import io.github.antistereov.start.widgets.widget.transport.model.PublicTransportWidgetData
import io.github.antistereov.start.widgets.widget.unsplash.model.UnsplashWidgetData
import io.github.antistereov.start.widgets.widget.weather.model.WeatherWidgetData

data class Widgets(
    var calDav: CalDavWidgetData = CalDavWidgetData(),
    var chatId: String? = null,
    var unsplash: UnsplashWidgetData = UnsplashWidgetData(),
    var weather: WeatherWidgetData = WeatherWidgetData(),
    var publicTransport: PublicTransportWidgetData = PublicTransportWidgetData(),
)