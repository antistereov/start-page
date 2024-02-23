package io.github.antistereov.start.widgets.widget.caldav.repository

import io.github.antistereov.start.widgets.widget.caldav.model.CalDavWidget
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface CalDavRepository : ReactiveMongoRepository<CalDavWidget, Long>