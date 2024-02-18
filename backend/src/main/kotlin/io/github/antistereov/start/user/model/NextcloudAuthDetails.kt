package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar

data class NextcloudAuthDetails(
    var host: String? = null,
    var username: String? = null,
    var password: String? = null,
    var calendars: MutableList<NextcloudCalendar> = mutableListOf(),
)