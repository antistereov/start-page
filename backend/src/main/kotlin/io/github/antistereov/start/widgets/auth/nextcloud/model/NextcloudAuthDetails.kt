package io.github.antistereov.start.widgets.auth.nextcloud.model

import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar

data class NextcloudAuthDetails(
    var host: String? = null,
    var username: String? = null,
    var password: String? = null,
    var calendars: MutableList<OnlineCalendar> = mutableListOf(),
)