package main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for routing
 */
@Controller
public class RouteController {

    /**
     * use frontend
     *
     * @return redirecting
     */

    @RequestMapping(value = {
        "/edit/*",
        "/calendar/*",
        "/my/*",
        "/login",
        "/login/**",
        "/moderator/*",
        "/moderation/*",
        "/post/*",
        "/posts/*",
        "/profile",
        "settings",
        "/stat",
        "/404"
    })
    public String frontend() {
        return "forward:/";
    }
}
