package com.gym.crm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
@Api(tags = "Authentication", description = "Endpoints for user login operations")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @GetMapping
    @ApiOperation(value = "User Login", notes = "Requires HTTP Basic Authentication header. Returns 200 OK if successful.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully authenticated"),
            @ApiResponse(code = 401, message = "Invalid username or password")
    })
    public ResponseEntity<Void> login() {
        log.info("Login request successful");
        return ResponseEntity.ok().build();
    }
}