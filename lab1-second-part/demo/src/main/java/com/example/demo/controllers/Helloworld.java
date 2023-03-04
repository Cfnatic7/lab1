package com.example.demo.controllers;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Helloworld {

    public static final String OUR_ADDRESS = "http://localhost:8081";
    public static final String THEIR_ADDRESS = "http://localhost:8080";

    @Value("${server.port}")
    private String port;
    private final List<String> messages = new ArrayList<>();

    @PostMapping(value = "/name", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> nameAndRedirect(@RequestBody MultiValueMap<String, String> map) {
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .body("""
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                      <meta charset="UTF-8">
                      <title>Title</title>
                    </head>""" +
                    "<body>" +
                            "<a href='http://localhost:8080/date-and-name'>go back</a>" +
                            "<p>hello: " + map.get("name") + "</p>" +
                    "</body>" +
                    """
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
                  <form action="http://localhost:8081/chat" method="POST" target="dummyframe">
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
