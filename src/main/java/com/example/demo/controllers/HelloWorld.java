package com.example.demo.controllers;

import com.example.demo.dtos.LoginDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class HelloWorld {

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

    @PostMapping("/session")
    public String session(Model model, HttpSession session, @ModelAttribute("login") LoginDto login) {
        if (login.getPassword().equals("test") && login.getUserName().equals("test")) {
            model.addAttribute("loggedIn", "true");
            session.setAttribute("loggedIn", true);
        }
        else {
            model.addAttribute("loggedIn", "false");
            session.setAttribute("loggedIn", false);
        }
        return "session-result";
    }

    @GetMapping("/session-result")
    public String getSessionResult(Model model, HttpSession session) {
        model.addAttribute("loggedIn",
                session.getAttribute("loggedIn") != null ?
                        session.getAttribute("loggedIn") : false);
        return "session-result";
    }


}
