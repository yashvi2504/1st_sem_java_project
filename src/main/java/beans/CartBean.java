

package beans;

import ejb.CustomerEJBLocal;
import entity.Cart;
import entity.CartItems;
import entity.Offers;
import entity.Orders;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.PrimeFaces;
//import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFile;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;

@Named("cartBean")
@SessionScoped
public class CartBean implements Serializable {

    @Inject
    private CustomerEJBLocal customerEJB;

    @Inject
    private LoginBean loginBean;

    @Inject
    private CustomerBean customerBean;   // ⭐ For selected address
private Integer currentOrderId;   // order being placed

    private Cart activeCart;
    private List<CartItems> cartItems;
private Offers appliedOffer;
private double discountedTotal;
private Integer lastPlacedOrderId;

    // ⭐ NEW FIELD — stores user selection (COD / Online)
    private String paymentMethod;

    public CartBean() {}
@PostConstruct
public void init() {
    loadCart();
    customerBean.ensureAddressSelected(); // 🔥 THIS FIXES UI
}

    // =============== INIT CART =================
  
private UploadedFile uploadedPrescription;
private Integer uploadMedicineId;

public UploadedFile getUploadedPrescription() { return uploadedPrescription; }
public void setUploadedPrescription(UploadedFile uploadedPrescription) { this.uploadedPrescription = uploadedPrescription; }
public void preparePrescriptionUpload(Integer medicineId) {
    if (medicineId == null) {
        addMessage("Invalid medicine selected!");
        return;
    }
    this.uploadMedicineId = medicineId;
}
public void uploadPrescription() {

    try {
        // ✅ Medicine must be selected
        if (uploadMedicineId == null) {
            addMessage("Medicine not selected!");
            return;
        }

        // ✅ File must be selected
        if (uploadedPrescription == null) {
            addMessage("Please choose a prescription file!");
            return;
        }

        // ✅ Save file
        String folderPath = "D:/java/yasi/prescriptions/";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_"
                + uploadedPrescription.getFileName();

        File target = new File(folder, fileName);

        try (InputStream in = uploadedPrescription.getInputStream();
             FileOutputStream out = new FileOutputStream(target)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }

        // ✅ SAVE prescription WITHOUT orderId (THIS IS WHAT YOU WANT)
        customerEJB.savePrescription(
                loginBean.getLoggedUser().getUserId(),
                null,                    // ✅ NO orderId
                uploadMedicineId,
                fileName,
                uploadedPrescription.getContentType()
        );

        addMessage("Prescription uploaded successfully!");

        // reset
        uploadedPrescription = null;
        uploadMedicineId = null;

    } catch (Exception e) {
        e.printStackTrace();
        addMessage("Upload failed!");
    }
}
public double getSubTotal() {
    if (cartItems == null) return 0;
    double sum = 0;
    for (CartItems ci : cartItems) {
        sum += ci.getPricePerUnit() * ci.getQuantity();
    }
    return sum;
}

public double getTotal() {
    return discountedTotal; // already calculated after offers
}

public int getItemCount() {
    if (cartItems == null) {
        return 0;
    }
    return cartItems.size();
}

private void addMessage(String msg) {
    FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null);
    FacesContext.getCurrentInstance().addMessage(null, m);
}
public Integer getLastPlacedOrderId() {
    return lastPlacedOrderId;
}
public boolean isPrescriptionRequiredButNotUploaded() {

    if (cartItems == null) return false;

    for (CartItems ci : cartItems) {
        if (ci.getMedicineId() != null &&
            Boolean.TRUE.equals(ci.getMedicineId().isPrescriptionRequired())) {

            // if order not placed OR prescription not uploaded
            if (lastPlacedOrderId == null) {
                return true;
            }
        }
    }
    return false;
}

public void loadCart() {

    if (loginBean == null || loginBean.getLoggedUser() == null) {
        cartItems = new ArrayList<>();
        activeCart = null;
        return;
    }

    activeCart = customerEJB.getActiveCart(
        loginBean.getLoggedUser().getUserId()
    );

    if (activeCart == null) {
        cartItems = new ArrayList<>();
        return;
    }

    cartItems = new ArrayList<>(activeCart.getCartItemsCollection());
    applyBestOffer();
}


    // =============== REMOVE ITEM =================
    public void remove(Integer cartItemId) {
        if (loginBean.getLoggedUser() == null) return;
        customerEJB.removeCartItem(loginBean.getLoggedUser().getUserId(), cartItemId);
        loadCart();
    }
    public void confirmOrder() {

    if (cartItems == null || cartItems.isEmpty()) {
        addMessage("Your cart is empty");
        return;
    }

    if (customerBean.getSelectedAddress() == null) {
        addMessage("Please add or select an address");
        return;
    }

    if (paymentMethod == null || paymentMethod.isBlank()) {
        addMessage("Please select a payment method");
        return;
    }

    try {
        Integer userId = loginBean.getLoggedUser().getUserId();
        Integer addressId = customerBean.getSelectedAddress().getAddressId();
        Integer offerId = appliedOffer != null ? appliedOffer.getOfferId() : null;

        Orders order = customerEJB.placeOrderFromCart(
                userId,
                addressId,
                paymentMethod,
                offerId
        );

        lastPlacedOrderId = order.getOrderId();

        customerEJB.attachUploadedPrescriptionsToOrder(
                userId,
                lastPlacedOrderId
        );

        addMessage("Order placed successfully!");
        loadCart();

    } catch (Exception e) {
    e.printStackTrace();

    addMessage(
        "Order failed: " + 
        (e.getMessage() != null ? e.getMessage() : e.getClass().getName())
    );
}

}

    // =============== GETTERS & SETTERS ===============

  public List<CartItems> getCartItems() {

    if (loginBean.getLoggedUser() == null) {
        return new ArrayList<>();
    }

    // 🔥 ALWAYS reload from DB
    activeCart = customerEJB.getActiveCart(
        loginBean.getLoggedUser().getUserId()
    );

    if (activeCart == null) {
        return new ArrayList<>();
    }

    cartItems = customerEJB.getCartItems(
        loginBean.getLoggedUser().getUserId()
    );

    applyBestOffer();

    return cartItems;
}


    public Cart getActiveCart() {
        return activeCart;
    }



    // ⭐ For Radio Button Binding
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }public void addToCart(Integer medId) {

    if (loginBean.getLoggedUser() == null) {
        return;
    }

    Integer userId = loginBean.getLoggedUser().getUserId();

    // Call EJB method
    customerEJB.addOrUpdateCartItem(userId, medId, 1);

    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Added to Cart!", null));
}
// ------------------- GETTERS & SETTERS -------------------
private void applyBestOffer() {

    appliedOffer = null;

    double cartTotal = getSubTotal();   // ✅ USE subtotal
    discountedTotal = cartTotal;        // ✅ DEFAULT

    List<Offers> offers = new ArrayList<>(customerEJB.getActiveOffers());

    double bestDiscount = 0;

    for (Offers o : offers) {

        double minAmount = o.getMinOrderAmount() != null
                ? o.getMinOrderAmount().doubleValue()
                : 0;

        if (cartTotal < minAmount) continue;

        double discount = 0;

        if ("PERCENT".equals(o.getDiscountType())) {
            discount = cartTotal * (o.getDiscountValue().doubleValue() / 100);
        } else if ("FIXED".equals(o.getDiscountType())) {
            discount = o.getDiscountValue().doubleValue();
        }

        if (discount > bestDiscount) {
            bestDiscount = discount;
            appliedOffer = o;
        }
    }

    discountedTotal = cartTotal - bestDiscount;

    if (discountedTotal < 0) discountedTotal = 0;
}

public Offers getAppliedOffer() {
    return appliedOffer;
}

public double getDiscountedTotal() {
    return discountedTotal;
}public void increase(Integer medicineId) {
    customerEJB.increaseCartItemQuantity(
        loginBean.getLoggedUser().getUserId(), medicineId
    );
}


public void decrease(Integer medicineId) {
    if (medicineId == null) return;
    customerEJB.decreaseCartItemQuantity(
        loginBean.getLoggedUser().getUserId(), medicineId
    );
    loadCart();
}


}