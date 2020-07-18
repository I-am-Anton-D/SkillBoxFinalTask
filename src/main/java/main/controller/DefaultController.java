package main.controller;

import java.util.List;
import main.model.Post;
import org.springframework.data.repository.query.Param;
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