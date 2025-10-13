package com.ighost.demo.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.google.code.kaptcha.impl.DefaultKaptcha;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;

@Controller
@RequiredArgsConstructor
public class CaptchaController {

    public static final String CAPTCHA_SESSION_KEY = "CAPTCHA_SESSION_KEY";

    private final DefaultKaptcha captchaProducer;

    @GetMapping("/captcha.jpg")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        String captchaText = captchaProducer.createText();
        request.getSession().setAttribute(CAPTCHA_SESSION_KEY, captchaText.toLowerCase());

        BufferedImage image = captchaProducer.createImage(captchaText);
        try (ServletOutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "jpg", out);
            out.flush();
        }
    }
}
