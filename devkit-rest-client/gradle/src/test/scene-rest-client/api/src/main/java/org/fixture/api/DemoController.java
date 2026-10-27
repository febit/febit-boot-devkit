package org.fixture.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", required = false) String name) {
        return "Hello " + name;
    }

    @GetMapping("/items/{id}")
    public String item(
            @PathVariable("id") String id,
            @RequestParam("verbose") boolean verbose,
            @RequestHeader(value = "X-Token", required = false) String token
    ) {
        return id + verbose + token;
    }

    @Deprecated
    @GetMapping("/legacy")
    public String legacy(@Deprecated @RequestParam(value = "old", required = false) String old) {
        return old;
    }
}
