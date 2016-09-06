package com.tradenity.shop.web;

import com.tradenity.sdk.model.*;
import com.tradenity.sdk.services.CategoryService;
import com.tradenity.sdk.services.CustomerService;
import com.tradenity.sdk.services.OrderService;
import com.tradenity.sdk.services.ShoppingCartService;
import com.tradenity.shop.model.User;
import com.tradenity.shop.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.List;

/**
 * Created by joseph
 * on 3/21/16.
 */
@Controller
@RequestMapping(path = "/orders")
public class OrdersController {
    @Value("${stripe.publicKey:xxxxx}")
    String stripePublicKey;

    @Value("${payment.gateway.name:bogus}")
    String gateway;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    OrderService orderService;

    @Autowired
    CustomerService customerService;

    public static class ShippingMethodEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) {
            ShippingMethod sm = new ShippingMethod();
            sm.setId(text);
            this.setValue(sm);
        }
    }

    @InitBinder
    public void initRequest(WebDataBinder dataBinder){
        dataBinder.registerCustomEditor(ShippingMethod.class, new ShippingMethodEditor());
    }


    @RequestMapping(method = RequestMethod.GET, path = {"", "/"})
    public String index(@CurrentUser User user, Model model, Pageable pageable){
        model.addAttribute("orders", orderService.findAllByCustomer(user.toCustomer(), Utils.mapPageable(pageable)));
        return "orders/index";
    }

    @RequestMapping(method = RequestMethod.GET, path="/checkout")
    public String newInstance(@CurrentUser User user, Model model){
        Checkout checkout = shoppingCartService.checkout();
        Order order = new Order(user.toCustomer());
        order.setShippingAddress(createAddress());
        order.setBillingAddress(createAddress());
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("gateway", gateway);
        model.addAttribute("order", order);
        model.addAttribute("checkout", checkout);
        return "orders/new";
    }

    @RequestMapping(method = RequestMethod.POST, path="/checkout")
    public String create(@CurrentUser User user, @Valid @ModelAttribute Order order, BindingResult result, @RequestParam("token") String stripeToken, HttpSession session, Model model, RedirectAttributes ra){
        if(result.hasErrors()){
            if(stripeToken != null && !stripeToken.trim().isEmpty()){
                session.setAttribute("stripToken", stripeToken);
                model.addAttribute("stripTokenAvailable", true);
            }
            Checkout checkout = shoppingCartService.checkout();
            model.addAttribute("stripePublicKey", stripePublicKey);
            model.addAttribute("gateway", gateway);
            model.addAttribute("order", order);
            model.addAttribute("checkout", checkout);
            return "orders/new";
        }else{
            Customer customer = user.toCustomer();
            order.setCustomer(customer);
            Transaction transaction = orderService.placeOrder(order, stripeToken);
            ra.addAttribute("orderId", transaction.getOrder().getId());
            return "redirect:/orders/{orderId}";
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{orderId}")
    public String show(@CurrentUser User user, @PathVariable("orderId")String orderId, Model model){
        Order order = orderService.findById(orderId);
        if(order.getCustomer() != null && order.getCustomer().getId().equals(user.getCustomerId())){
            model.addAttribute("order", order);
            return "orders/show";
        }else{
            return "errors/403";
        }

    }

    @RequestMapping(method = RequestMethod.DELETE, path="/{orderId}")
    public String refund(@PathVariable("orderId") String orderId, RedirectAttributes ra){
        Transaction transaction = orderService.refund(orderId);
        ra.addAttribute("orderId", transaction.getOrder().getId());
        return "redirect:/orders/{orderId}";
    }

    @ExceptionHandler(Exception.class )
    public String handleException(Exception e, Model model){
        e.printStackTrace();
        model.addAttribute("message", e.getMessage());
        return "errors/500";
    }

    private Address createAddress() {
        return new Address("3290 Hermosillo Place", "", "Washington", "Washington, DC", "20521-3290", "USA");
    }
}
