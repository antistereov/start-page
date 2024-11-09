package io.github.antistereov.start.widget.shared.exception

import io.github.antistereov.start.global.exception.StartPageException

open class WidgetException(message: String? = null, cause: Throwable? = null) : StartPageException(message, cause)