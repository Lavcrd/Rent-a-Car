package com.sda.carrental.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/regulations")
public class RegulationsController {
    @RequestMapping(method = RequestMethod.GET)
    public String welcomePage() {
        return "regulations";
    }
}