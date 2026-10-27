package org.fixture.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
@RequestMapping("/legacy")
public class LegacyController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
