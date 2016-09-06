package com.tradenity.shop.security;

import com.tradenity.sdk.model.Customer;
import com.tradenity.sdk.services.CustomerService;
import com.tradenity.shop.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Created by joseph
 * on 2/23/16.
 */
@Service
public class UserServiceImpl implements UserDetailsService{
    @Autowired
    CustomerService customerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerService.findByUsername(username);
        if(customer == null) {
            throw new UsernameNotFoundException("Could not find user " + username);
        }
        return new User(customer);
    }
}
