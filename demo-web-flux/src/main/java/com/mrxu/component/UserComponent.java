package com.mrxu.component;

import com.mrxu.pojo.User;
import com.mrxu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class UserComponent {
    @Autowired
    private UserService service;

    public Mono<ServerResponse> insert(ServerRequest serverRequest){
        Mono<User> user = serverRequest.bodyToMono(User.class);
        return ok().build(user.doOnNext(u -> service.insert(u)).then());
    }
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        Mono<User> user = serverRequest.bodyToMono(User.class);
        return ok().build(user.doOnNext(u -> service.update(u)).then());
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        return ok().build(service.delete(id));
    }

    public Mono<ServerResponse> find(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        return ok().contentType(MediaType.APPLICATION_JSON).body(Mono.fromSupplier(() -> service.find(id)),User.class);
    }

    public Mono<ServerResponse> findAll(ServerRequest serverRequest){
        return ok().contentType(MediaType.APPLICATION_JSON).body(Flux.fromStream(() -> service.findAll().stream()),User.class);
    }
}
