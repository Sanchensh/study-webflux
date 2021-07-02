package com.mrxu.controller;

import com.mrxu.pojo.User;
import com.mrxu.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin
public class HelloController {
    @Autowired
    private HelloService service;

    @GetMapping("/hello")
    public Flux<User> findAll() {
        return service.findAll();
    }

    @GetMapping("/hello/{id}")
    public Mono<User> find(@RequestParam(value = "id", defaultValue = "1", required = false) Integer id) {
        return service.find(id);
    }

    @PostMapping("/hello")
    public Mono<Void> insert(@RequestBody User user) {
        return service.insert(user);
    }

    @PutMapping("/hello")
    public Mono<Void> update(@RequestBody User user) {
        return service.update(user);
    }

    @DeleteMapping("/hello/{id}")
    public Mono<Void> delete(@PathVariable(value = "id") Integer id) {
        return service.delete(id);
    }
}
