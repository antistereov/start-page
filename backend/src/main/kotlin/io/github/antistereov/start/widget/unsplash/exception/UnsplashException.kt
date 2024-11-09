package io.github.antistereov.start.widget.unsplash.exception

import io.github.antistereov.start.widget.shared.exception.WidgetException

open class UnsplashException(message: String?, cause: Throwable? = null) : WidgetException(message, cause)