package com.tradenity.shop;

import com.tradenity.sdk.spring.config.EnableTradenity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by joseph
 * on 2/23/16.
 */
@SpringBootApplication
@EnableTradenity
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
