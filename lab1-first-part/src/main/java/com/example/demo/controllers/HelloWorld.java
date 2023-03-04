package com.example.demo.controllers;

import com.example.demo.dtos.LoginDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class HelloWorld {

    public static final String LOGGED_IN = "loggedIn";
    public static final String SESSION_RESULT = "session-result";
    private int integer = 1;

    @GetMapping("/index")
    public String index(Model model) {
        integer++;
        int integer2 = 0;
        model.addAttribute("integer", integer);
        model.addAttribute("integer2", integer2);
        return "index";
    }


    @RequestMapping("/headers/{header}")
    public String httpMethodGet(Model model, @RequestHeader Map<String, String> headers,
                                @PathVariable("header") String header, HttpServletRequest request) {
        model.addAttribute("httpMethod", request.getMethod());
        model.addAttribute("remoteAddr", request.getRemoteAddr());
        model.addAttribute("serverName", request.getServerName());
        model.addAttribute("header", request.getHeader(header));

        return "method";
    }


    @GetMapping("/params")
    public String nameAndSurname(
            @ModelAttribute(name = "param1") @RequestParam("param1") String param1,
            @ModelAttribute(name = "param2") @RequestParam("param2") String param2,
            Model model) {
        try {
            model.addAttribute("param1", Integer.parseInt(param1) + Integer.parseInt(param2));
        } catch(Exception e) {
            model.addAttribute("param1", param1);
            model.addAttribute("param2", param2);
        }

        return "params";
    }

    @GetMapping("/login-form")
    public String loginForm(Model model) {
        model.addAttribute("login", new LoginDto());
        return "session";
    }

    @GetMapping("/logout-button")
    public String logoutButton() {
        return "logout-button";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.setAttribute(LOGGED_IN, false);
        return SESSION_RESULT;
    }

    @PostMapping("/session")
    public String session(Model model, HttpSession session, @ModelAttribute("login") LoginDto login) {
        if (login.getPassword().equals("test") && login.getUserName().equals("test")) {
            model.addAttribute(LOGGED_IN, true);
            session.setAttribute(LOGGED_IN, true);
        }
        else {
            model.addAttribute(LOGGED_IN, false);
            session.setAttribute(LOGGED_IN, false);
        }
        return SESSION_RESULT;
    }

    @GetMapping("/session-result")
    public String getSessionResult(Model model, HttpSession session) {
        model.addAttribute(LOGGED_IN,
                session.getAttribute(LOGGED_IN) != null ?
                        session.getAttribute(LOGGED_IN) : false);
        return SESSION_RESULT;
    }

    @GetMapping("/count-cookie")
    public ResponseEntity<?> countCookie(Model model, @CookieValue(name = "count", defaultValue = "0") String count) {
        ResponseCookie cookie = ResponseCookie.from("count", String.valueOf(Integer.parseInt(count) + 1))
                .httpOnly(true)
                .maxAge(60)
                        .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(cookie.getValue());
    }

    @GetMapping("/date-and-name")
    public ResponseEntity<String> dateAndName() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String dateString = dateFormat.format(date);
        return ResponseEntity.ok()
                .body("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>Title</title>
                </head>
                <body>
                  <p>
                  """ + dateString + """
                  </p>
                  <form action="http://localhost:8081/name" method="POST">
                    <label for="name">name:</label>
                    <input type="text" id="name" name="name" /><br/>
                    <button type="submit">submit</button>
                  </form>
                </body>
                </html>
                """);
    }

}
