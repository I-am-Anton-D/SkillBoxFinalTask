package main.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Default controller for index page ant other
 */
@Controller
public class DefaultController {

    /**
     * Mapint index page
     *
     * @param model Spring model
     * @return template (index.html)
     */

    @RequestMapping("/")
    public static String index(Model model) {
        return "index";
    }

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