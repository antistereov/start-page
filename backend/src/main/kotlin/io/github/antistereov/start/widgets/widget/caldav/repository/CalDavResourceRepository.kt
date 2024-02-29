package io.github.antistereov.start.widgets.widget.caldav.repository

import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface CalDavResourceRepository : ReactiveMongoRepository<CalDavResource, String>