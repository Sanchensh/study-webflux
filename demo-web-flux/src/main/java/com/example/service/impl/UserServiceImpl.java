package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.pojo.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    @Cacheable(cacheNames = "user",key = "'user_' + #p0")
    public User find(String id) {
        return userDao.find(Integer.parseInt(id));
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public void insert(User user) {
        userDao.insert(user);
    }

    @Override
    @CachePut(cacheNames = "user",key = "'user_' + #p0.id")
    public void update(User user) {
        userDao.update(user);
    }

    @Override
    @CacheEvict(cacheNames = "user",key = "'user_' + #p0")
    public Mono<Void> delete(String id) {
        userDao.delete(Integer.parseInt(id));
        return Mono.empty();
    }
}
