package main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController
{
    @RequestMapping("/")
    public static String index(Model model)
    {
        return "index";
    }

}