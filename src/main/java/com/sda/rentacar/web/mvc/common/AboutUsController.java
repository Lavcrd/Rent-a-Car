package com.sda.rentacar.web.mvc.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/about")
public class AboutUsController
{
    @RequestMapping(method = RequestMethod.GET)
    public String aboutPage()
    {
        return "common/about";
    }
}
