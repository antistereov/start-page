package io.github.antistereov.start.widget.spotify.exception

import io.github.antistereov.start.widget.shared.exception.WidgetException

open class SpotifyException(message: String, cause: Throwable? = null) : WidgetException(message, cause)