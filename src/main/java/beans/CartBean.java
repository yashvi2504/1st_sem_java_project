

package beans;

import ejb.CustomerEJBLocal;
import entity.Cart;
import entity.CartItems;
import entity.Offers;
import entity.Orders;
import jakarta.annotation.PostConstruct;
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
@ViewScoped
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

    // =============== INIT CART =================
    @PostConstruct
    public void init() {
        loadCart();
    }
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
        if (uploadMedicineId == null) {
            addMessage("Medicine not selected!");
            return;
        }

        if (uploadedPrescription == null) {
            addMessage("Please choose a prescription file!");
            return;
        }

        String folderPath = "D:/java/yasi/prescriptions/";
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

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

        customerEJB.savePrescription(
                loginBean.getLoggedUser().getUserId(),
                currentOrderId,
//                null,                 // ✅ orderId NOT needed now
                uploadMedicineId,
                fileName,
                uploadedPrescription.getContentType()
        );
customerEJB.updateOrderStatus(currentOrderId, "WAITING_APPROVAL");

        addMessage("Prescription uploaded successfully!");

    } catch (Exception e) {
        e.printStackTrace();
        addMessage("Upload failed!");
    }
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
            Boolean.TRUE.equals(ci.getMedicineId().getPrescriptionRequired())) {

            // if order not placed OR prescription not uploaded
            if (lastPlacedOrderId == null) {
                return true;
            }
        }
    }
    return false;
}

public void loadCart() {
    if (loginBean.getLoggedUser() == null) {
        cartItems = new ArrayList<>();
        return;
    }

    activeCart = customerEJB.getActiveCart(loginBean.getLoggedUser().getUserId());

    if (activeCart == null || activeCart.getCartItemsCollection() == null) {
        cartItems = new ArrayList<>();
        discountedTotal = 0;
        appliedOffer = null;
        return;
    }

    cartItems = new ArrayList<>(activeCart.getCartItemsCollection());

    // ⭐ AUTO APPLY OFFER
    applyBestOffer();
}

private void applyBestOffer() {

    appliedOffer = null;
    discountedTotal = activeCart != null ? activeCart.getTotalAmount() : 0;

    List<Offers> offers = new ArrayList<>(customerEJB.getActiveOffers());
    double cartTotal = activeCart != null ? activeCart.getTotalAmount() : 0;

    double bestDiscount = 0;

    for (Offers o : offers) {

        // ⭐ Convert BigDecimal → double safely
        double minAmount = (o.getMinOrderAmount() == null)
                ? 0
                : o.getMinOrderAmount().doubleValue();

        // ⭐ FIX 1 — Compare double < double
        if (cartTotal < minAmount) {
            continue;
        }

        // ⭐ Medicine-specific offer check
        if (o.getMedicineId() != null) {
            boolean found = cartItems.stream()
                .anyMatch(ci -> ci.getMedicineId().getMedicineId()
                        .equals(o.getMedicineId().getMedicineId()));

            if (!found) continue;
        }

        double discount = 0;

        if ("PERCENT".equals(o.getDiscountType())) {

            // ⭐ FIX 2 — BigDecimal → double before division
            discount = cartTotal * (o.getDiscountValue().doubleValue() / 100.0);

        } else if ("FIXED".equals(o.getDiscountType())) {

            // ⭐ FIX 3 — Convert BigDecimal → double
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

    // =============== REMOVE ITEM =================
    public void remove(Integer cartItemId) {
        if (loginBean.getLoggedUser() == null) return;
        customerEJB.removeCartItem(loginBean.getLoggedUser().getUserId(), cartItemId);
        loadCart();
    }
    public void confirmOrder() {
    try {
        for (CartItems ci : cartItems) {
            if (ci.getMedicineId() != null &&
                Boolean.TRUE.equals(ci.getMedicineId().getPrescriptionRequired())) {

                boolean uploaded = customerEJB
                        .hasPrescription(loginBean.getLoggedUser().getUserId(),
                                          ci.getMedicineId().getMedicineId());

                if (!uploaded) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Prescription required. Please upload prescription before placing order.", null));
                    return;
                }
            }
        }

        Integer userId = loginBean.getLoggedUser().getUserId();
        Integer addressId = customerBean.getSelectedAddress().getAddressId();
        Integer offerId = appliedOffer != null ? appliedOffer.getOfferId() : null;

        Orders order = customerEJB.placeOrderFromCart(
                userId, addressId, paymentMethod, offerId
        );

        lastPlacedOrderId = order.getOrderId();

currentOrderId = order.getOrderId();   // ✅ ADD THIS LIN
        loadCart();

        addMessage("Order placed successfully!");

    } catch (Exception e) {
        addMessage("Order failed: " + e.getMessage());
    }
}

    // =============== GETTERS & SETTERS ===============

    public List<CartItems> getCartItems() {
        return cartItems;
    }

    public Cart getActiveCart() {
        return activeCart;
    }

   public double getTotal() {
    return discountedTotal;
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

public Offers getAppliedOffer() {
    return appliedOffer;
}

public double getDiscountedTotal() {
    return discountedTotal;
}public void increase(Integer medicineId) {
    if (medicineId == null) return;
    customerEJB.increaseCartItemQuantity(
        loginBean.getLoggedUser().getUserId(), medicineId
    );
    loadCart();
}

public void decrease(Integer medicineId) {
    if (medicineId == null) return;
    customerEJB.decreaseCartItemQuantity(
        loginBean.getLoggedUser().getUserId(), medicineId
    );
    loadCart();
}


}