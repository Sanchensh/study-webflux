package com.mrxu.handler;
import com.mrxu.component.UserComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserHandler {
    private final static String path = "/user";
    private final static String id = "/{id}";
    @Bean
    public RouterFunction<ServerResponse> routes(UserComponent component) {
        return route(GET(path), component::findAll)
                .andRoute(GET(path + id), component::find)
                .andRoute(PUT(path).and(accept(MediaType.APPLICATION_JSON)), component::update)
                .andRoute(POST(path).and(accept(MediaType.APPLICATION_JSON)),component::insert)
                .andRoute(DELETE(path + id),component::delete);
    }
}
