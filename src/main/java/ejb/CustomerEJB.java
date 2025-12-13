

package ejb;

import entity.Prescription;
import entity.*;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Stateless

public class CustomerEJB implements CustomerEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

   @Override
public List<Categories> getAllCategories() {
    List<Categories> categories = em.createNamedQuery("Categories.findAll", Categories.class)
                                    .getResultList();
    return categories;
}
@Override
public Collection<Medicines> getAllMedicines() {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findAll", Medicines.class)
                                        .getResultList();
    return medicines;
}

@Override
public List<Manufacturers> getAllManufacturers() {
    List<Manufacturers> manufacturers = em.createNamedQuery("Manufacturers.findAll", Manufacturers.class)
                                          .getResultList();
    return manufacturers;
}

   @Override
public Collection<Medicines> getMedicinesByCategory(Integer categoryId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByCategory", Medicines.class)
                                        .setParameter("categoryId", categoryId)
                                        .getResultList();
    return medicines;
}

    @Override
public Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByManufacturer", Medicines.class)
                                        .setParameter("manufacturerId", manufacturerId)
                                        .getResultList();
    return medicines;
}
@Override
public Collection<Medicines> getMedicineByName(String name) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByName", Medicines.class)
                                        .setParameter("name", name)
                                        .getResultList();
    return medicines;
}
@Override
public Collection<Offers> getActiveOffers() {
    return em.createNamedQuery("Offers.findActive", Offers.class)
             .getResultList();
}
@Override
public Cart getActiveCart(Integer userId) {
    Users user = em.find(Users.class, userId);
    return em.createNamedQuery("Cart.findActiveByUser", Cart.class)
             .setParameter("user", user)
             .getResultStream()
             .findFirst()
             .orElse(null); // Returns null if no active cart
}
//@Override
//public String addOrUpdateCartItem(Integer userId, Integer medicineId, int quantity) {
//    // 1️⃣ Find the user
//    Users user = em.find(Users.class, userId);
////    if (user == null) {
////        return "Error: User not found";
////    }
//
//    // 2️⃣ Find the medicine
//    Medicines med = em.find(Medicines.class, medicineId);
////    if (med == null) {
////        return "Error: Medicine not found";
////    }
//
//    // 3️⃣ Get or create active cart
//    TypedQuery<Cart> q = em.createNamedQuery("Cart.findActiveByUser", Cart.class);
//    q.setParameter("user", user);
//    Cart cart = q.getResultStream().findFirst().orElse(null);
//
//    if (cart == null) {
//        cart = new Cart();
//        cart.setUserId(user);
//        cart.setCreatedAt(new Date());
//        cart.setStatus("ACTIVE");
//        cart.setTotalAmount(0.0);
//        cart.setCartItemsCollection(new ArrayList<>());
//        em.persist(cart);
//    }
//
//    // 4️⃣ Check if item already exists
//    CartItems existingItem = null;
//    for (CartItems item : cart.getCartItemsCollection()) {
//        if (item.getMedicineId().getMedicineId().equals(medicineId)) {
//            existingItem = item;
//            break;
//        }
//    }
//
//    // 5️⃣ Add or update the cart item
//    if (existingItem != null) {
//        existingItem.setQuantity(existingItem.getQuantity() + quantity);
//        em.merge(existingItem);
//    } else {
//        CartItems newItem = new CartItems();
//        newItem.setCartId(cart);
//        newItem.setMedicineId(med);
//        newItem.setQuantity(quantity);
//        newItem.setPricePerUnit(med.getPrice().doubleValue());
//        newItem.setAddedDate(new Date());
//        em.persist(newItem);
//
//        cart.getCartItemsCollection().add(newItem);
//        em.merge(cart);
//    }
//
//    return "Cart updated successfully";
//}
@Override
public String addOrUpdateCartItem(Integer userId, Integer medicineId, int quantity) {
    if (userId == null || medicineId == null || quantity <= 0) {
        return "Invalid request";
    }

    Users user = em.find(Users.class, userId);
    if (user == null) {
        return "User not found";
    }

    // 1) find active cart (if any)
    TypedQuery<Cart> cartQ = em.createNamedQuery("Cart.findActiveByUser", Cart.class);
    cartQ.setParameter("user", user);
    Cart cart = cartQ.getResultStream().findFirst().orElse(null);

    // 2) create cart when needed
    if (cart == null) {
        cart = new Cart();
        cart.setUserId(user);
        cart.setCreatedAt(new Date());
        cart.setStatus("ACTIVE");
        cart.setTotalAmount(0.0);
        cart.setCartItemsCollection(new ArrayList<>());
        em.persist(cart);
        em.flush(); // ensure cart gets an ID
    }

    // 3) load medicine
    Medicines med = em.find(Medicines.class, medicineId);
    if (med == null) {
        return "Medicine not found";
    }

    // 4) Try to find existing CartItems row using JPQL
    CartItems existing = null;
    try {
        TypedQuery<CartItems> itemQ = em.createQuery(
            "SELECT ci FROM CartItems ci WHERE ci.cartId.cartId = :cid AND ci.medicineId.medicineId = :mid",
            CartItems.class);
        itemQ.setParameter("cid", cart.getCartId());
        itemQ.setParameter("mid", medicineId);
        existing = itemQ.getSingleResult();
    } catch (NoResultException nre) {
        existing = null;
    }

    if (existing != null) {
        existing.setQuantity(existing.getQuantity() + quantity);
        em.merge(existing);
    } else {
        CartItems newItem = new CartItems();
        newItem.setCartId(cart);
        newItem.setMedicineId(med);
        newItem.setQuantity(quantity);
        newItem.setPricePerUnit(med.getPrice() != null ? med.getPrice().doubleValue() : 0.0);
        newItem.setAddedDate(new Date());
        em.persist(newItem);
        em.flush(); // force DB write

        if (cart.getCartItemsCollection() == null) {
            cart.setCartItemsCollection(new ArrayList<>());
        }
        cart.getCartItemsCollection().add(newItem);
    }

    // 5) recalc total and merge
    double total = 0.0;
    if (cart.getCartItemsCollection() != null) {
        for (CartItems ci : cart.getCartItemsCollection()) {
            total += (ci.getPricePerUnit() != null ? ci.getPricePerUnit() : 0.0) * ci.getQuantity();
        }
    }
    cart.setTotalAmount(total);
    em.merge(cart);
    em.flush();

    return "Cart updated successfully. Total: " + total;
}

@Override
public String removeCartItem(Integer userId, Integer cartItemId) {
    // Get the active cart for the given user
    Cart cart = getActiveCart(userId);

    // If no active cart exists, return a message
    if (cart == null) {
        return "No active cart found for user.";
    }

    // Initialize a variable to hold the item to remove
    CartItems itemToRemove = null;

    // Loop through the items in the cart to find the one to remove
    for (CartItems item : cart.getCartItemsCollection()) {
        // Check if the current item's ID matches the cartItemId
        if (item.getCartItemId().equals(cartItemId)) {
            // Found the item, assign it to itemToRemove
            itemToRemove = item;
            // Exit the loop since item is found
            break;
        }
    }

    // If the item was not found in the cart, return a message
    if (itemToRemove == null) {
        return "Cart item not found.";
    }

    // Remove the item from the cart's collection
    cart.getCartItemsCollection().remove(itemToRemove);

    // Remove the item from the database
    em.remove(em.contains(itemToRemove) ? itemToRemove : em.merge(itemToRemove));

    // Recalculate the total amount of the cart after removal
    double total = 0.0;
    for (CartItems item : cart.getCartItemsCollection()) {
        total += item.getPricePerUnit() * item.getQuantity(); // Sum price * quantity
    }

    // Update the cart's total amount
    cart.setTotalAmount(total);

    // Merge the updated cart back into the database
    em.merge(cart);

    // Return a success message with the updated total
    return "Cart item removed successfully. Total: " + total;
}
 
  
    @Override
    public void addAddressToCustomer(String street, String city, String state, String zip, Integer userId) {
        // Find the user
        Users user = em.find(Users.class, userId);

        // Create new address
        Addresses a = new Addresses();
        a.setAddressLine(street);
        a.setCity(city);
        a.setState(state);
        a.setPincode(zip);
        a.setUserId(user);

        // Add to user's address collection
        Collection<Addresses> addresses = user.getAddressesCollection();         if (addresses == null) {
            addresses = new ArrayList<>();
        }
        addresses.add(a);
        user.setAddressesCollection(addresses);

        // Persist address and merge user
        em.persist(a);
        em.merge(user);
    }

    @Override
    public void updateAddress(Integer addressId, String street, String city, String state, String zip, Integer userId) {
        Addresses a = em.find(Addresses.class, addressId);
        if (a != null && a.getUserId().getUserId().equals(userId)) {
            a.setAddressLine(street);
            a.setCity(city);
            a.setState(state);
            a.setPincode(zip);
            em.merge(a);
        }
    }

    @Override
    public void deleteAddress(Integer addressId, Integer userId) {
        Addresses a = em.find(Addresses.class, addressId);
        if (a != null && a.getUserId().getUserId().equals(userId)) {
            em.remove(a);
        }
    }
// CORRECTED and more EFFICIENT Code
@Override
public List<Addresses> getAddressesByUserId(Integer userId) {
    // We use a JPQL query that directly filters by the user's ID in the database.
    // This ensures only valid addresses linked to the user are returned,
    // avoiding the need to iterate through ALL addresses and perform a crash-prone check.
    try {
        TypedQuery<Addresses> q = em.createQuery(
            "SELECT a FROM Addresses a WHERE a.userId.userId = :userId", Addresses.class);
        q.setParameter("userId", userId);
        return q.getResultList();
        
    } catch (Exception e) {
        // Log the error if the query fails for some reason
        e.printStackTrace();
        return new ArrayList<>();
    }
}
// ===================== ORDER MANAGEMENT =====================

@Override
public Orders placeOrderFromCart(Integer userId, Integer shippingAddressId, String paymentMethod, Integer offerId) {

    Users user = em.find(Users.class, userId);
    Addresses address = em.find(Addresses.class, shippingAddressId);

    // 1️⃣ Get active cart
    Cart cart = getActiveCart(userId);

    if (cart == null || cart.getCartItemsCollection() == null || cart.getCartItemsCollection().isEmpty()) {
        throw new RuntimeException("Cart is empty or not found.");
    }

    // 2️⃣ Create order
    Orders order = new Orders();
    order.setUserId(user);
    order.setDeliveryAddressId(address);
    order.setOrderDate(new Date());
    order.setStatus("Placed");
    order.setPaymentMethod(paymentMethod);

    // ⭐ APPLY OFFER ID TO ORDER
    if (offerId != null) {
        Offers offer = em.find(Offers.class, offerId);
        order.setOfferId(offer);
    }

    BigDecimal total = BigDecimal.ZERO;

    List<OrderItems> orderItemsList = new ArrayList<>();

    // 3️⃣ Convert cart items → order items
    for (CartItems ci : cart.getCartItemsCollection()) {

        OrderItems oi = new OrderItems();
        oi.setOrderId(order);
        oi.setMedicineId(ci.getMedicineId());
        oi.setQuantity(ci.getQuantity());

        BigDecimal price = BigDecimal.valueOf(ci.getPricePerUnit() * ci.getQuantity());
        oi.setPrice(price);

        total = total.add(price);

        em.persist(oi);
        orderItemsList.add(oi);
    }

    // ⭐ If an offer is applied, reduce total
    if (order.getOfferId() != null) {
        Offers applied = order.getOfferId();

        double finalTotal = total.doubleValue();

        if ("PERCENT".equals(applied.getDiscountType())) {
            double discount = finalTotal * (applied.getDiscountValue().doubleValue() / 100);
            finalTotal -= discount;
        } else if ("FIXED".equals(applied.getDiscountType())) {
            double discount = applied.getDiscountValue().doubleValue();
            finalTotal -= discount;
        }

        if (finalTotal < 0) finalTotal = 0;

        total = BigDecimal.valueOf(finalTotal);
    }

    order.setTotalAmount(total);
    order.setOrderItemsCollection(orderItemsList);

    em.persist(order);
    em.flush();

    // 4️⃣ Create payment record
    Payments payment = new Payments();
    payment.setOrderId(order);
    payment.setPaymentDate(new Date());
    payment.setAmount(total);
    payment.setMethod(paymentMethod);

    if (paymentMethod.equalsIgnoreCase("COD")) {
        payment.setStatus("Pending");
    } else {
        payment.setStatus("Paid");
    }

    em.persist(payment);

    // 5️⃣ Clear cart (items + cart status)
    for (CartItems ci : cart.getCartItemsCollection()) {
        em.remove(em.contains(ci) ? ci : em.merge(ci));
    }
    cart.getCartItemsCollection().clear();
    cart.setTotalAmount(0.0);
    cart.setStatus("Completed");
    em.merge(cart);

    return order;
}

@Override
public List<Orders> getOrderHistory(Integer userId) {
    TypedQuery<Orders> q = em.createQuery(
        "SELECT o FROM Orders o WHERE o.userId.userId = :userId ORDER BY o.orderDate DESC", Orders.class);
    q.setParameter("userId", userId);
    return q.getResultList();
}

@Override
public Orders getOrderDetails(Integer orderId, Integer userId) {
    TypedQuery<Orders> q = em.createQuery(
        "SELECT o FROM Orders o WHERE o.orderId = :orderId AND o.userId.userId = :userId", Orders.class);
    q.setParameter("orderId", orderId);
    q.setParameter("userId", userId);
    return q.getResultStream().findFirst().orElse(null);
}
@Override
public Integer savePrescription(Integer userId, Integer medicineId, String filePath, String fileType) {

    Users user = em.find(Users.class, userId);
    Medicines med = em.find(Medicines.class, medicineId);

    Prescription p = new Prescription();
    p.setUserId(user);
    p.setMedicineId(med);
    p.setFilePath(filePath);
    p.setFileType(fileType);
    p.setStatus("PENDING");
    p.setUploadedAt(new Date());

    em.persist(p);
    em.flush();

    return p.getPrescriptionId();
}

// inside class CustomerEJB (which already has @PersistenceContext EntityManager em;)
@Override
public Orders getOrderById(Integer orderId) {
    try {
        // Fetch order, address, items and item->medicine in one query to avoid lazy init issues
        TypedQuery<Orders> q = em.createQuery(
            "SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.orderItemsCollection i " +
            "LEFT JOIN FETCH i.medicineId m " +
            "LEFT JOIN FETCH o.deliveryAddressId a " +   // or o.addressId depending on your field name
            "WHERE o.orderId = :oid", Orders.class);
        q.setParameter("oid", orderId);
        return q.getSingleResult();
    } catch (NoResultException nre) {
        return null;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

@Override
public void updateOrderStatus(Integer orderId, String newStatus) {

    Orders order = em.find(Orders.class, orderId);

    if (order == null) return;

    // ⭐ If order is Packed or Shipped → reduce stock
    if (newStatus.equalsIgnoreCase("Packed") ||
        newStatus.equalsIgnoreCase("Shipped")) {

        for (OrderItems item : order.getOrderItemsCollection()) {

            Medicines med = item.getMedicineId();

            // reduce stock
            int updatedStock = med.getStock() - item.getQuantity();
            if (updatedStock < 0) updatedStock = 0;

            med.setStock(updatedStock);
            em.merge(med);
        }
    }

    // ⭐ Update status normally
    order.setStatus(newStatus);
    em.merge(order);
}
@Override
public List<Orders> getAllOrders() {
    return em.createQuery("SELECT o FROM Orders o ORDER BY o.orderId DESC", Orders.class)
             .getResultList();
}
public String registerCustomer(String username, String email, String phone, String password) {
    try {
        // 🔍 Check duplicate username
        List<Users> checkUser = em.createNamedQuery("Users.findByUsername", Users.class)
                .setParameter("username", username)
                .getResultList();

        if (!checkUser.isEmpty()) {
            return "Username already exists!";
        }

        // 🔍 Check duplicate email
        List<Users> checkEmail = em.createNamedQuery("Users.findByEmail", Users.class)
                .setParameter("email", email)
                .getResultList();

        if (!checkEmail.isEmpty()) {
            return "Email already registered!";
        }

        // 🎯 Fetch the CUSTOMER role
        Roles role = em.createNamedQuery("Roles.findByRoleName", Roles.class)
                .setParameter("roleName", "CUSTOMER")
                .getSingleResult();

        // 🎯 Create new user
        Users u = new Users();
        u.setUsername(username);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPassword(password); // 👉 If hashing required, tell me
        u.setRoleId(role);

        em.persist(u);

        return "SUCCESS";

    } catch (Exception e) {
        e.printStackTrace();
        return "Registration failed: " + e.getMessage();
    }
}
private void recalcAndMergeCartTotal(Cart cart) {
    double total = 0.0;

    if (cart.getCartItemsCollection() != null) {
        for (CartItems ci : cart.getCartItemsCollection()) {
            double price = ci.getPricePerUnit() != null ? ci.getPricePerUnit() : 0.0;
            total += price * ci.getQuantity();
        }
    }

    cart.setTotalAmount(total);
    em.merge(cart);
    em.flush();
}
@Override
public Integer getCartItemQuantity(int userId, int medicineId) {
    CartItems item = em.createQuery(
        "SELECT ci FROM CartItems ci WHERE ci.cartId.userId.userId = :uid AND ci.medicineId.medicineId = :mid",
        CartItems.class)
        .setParameter("uid", userId)
        .setParameter("mid", medicineId)
        .getResultStream()
        .findFirst()
        .orElse(null);

    return item != null ? item.getQuantity() : 0;
}
@Override
public void increaseCartItemQuantity(int userId, int medicineId) {

    Users user = em.find(Users.class, userId);
    if (user == null) return;

    // 1) Find or create active cart
    Cart cart = em.createNamedQuery("Cart.findActiveByUser", Cart.class)
                  .setParameter("user", user)
                  .getResultStream()
                  .findFirst()
                  .orElse(null);

    if (cart == null) {
        cart = new Cart();
        cart.setUserId(user);
        cart.setCreatedAt(new Date());
        cart.setStatus("ACTIVE");
        cart.setTotalAmount(0.0);
        cart.setCartItemsCollection(new ArrayList<>());
        em.persist(cart);
        em.flush();
    }

    Medicines med = em.find(Medicines.class, medicineId);
    if (med == null) return;

    // 2) Get existing cart item
    CartItems existing = em.createQuery(
        "SELECT ci FROM CartItems ci WHERE ci.cartId.cartId = :cid AND ci.medicineId.medicineId = :mid",
        CartItems.class)
        .setParameter("cid", cart.getCartId())
        .setParameter("mid", medicineId)
        .getResultStream()
        .findFirst()
        .orElse(null);

    if (existing != null) {
        existing.setQuantity(existing.getQuantity() + 1);
        em.merge(existing);
    } else {
        CartItems newItem = new CartItems();
        newItem.setCartId(cart);
        newItem.setMedicineId(med);
        newItem.setQuantity(1);
        newItem.setPricePerUnit(med.getPrice().doubleValue());
        newItem.setAddedDate(new Date());
        em.persist(newItem);

        cart.getCartItemsCollection().add(newItem);
    }

    recalcAndMergeCartTotal(cart);
}
@Override
public void decreaseCartItemQuantity(int userId, int medicineId) {

    Users user = em.find(Users.class, userId);
    if (user == null) return;

    Cart cart = em.createNamedQuery("Cart.findActiveByUser", Cart.class)
                  .setParameter("user", user)
                  .getResultStream()
                  .findFirst()
                  .orElse(null);

    if (cart == null) return;

    CartItems existing = em.createQuery(
        "SELECT ci FROM CartItems ci WHERE ci.cartId.cartId = :cid AND ci.medicineId.medicineId = :mid",
        CartItems.class)
        .setParameter("cid", cart.getCartId())
        .setParameter("mid", medicineId)
        .getResultStream()
        .findFirst()
        .orElse(null);

    if (existing == null) return;

    if (existing.getQuantity() > 1) {
        existing.setQuantity(existing.getQuantity() - 1);
        em.merge(existing);
    } else {
        // quantity = 1 → remove
        cart.getCartItemsCollection().remove(existing);
        em.remove(em.contains(existing) ? existing : em.merge(existing));
    }

    recalcAndMergeCartTotal(cart);
}

@Override
public List<Prescription> getPrescriptionsByUser(Integer userId) {
    return em.createQuery("SELECT p FROM Prescription p WHERE p.userId.userId = :uid", Prescription.class)
             .setParameter("uid", userId)
             .getResultList();
}

@Override
public void updatePrescriptionStatus(Integer userId, Integer medicineId, String newStatus) {

    Prescription p = em.createQuery(
        "SELECT p FROM Prescription p WHERE p.userId.userId = :uid AND p.medicineId.medicineId = :mid",
        Prescription.class)
        .setParameter("uid", userId)
        .setParameter("mid", medicineId)
        .getResultStream()
        .findFirst()
        .orElse(null);

    if (p != null) {
        p.setStatus(newStatus);
        em.merge(p);
    }
}

}
