package com.npl.na_ponta_do_lapis.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiRootController {

    @GetMapping(value = {"", "/"})
    public String redirectToSwagger() {
        // Com context-path=/api, o redirecionamento relativo ao contexto resulta em /api/docs
        return "redirect:/docs";
    }
}
