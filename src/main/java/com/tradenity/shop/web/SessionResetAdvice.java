package com.tradenity.shop.web;

import com.tradenity.sdk.client.TradenityClient;
import com.tradenity.sdk.exceptions.SessionExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by joseph
 * on 8/30/16.
 */
@ControllerAdvice
public class SessionResetAdvice {

    @Autowired
    TradenityClient client;

    @ExceptionHandler({SessionExpiredException.class})
    public String handleNotFoundErrors(SessionExpiredException e, HttpServletRequest request){
        System.out.println("Handling session expiration....");
        client.resetSession();
        //Spring does not allow RedirectAttributes in ExceptionHandler methods
        //ra.addFlashAttribute("info", "Session has been reset.");
        //this is a work around
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("info","Session has been reset.");
        return "redirect:/";
    }

}
