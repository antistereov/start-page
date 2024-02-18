package io.github.antistereov.start.widgets.transport.controller

import io.github.antistereov.start.widgets.transport.service.DVBService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/widgets/transport/dvb")
class DVBController(
    private val dvbService: DVBService
) {
    //TODO: Implement DVBController
}