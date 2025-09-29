package com.ighost.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FunctionPageController {

    @GetMapping("/policy-query")
    public String policyQuery() {
        return "policy-query";
    }

    @GetMapping("/policy-edit")
    public String policyEdit() {
        return "policy-edit";
    }

    @GetMapping("/customer-query")
    public String customerQuery() {
        return "customer-query";
    }

    @GetMapping("/customer-edit")
    public String customerEdit() {
        return "customer-edit";
    }

    @GetMapping("/user-query")
    public String userQuery() {
        return "user-query";
    }

    @GetMapping("/user-edit")
    public String userEdit() {
        return "user-edit";
    }
}
