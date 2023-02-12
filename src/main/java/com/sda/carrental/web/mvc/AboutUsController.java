package com.sda.carrental.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/aboutUs")
public class AboutUsController
{
    @RequestMapping(method = RequestMethod.GET)
    public String welcomePage()
    {
        return "aboutUs";
    }
}