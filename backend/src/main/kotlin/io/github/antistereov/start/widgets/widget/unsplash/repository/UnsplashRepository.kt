package io.github.antistereov.start.widgets.widget.unsplash.repository

import io.github.antistereov.start.widgets.widget.unsplash.model.UnsplashWidget
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UnsplashRepository : ReactiveMongoRepository<UnsplashWidget, Long>