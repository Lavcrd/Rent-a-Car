package com.sda.carrental.web.mvc.handlers;

import com.sda.carrental.global.ConstantValues;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ConstantValues cv;
    private final String KEY = "message";
    private final String CB_URI_PREFIX = "/mg-car/car-bases";
    private final String VIEW_CB_URI_SUFFIX = "/update-image";

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus
    public void handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest req, HttpServletResponse res) {
        try {
            String uri = req.getRequestURI();
            final String SIZE_MESSAGE = "Failure: File size exceeds permitted maximum of " + cv.getMaxFileSize() / (1024 * 1024) + "Mb";

            if (uri.equals(CB_URI_PREFIX + "/register")) {
                req.getSession().setAttribute(KEY, SIZE_MESSAGE);
                res.sendRedirect(CB_URI_PREFIX);
                return;
            } else if (uri.startsWith(CB_URI_PREFIX) && uri.endsWith(VIEW_CB_URI_SUFFIX) && extractCarBaseId(uri) != null) {
                req.getSession().setAttribute(KEY, SIZE_MESSAGE);
                res.sendRedirect(CB_URI_PREFIX + "/" + extractCarBaseId(uri));
                return;
            }
            req.getSession().setAttribute(KEY, "Failure: Unexpected error or invalid URI");
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
