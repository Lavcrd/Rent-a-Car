package com.sda.carrental.web.mvc.handlers;

import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class RequestRejectedExceptionHandler implements RequestRejectedHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, RequestRejectedException ex) {
        try {
            req.getSession().setAttribute("message", "Page not found or access denied.");
            res.sendRedirect("/");
        } catch (IOException ignore) {
        }
    }
}
