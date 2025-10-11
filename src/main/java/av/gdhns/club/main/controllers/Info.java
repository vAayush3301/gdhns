package av.gdhns.club.main.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Info {
    @GetMapping("/info")
    public String info() {
        return "Hello World";
    }
}
