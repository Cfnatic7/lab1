package com.example.demo.controllers;

import com.example.demo.dtos.LoginDto;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class HelloWorld {

    public static final String LOGGED_IN = "loggedIn";
    public static final String SESSION_RESULT = "session-result";
    public static final String OUR_ADDRESS = "http://localhost:8080";
    public static final String THEIR_ADDRESS = "http://localhost:8081";

    @Value("${server.port}")
    private String port;

    private final List<String> messages = new ArrayList<>();
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


    @GetMapping("/chat-display")
    public ResponseEntity<String> displayChat() {
        StringBuilder message = concatenateMessages();
        return ResponseEntity.ok()
                .body("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta http-equiv=Refresh content='10'>
                  <title>chat</title>
                </head>
                <body>
                  <p>
                  """ + message + """
                  </p>
                  <iframe name="dummyframe" id="dummyframe" style="display: none;"></iframe>
                  <form action="http://localhost:8080/chat" method="POST" target="dummyframe">
                    <label for="message">message:</label>
                    <input type="text" id="message" name="message" /><br/>
                    <button type="submit">send</button>
                  </form>
                </body>
                </html>
                """);
    }

    @PostMapping(value = "/chat", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> message(@RequestBody MultiValueMap<String, String> map, HttpServletRequest request)
            throws IOException {
        String newMessage = map.get("message").get(0);
        List<String> requestPort = map.get("port");
        this.messages.add(newMessage);
        if (requestPort == null) {
            sendMessageToOtherServlet(newMessage);
        }

        return ResponseEntity.ok()
                .build();
    }

    private void sendMessageToOtherServlet(String newMessage) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(THEIR_ADDRESS + "/chat");
        List<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("message", newMessage));
        params.add(new BasicNameValuePair("port", port));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpclient.execute(httppost);
    }

    private StringBuilder concatenateMessages() {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < this.messages.size(); i++) {
            message.append(this.messages.get(i)).append("<br/>");
        }
        return message;
    }

}
