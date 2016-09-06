package com.tradenity.shop.web;

import com.tradenity.sdk.exceptions.CustomerCreationException;
import com.tradenity.sdk.services.CustomerService;
import com.tradenity.shop.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by joseph
 * on 3/17/16.
 */
@Controller
public class AccountController {

    @Autowired
    CustomerService customerService;

    @RequestMapping(path = "/login", method = GET)
    public String login(){
        return "account/login";
    }

    @RequestMapping(path = "/register", method = GET)
    public String register(Model model){
        model.addAttribute("user", new User());
        return "account/register";
    }

    @RequestMapping(path = "/register", method = POST)
    public String create(@Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes ra){
        if(result.hasErrors()){
            return "account/register";
        }
        if(!user.getPassword().equals( user.getConfirmPassword())){
            result.addError(new FieldError("user", "password", "Password does not match"));
            return "account/register";
        }
        try {
            customerService.create(user.toCustomer());
            ra.addFlashAttribute("info", "Registration successful, please login!");
            return "redirect:/login";
        }catch (CustomerCreationException ex){
            result.addError(new FieldError("user", ex.getErrorFieldName(), ex.getMessage()));
            return "account/register";
        }
    }
}
