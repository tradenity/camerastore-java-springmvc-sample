package com.tradenity.shop.web;

import com.tradenity.sdk.model.*;
import com.tradenity.sdk.services.*;
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
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.List;

import static com.tradenity.sdk.model.PaymentSource.STATUS_NEW;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by joseph
 * on 3/21/16.
 */
@Controller
@SessionAttributes("order")
@RequestMapping(path = "/orders")
public class OrdersController {
    @Value("${stripe.publicKey:xxxxx}")
    String stripePublicKey;

    @Value("${payment.gateway.name:bogus}")
    String gateway;

    Country usa;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    CountryService countryService;

    @Autowired
    OrderService orderService;

    @Autowired
    StateService stateService;

    @Autowired
    ShippingMethodService shippingMethodService;

    @Autowired
    CustomerService customerService;

    @Autowired
    PaymentTokenService paymentTokenService;

    @Autowired
    CreditCardPaymentService creditCardPaymentService;

    public static class ModelEditor extends PropertyEditorSupport {
        Class<? extends BaseModel> modelClass;

        public ModelEditor(Class<? extends BaseModel> modelClass) {
            this.modelClass = modelClass;
        }

        @Override
        public void setAsText(String text) {
            BaseModel m = null;
            try {
                m = modelClass.newInstance();
                m.setId(text);
                this.setValue(m);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    @InitBinder
    public void initRequest(WebDataBinder dataBinder){
        dataBinder.registerCustomEditor(ShippingMethod.class, new ModelEditor(ShippingMethod.class));
        dataBinder.registerCustomEditor(Country.class, new ModelEditor(Country.class));
        dataBinder.registerCustomEditor(State.class, new ModelEditor(State.class));
    }


    @RequestMapping(method = GET, path = {"", "/"})
    public String index(@CurrentUser User user, Model model, Pageable pageable){
        model.addAttribute("orders", orderService.findAllBy("customer", user.toCustomer(), Utils.mapPageable(pageable)));
        return "orders/index";
    }

    @RequestMapping(method = GET, path="/checkout")
    public String newInstance(@CurrentUser User user, Model model){
        Order order = new Order().customer(user.toCustomer());
        Page<Country> countries = countryService.findAll(new PageRequest(0, 250).sortBy("name", Sort.SortOrder.ASC));
        Page<State> usStates = stateService.findAllBy("country", getUSA().getId(), new PageRequest(0, 100).sortBy("name"));
        order.setShippingAddress(createAddress());
        order.setBillingAddress(createAddress());
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("cart", shoppingCartService.get());
        model.addAttribute("order", order);
        model.addAttribute("states", usStates);
        model.addAttribute("countries", countries);
        return "orders/new";
    }

    @RequestMapping(method = GET, path="/countries/{countryId}/states")
    @ResponseBody
    public List<State> getStates(@PathVariable("countryId")String countryId){
        return stateService.findAllBy("country", countryId, new PageRequest(0, 100)).getContent();
    }

    @RequestMapping(method = POST, path="/checkout")
    public String create(@Valid @ModelAttribute("order") Order order, BindingResult result, Model model, RedirectAttributes ra){
        if(result.hasErrors()){
            return "redirect:/orders/checkout";
        }
        order = orderService.create(order);
        Page<ShippingMethod> shippingMethods = shippingMethodService.findAllForOrder(order.getId(), new PageRequest());
        //Page<ShippingMethod> shippingMethods = shippingMethodService.findAll();
        model.addAttribute("shippingMethods", shippingMethods);
        model.addAttribute("order", order);
        return "orders/checkout-forms :: shipping-form";
    }

    @RequestMapping(method = POST, path="/add_shipping")
    public String addShippingInfo(@ModelAttribute("order") Order order, Model model){
        order = orderService.update(order);
        model.addAttribute("order", order);
        return "orders/checkout-forms :: payment-form";
    }

    @RequestMapping(method = POST, path="/add_payment")
    public String addPaymentInfo(@ModelAttribute("order") Order order, @RequestParam("token") String stripeToken, HttpSession session, SessionStatus sessionStatus, Model model, RedirectAttributes ra){
        if (stripeToken != null && !stripeToken.trim().isEmpty()) {
            session.setAttribute("stripToken", stripeToken);
            model.addAttribute("stripTokenAvailable", true);
        }
        PaymentSource ps = paymentTokenService.create(new PaymentToken().token(stripeToken).customer(order.getCustomer()).status(STATUS_NEW));
        Payment p = creditCardPaymentService.create(new CreditCardPayment().order(order).paymentSource(ps));

        sessionStatus.setComplete();
        ra.addAttribute("orderId", order.getId());
        return "redirect:/orders/{orderId}";
    }

    private boolean isValid(Order order) {
        return true;
    }

    @RequestMapping(method = GET, path = "/{orderId}")
    public String show(@CurrentUser User user, @PathVariable("orderId")String orderId, Model model){
        Order order = orderService.findById(orderId);
        if(order.getCustomer() != null && order.getCustomer().getId().equals(user.getCustomerId())){
            model.addAttribute("order", order);
            return "orders/show";
        }else{
            return "errors/403";
        }

    }

    @RequestMapping(method = DELETE, path="/{orderId}")
    public String refund(@PathVariable("orderId") String orderId, RedirectAttributes ra){
        /*Transaction transaction = orderService.refund(orderId);
        ra.addAttribute("orderId", transaction.getOrder().getId());*/
        return "redirect:/orders/{orderId}";
    }

    @ExceptionHandler(Exception.class )
    public String handleException(Exception e, Model model){
        e.printStackTrace();
        model.addAttribute("message", e.getMessage());
        return "errors/500";
    }

    private Address createAddress() {
        return new Address().streetLine1("3290 Hermosillo Place").streetLine2("").city("Washington").state(new State().id("")).zipCode("20521-3290").country(getUSA());
    }

    private Country getUSA() {
        if(usa == null) {
            usa = countryService.findBy("iso2", "US");
        }
        return usa;
    }
}
