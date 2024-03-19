package com.sda.carrental.web.mvc.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
@RequiredArgsConstructor
public class FileSizeExceptionHandler {
    private final String MSG_KEY = "message";
    private final String CB_URI_PREFIX = "/mg-car/car-bases";
    private final String VIEW_CB_URI_SUFFIX = "/update-image";
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus
    public void handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest req, HttpServletResponse res) {
        try {
            String uri = req.getRequestURI();
            final String SIZE_MESSAGE = "Failure: File size exceeds permitted maximum of " + maxFileSize;

            if (uri.equals(CB_URI_PREFIX + "/register")) {
                req.getSession().setAttribute(MSG_KEY, SIZE_MESSAGE);
                res.sendRedirect(CB_URI_PREFIX);
                return;
            } else if (uri.startsWith(CB_URI_PREFIX) && uri.endsWith(VIEW_CB_URI_SUFFIX) && extractCarBaseId(uri) != null) {
                req.getSession().setAttribute(MSG_KEY, SIZE_MESSAGE);
                res.sendRedirect(CB_URI_PREFIX + "/" + extractCarBaseId(uri));
                return;
            }
            req.getSession().setAttribute(MSG_KEY, "Failure: Unexpected error or invalid URI");
            res.sendRedirect("/");
        } catch (IOException ignored) {
        }
    }

    private String extractCarBaseId(String uri) {
        if (uri.startsWith(CB_URI_PREFIX) && uri.endsWith(VIEW_CB_URI_SUFFIX)) {
            String id = uri.substring(CB_URI_PREFIX.length() + 1, uri.length() - VIEW_CB_URI_SUFFIX.length());
            return id.isEmpty() ? null : id;
        }
        return null;
    }
}
