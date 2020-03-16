package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.pojo.User;
import com.example.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class HelloServiceImpl implements HelloService {
    @Autowired
    private UserDao userDao;

    @Override
    public Mono<User> find(Integer id) {
        return Mono.fromSupplier(() -> userDao.find(id));
    }

    @Override
    public Flux<User> findAll() {
        return Flux.fromStream(() -> userDao.findAll().stream());
    }

    @Override
    public Mono<Void> insert(User user) {
        return Mono.just(userDao.insert(user) != 0).then();
    }

    @Override
    public Mono<Void> update(User user) {
        return Mono.just(userDao.update(user) != 0).then();
    }

    @Override
    public Mono<Void> delete(Integer id) {
        return Mono.just(userDao.delete(id) != 0).then();
    }
}
