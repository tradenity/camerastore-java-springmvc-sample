package com.tradenity.shop.web;

import com.tradenity.sdk.client.TradenityClient;
import com.tradenity.sdk.exceptions.*;
import com.tradenity.sdk.model.*;
import com.tradenity.sdk.services.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static com.tradenity.sdk.model.Sort.sort;
import static com.tradenity.sdk.spring.web.Utils.pageRequest;

/**
 * User: Joseph Fouad
 * Date: 10/23/2015
 * Time: 7:37 PM
 */
@Controller
@RequestMapping("/")
public class StoreFrontController {

    @Autowired
    TradenityClient client;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    BrandService brandService;

    @Autowired
    CustomerService customerService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    OrderService orderService;

    @ModelAttribute
    public void commonAttributes(Model model){
        model.addAttribute("categories", categoryService.findAll(new PageRequest(0, 10, sort("position"))));
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("cart", shoppingCartService.get());
    }

    @RequestMapping({"", "/"})
    public String index(Model model){
        model.addAttribute("collections", collectionService.findAll());
        return "shop/index";
    }

    @RequestMapping("/products")
    public String allProducts(@RequestParam(value = "query", required = false) String query, Model model, @PageableDefault(9) Pageable pageable){
        Page<Product> products;
        if(query != null){
            products = productService.search("name", query);
        }else {
            products = productService.findAll(pageRequest(pageable));
        }
        model.addAttribute("products", products);
        model.addAttribute("featured", collectionService.findBy("slug", "featured"));
        model.addAttribute("breadcrumb", "All");
        model.addAttribute("breadcrumbName", "products");
        return "shop/products";
    }

    @RequestMapping("/categories/{cat_id}/products")
    public String productsByCategory(@PathVariable("cat_id") String id, Model model){
        Category category = categoryService.findById(id);
        model.addAttribute("category", category);
        model.addAttribute("products", productService.findAllBy("categories__contains", category));
        model.addAttribute("featured", collectionService.findBy("slug", "featured"));
        model.addAttribute("breadcrumb", "Categories");
        model.addAttribute("breadcrumbName", category.getName());
        return "shop/products";
    }

    @RequestMapping("/brands/{brand_id}/products")
    public String productsByBrand(@PathVariable("brand_id")String id, Model model){
        Brand brand = brandService.findById(id);
        model.addAttribute("breadcrumb", "Brands");
        model.addAttribute("breadcrumbName", brand.getName());
        model.addAttribute("brand", brand);
        model.addAttribute("products", productService.findAllBy("brand", brand));
        model.addAttribute("featured", collectionService.findBy("slug", "featured"));
        return "shop/products";
    }

    @RequestMapping("/products/{id}")
    public String product(@PathVariable("id")String id, Model model){
        model.addAttribute("featured", collectionService.findBy("slug", "featured"));
        model.addAttribute("product", productService.findById(id));
        return "shop/single";
    }



    @ExceptionHandler({EntityNotFoundException.class})
    public String handleNotFoundErrors(EntityNotFoundException e, Model model){
        model.addAttribute("message", e.getMessage());
        return "errors/404";
    }
        //@ExceptionHandler({ClientErrorException.class, ServerErrorException.class, RequestErrorException.class})
    public String handleErrors(RuntimeException e, Model model){
        String error = "Something wrong happened, please try again.";
        if(e instanceof ClientErrorException){
            error = "Internal error occured";
        }else if(e instanceof RequestErrorException){
            error = "Invalid data error.";
        }else if(e instanceof ServerErrorException){
            error = "Remote error occured.";
        }
        model.addAttribute("message", error);
        return "shop/error";
    }


}
