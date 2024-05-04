package com.ljz.transmission.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class Cocurrent {
    @Bean
    @Scope("singleton")
    public Lock lock(){
        return new ReentrantLock();
    }
}
