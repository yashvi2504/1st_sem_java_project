//
//package rest;
//
//import ejb.AdminEJBLocal;
//import ejb.CustomerEJBLocal;
//import entity.Addresses;
//import entity.Cart;
//import entity.Categories;
//import entity.DeliveryTracking;
//import entity.Manufacturers;
//import entity.Medicines;
//import entity.Offers;
//import entity.OrderItems;
//import entity.Orders;
//import entity.Users;
//import jakarta.ejb.EJB;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//import java.util.List;
//
//@Path("customer")
//@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.APPLICATION_JSON)
//public class CustomerREST {
//
//    @EJB
//    private CustomerEJBLocal customerEJB;
//@EJB
//private AdminEJBLocal adminEJB;
//  
//    // Get all categories
//    @GET
//    @Path("categories")
//    public List<Categories> getAllCategories() {
//        return adminEJB.getAllCategories();
//    }
//    @GET
//@Path("medicines")
//public Collection<Medicines> getAllMedicines() {
//    return adminEJB.getAllMedicines();
//}
//@GET
//@Path("manufacturers")
//public List<Manufacturers> getAllManufacturers() {
//    return adminEJB.getAllManufacturers();
//}
//@GET
//@Path("medicines/category/{categoryId}")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> getMedicinesByCategory(@PathParam("categoryId") Integer categoryId) {
//    return customerEJB.getMedicinesByCategory(categoryId);
//}
//@GET
//@Path("medicines/manufacturer/{manufacturerId}")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> getMedicinesByManufacturer(@PathParam("manufacturerId") Integer manufacturerId) {
//    return customerEJB.getMedicinesByManufacturer(manufacturerId);
//}
//@GET
//@Path("medicines/search/{name}")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> getMedicineByName(@PathParam("name") String name) {
//    return customerEJB.getMedicineByName(name);
//}
//@GET
//@Path("offers/active")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Offers> getActiveOffers() {
//    return customerEJB.getActiveOffers();
//}
//// Get active cart for a user
//@GET
//@Path("cart/active/{userId}")
//public Cart getActiveCart(@PathParam("userId") Integer userId) {
//    return customerEJB.getActiveCart(userId);
//}
//@POST
//@Path("cart/add/{userId}/{medicineId}/{quantity}")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
//public String addOrUpdateCartItem(
//        @PathParam("userId") Integer userId,
//        @PathParam("medicineId") Integer medicineId,
//        @PathParam("quantity") int quantity) {
//    return customerEJB.addOrUpdateCartItem(userId, medicineId, quantity);
//}
//
//  // Remove cart item
//@DELETE
//@Path("/{userId}/cart/{cartItemId}")
//public Response removeCartItem(
//    @PathParam("userId") Integer userId,
//    @PathParam("cartItemId") Integer cartItemId
//) {
//    String result = customerEJB.removeCartItem(userId, cartItemId);
//    return Response.ok(result).build();
//}
//
//
//  @POST
//    @Path("add/{userId}")
//    public String addAddressToCustomer(
//            @PathParam("userId") Integer userId,
//            @QueryParam("street") String street,
//            @QueryParam("city") String city,
//            @QueryParam("state") String state,
//            @QueryParam("zip") String zip
//    ) {
//        customerEJB.addAddressToCustomer(street, city, state, zip, userId);
//        return "Address added successfully";
//    }
//
//    // Update address
//    @PUT
//    @Path("update/{userId}/{addressId}")
//    public String updateAddress(
//            @PathParam("userId") Integer userId,
//            @PathParam("addressId") Integer addressId,
//            @QueryParam("street") String street,
//            @QueryParam("city") String city,
//            @QueryParam("state") String state,
//            @QueryParam("zip") String zip
//    ) {
//        customerEJB.updateAddress(addressId, street, city, state, zip, userId);
//        return "Address updated successfully";
//    }
//
//    // Delete address
//    @DELETE
//    @Path("delete/{userId}/{addressId}")
//    public String deleteAddress(
//            @PathParam("userId") Integer userId,
//            @PathParam("addressId") Integer addressId
//    ) {
//        customerEJB.deleteAddress(addressId, userId);
//        return "Address deleted successfully";
//    }
//
//    // Get all addresses for a user
//    @GET
//    @Path("user/{userId}")
//    public List<Addresses> getAddressesByUser(@PathParam("userId") Integer userId) {
//        return customerEJB.getAddressesByUserId(userId);
//    }
//    // ===================== ORDER REST ENDPOINTS =====================
//
//// Place order from cart
//@POST
//@Path("order/place/{userId}/{shippingAddressId}/{paymentMethod}")
//@Produces(MediaType.APPLICATION_JSON)
//public Orders placeOrderFromCart(
//        @PathParam("userId") Integer userId,
//        @PathParam("shippingAddressId") Integer shippingAddressId,
//        @PathParam("paymentMethod") String paymentMethod) {
//    return customerEJB.placeOrderFromCart(userId, shippingAddressId, paymentMethod);
//}
//
//// Get all past orders of a user
//@GET
//@Path("order/history/{userId}")
//@Produces(MediaType.APPLICATION_JSON)
//public List<Orders> getOrderHistory(@PathParam("userId") Integer userId) {
//    return customerEJB.getOrderHistory(userId);
//}
//// Get order details
//@GET
//@Path("order/details/{orderId}/{userId}")
//@Produces(MediaType.APPLICATION_JSON)
//public Orders getOrderDetails(
//        @PathParam("orderId") Integer orderId,
//        @PathParam("userId") Integer userId) {
//    return customerEJB.getOrderDetails(orderId, userId);
//}
//
//
//
//}
