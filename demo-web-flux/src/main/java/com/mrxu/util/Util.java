package com.mrxu.util;

import com.mrxu.pojo.User;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<User> get() {
        List<User> list = new ArrayList<>();
        list.add(new User(1,"xujl"));
        list.add(new User(2,"xujl6"));
        list.add(new User(3,"xujl7"));
        return list;
    }
}
