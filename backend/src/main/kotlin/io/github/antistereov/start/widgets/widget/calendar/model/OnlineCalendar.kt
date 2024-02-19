package io.github.antistereov.start.widgets.widget.calendar.model


data class OnlineCalendar(
    val name: String,
    val color: String,
    val icsLink: String,
    val auth: CalendarAuth,
    val description: String?,
    var calendarEvents: List<CalendarEvent>,
)
