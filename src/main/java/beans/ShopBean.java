package beans;

import ejb.CustomerEJBLocal;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("shopBean")
@SessionScoped
public class ShopBean implements Serializable {

    @Inject
    private CustomerEJBLocal customerEJB;

    @Inject
    private LoginBean loginBean;

    @Inject
    private CartBean cartBean;

    // =========================
    // ADD TO CART
    // =========================
    public void addToCart(Integer medicineId) {

        if (loginBean.getLoggedUser() == null || medicineId == null) {
            return;
        }

        customerEJB.addOrUpdateCartItem(
            loginBean.getLoggedUser().getUserId(),
            medicineId,
            1
        );

        cartBean.loadCart();
    }

    // =========================
    // QUANTITY
    // =========================
    public int quantity(Integer medicineId) {

        if (loginBean.getLoggedUser() == null || medicineId == null) {
            return 0;
        }

        try {
            return customerEJB.getCartItemQuantity(
                loginBean.getLoggedUser().getUserId(),
                medicineId
            );
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================
    // INCREASE
    // =========================
    public void increase(Integer medicineId) {

        if (loginBean.getLoggedUser() == null || medicineId == null) return;

        customerEJB.increaseCartItemQuantity(
            loginBean.getLoggedUser().getUserId(),
            medicineId
        );

        cartBean.loadCart();
    }

    // =========================
    // DECREASE
    // =========================
    public void decrease(Integer medicineId) {

        if (loginBean.getLoggedUser() == null || medicineId == null) return;

        customerEJB.decreaseCartItemQuantity(
            loginBean.getLoggedUser().getUserId(),
            medicineId
        );

        cartBean.loadCart();
    }

    // =========================
    // SHOW ADD BUTTON LOGIC
    // =========================
    public boolean showAddButton(Integer medicineId) {

        if (medicineId == null) return false;

        // Not logged in → show Add button
        if (loginBean.getLoggedUser() == null) {
            return true;
        }

        // Logged in → show Add only if qty = 0
        return quantity(medicineId) == 0;
    }
}
