package com.tradenity.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by joseph
 * on 2/23/16.
 */
@Configuration
public class MVCConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/").setCachePeriod(31556926);
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/").setCachePeriod(31556926);
        //registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
