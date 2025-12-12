
package ejb;

import entity.Addresses;
import entity.Cart;
import entity.Categories;
import entity.Medicines;
import entity.Offers;
import entity.Orders;
import entity.DeliveryTracking; 
import entity.Manufacturers;
import jakarta.ejb.Local;
import java.util.Collection;
import java.util.List;

@Local
public interface CustomerEJBLocal {

    List<Categories> getAllCategories();//done
    Collection<Medicines> getAllMedicines();//done
    List<Manufacturers> getAllManufacturers();//done
Orders getOrderById(Integer orderId);
void updateOrderStatus(Integer orderId, String newStatus);

    Collection<Medicines> getMedicinesByCategory(Integer categoryId);
    Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId);

    Collection<Medicines> getMedicineByName(String name);
List<Orders> getAllOrders();

    Collection<Offers> getActiveOffers();

    Cart getActiveCart(Integer userId);

    String addOrUpdateCartItem(Integer userId, Integer medicineId, int quantity);

    String removeCartItem(Integer userId, Integer cartItemId);
  
    void addAddressToCustomer(String street, String city, String state, String zip, Integer userId);

    void updateAddress(Integer addressId, String street, String city, String state, String zip, Integer userId);

    void deleteAddress(Integer addressId, Integer userId);

    List<Addresses> getAddressesByUserId(Integer userId);
    
  Orders placeOrderFromCart(Integer userId, Integer shippingAddressId, String paymentMethod, Integer offerId);
  List<Orders> getOrderHistory(Integer userId);
    Orders getOrderDetails(Integer orderId, Integer userId);
String registerCustomer(String username, String email, String phone, String password);
 Integer savePrescription(Integer userId, Integer medicineId, String filePath, String fileType) ;

    // Cart Quantity Controls
    Integer getCartItemQuantity(int userId, int medicineId);
    void increaseCartItemQuantity(int userId, int medicineId);
    void decreaseCartItemQuantity(int userId, int medicineId);

    
}
