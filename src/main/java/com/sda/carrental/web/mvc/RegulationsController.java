package com.sda.carrental.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/regulations")
public class RegulationsController {
    //Page
    @RequestMapping(method = RequestMethod.GET)
    public String regulationsPage() {
        return "core/regulations";
    }
}
