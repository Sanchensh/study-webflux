package com.mrxu.service;

import com.mrxu.pojo.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HelloService {
    Mono<User> find(Integer id);

    Flux<User> findAll();

    Mono<Void> insert(User user);

    Mono<Void> update(User user);

    Mono<Void> delete(Integer id);
}
