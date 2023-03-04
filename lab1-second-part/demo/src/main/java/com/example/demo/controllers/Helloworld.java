package com.example.demo.controllers;

import com.example.demo.controllers.Dtos.NameRequestBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class Helloworld {

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
}
