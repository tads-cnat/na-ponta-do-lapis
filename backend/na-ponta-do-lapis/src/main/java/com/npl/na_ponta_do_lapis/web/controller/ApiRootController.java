package com.npl.na_ponta_do_lapis.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiRootController {

    @Value("${springdoc.swagger-ui.enabled:true}")
    private boolean swaggerEnabled;

    @Value("${app.web-url:https://napontadolapis.app}")
    private String webUrl;

    @GetMapping(value = {"", "/"})
    public String index() {
        if (swaggerEnabled) {
            // Com context-path=/api, o redirecionamento relativo resulta em /api/docs
            return "redirect:/docs";
        }
        return "redirect:" + webUrl;
    }
}
