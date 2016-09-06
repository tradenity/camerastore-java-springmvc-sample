/**
 * Created by joseph
 * on 3/17/16.
 */
$(function(){
    var cart_total = $("#cart_total");
    var cart_items_count = $("#cart_items_count");

    $(".add_to_cart_button").click(function(){
        var el = $(this);
        var product_id = el.data("product_id");
        var url = "/cart";
        $.ajax(url, {
            method: 'POST',
            data: {product: product_id, quantity: 1},
            success: function(result){
                console.log(result);
                cart_total.text(result.content.total);
                cart_items_count.text(result.content.items.length);
            }
        });
        return false;
    });
});