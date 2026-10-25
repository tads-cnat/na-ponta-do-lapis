package com.npl.na_ponta_do_lapis.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiRootController {

    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/api/docs";
    }
}
