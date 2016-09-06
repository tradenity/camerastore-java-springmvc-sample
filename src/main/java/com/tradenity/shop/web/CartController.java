package com.tradenity.shop.web;

import com.tradenity.sdk.exceptions.InventoryErrorException;
import com.tradenity.sdk.exceptions.RequestErrorException;
import com.tradenity.sdk.model.LineItem;
import com.tradenity.sdk.model.Product;
import com.tradenity.sdk.model.ShoppingCart;
import com.tradenity.sdk.services.CategoryService;
import com.tradenity.sdk.services.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.List;

/**
 * Created by joseph
 * on 8/12/16.
 */
@Controller
public class CartController {

    @Autowired
    CategoryService categoryService;
    @Autowired
    ShoppingCartService shoppingCartService;

    public static class ProductEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) {
            Product p = new Product();
            p.setId(text);
            this.setValue(p);
        }
    }

    @InitBinder
    public void initRequest(WebDataBinder dataBinder){
        dataBinder.registerCustomEditor(Product.class, new ProductEditor());
    }

    @RequestMapping(method = RequestMethod.POST, path = "/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Op addToCart(@ModelAttribute LineItem item){
        try {
            ShoppingCart sc = shoppingCartService.addItem(item);
            return new Op<>("success", "Item added successfully!", sc);
        }catch (RequestErrorException e) {
            return new Op("error", e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/cart/update/{itemId}")
    public String updateItem(@PathVariable("itemId") String itemId,
                             @RequestParam(value = "quantity", required = false)Integer quantity,
                             @RequestParam(value = "onsuccess", required = false)String onsuccess,
                             RedirectAttributes ra){

        String view = "redirect:/cart";
        if(onsuccess != null && onsuccess.equals("checkout")) {
            view = "redirect:/orders/checkout";
        }

        if (quantity != null) {
            if(quantity <= 0) {
                shoppingCartService.delete(itemId);
            }else{
                try {
                    shoppingCartService.update(itemId, quantity);
                }catch (InventoryErrorException e){
                    ra.addFlashAttribute("error", e.getMessage());
                    return view;
                }
            }
        }
        ra.addFlashAttribute("info", "Shopping cart updated successfully");
        return view;

    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/cart/{itemId}")
    public String removeItem(@PathVariable("itemId") String itemId){
        shoppingCartService.delete(itemId);
        return "redirect:/orders/checkout";
    }

    //headers={"X-Requested-With=XMLHttpRequest"}
    @RequestMapping(method = RequestMethod.DELETE, path = "/cart/{itemId}",
            produces = MediaType.APPLICATION_JSON_VALUE, headers={"X-Requested-With=XMLHttpRequest"})
    @ResponseBody
    public Op removeItemAjax(@PathVariable("itemId") String itemId){
        try {
            ShoppingCart sc = shoppingCartService.delete(itemId);
            return new Op<>("success", "Item removed successfully!", sc);
        }catch (RuntimeException e) {
            return new Op("error", e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/cart")
    public String cart(Model model){
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("cart", shoppingCartService.get());
        return "shop/cart";
    }

    public static class Op<T>{
        String status;
        String message;
        T content;

        public Op(String status, String message) {
            this(status, message, null);
        }

        public Op(String status, String message, T content) {
            this.status = status;
            this.message = message;
            this.content = content;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public T getContent() {
            return content;
        }
    }
}
