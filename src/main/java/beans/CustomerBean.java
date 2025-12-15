
package beans;

import ejb.CustomerEJBLocal;
import entity.Addresses;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import org.primefaces.event.SelectEvent;

@Named("customerBean")
@SessionScoped
public class CustomerBean implements Serializable {

    @Inject
    private CustomerEJBLocal customerEJB;
@Inject
private CartBean CartBean;
    @Inject
    private LoginBean loginBean;

    // ---------------------------------------------------------
    // ➤ Address fields
    // ---------------------------------------------------------
    private String street;
    private String city;
    private String state;
    private String zip;

    private Addresses selectedAddress;
    private Integer selectedAddressId;

    // ---------------------------------------------------------
    // ➤ Registration fields (ADDED HERE)
    // ---------------------------------------------------------
    private String username;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;

    // ---------------------------------------------------------
    // ➤ REGISTRATION FUNCTION (ADDED HERE)
    // ---------------------------------------------------------
    @PostConstruct
public void init() {
    List<Addresses> list = getUserAddresses();

    if (list != null && !list.isEmpty()) {
        selectedAddress = list.get(list.size() - 1); // Last address as default
    }
}public Addresses getSelectedAddress() {
    return selectedAddress;
}

public void setSelectedAddress(Addresses selectedAddress) {
    this.selectedAddress = selectedAddress;
}


    public String register() {

        // Password check
        if (!password.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Passwords do not match!", null));
            return null;
        }

        // Call EJB register function
        String result = customerEJB.registerCustomer(username, email, phone, password);

        if (!result.equals("SUCCESS")) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, result, null));
            return null;
        }

        // Redirect to login after successful registration
        return "Login.xhtml?faces-redirect=true&registered=true";
    }

    // ---------------------------------------------------------
    // ➤ Address Management
    // ---------------------------------------------------------

    public List<Addresses> getUserAddresses() {
        if (loginBean.getLoggedUser() == null) return List.of();
        return customerEJB.getAddressesByUserId(loginBean.getLoggedUser().getUserId());
    }

    public void addAddress() {
        if (loginBean.getLoggedUser() == null) return;

        customerEJB.addAddressToCustomer(street, city, state, zip,
                loginBean.getLoggedUser().getUserId());

        clearForm();
    }


// ... inside CustomerBean ...

public void onAddressSelect(SelectEvent<Addresses> event) {
    // Manually set the selected address from the event to ensure the bean is synced
    this.selectedAddress = event.getObject();
    
    // Optional: If you need to perform database updates or session logging 
    // when the user switches addresses, do it here.
    System.out.println("New address selected: " + selectedAddress.getCity());
}
    public void chooseAddress(Integer id) {
        for (Addresses a : getUserAddresses()) {
            if (a.getAddressId().equals(id)) {
                selectedAddress = a;
                selectedAddressId = id;
                break;
            }
        }
    }

    public void clearForm() {
        street = "";
        city = "";
        state = "";
        zip = "";
    }
private List<Addresses> userAddresses;
//private Addresses selectedAddress;
public void ensureAddressSelected() {
    if (selectedAddress == null) {
        if (getUserAddresses() != null && !getUserAddresses().isEmpty()) {
            selectedAddress = getUserAddresses().get(0); // auto-pick first address
        }
    }
}


    // ---------------------------------------------------------
    // ➤ Add to Cart
    // ---------------------------------------------------------
  
public void addToCart(Integer medicineId) {

    System.out.println("🔥 addToCart called, medicineId = " + medicineId);

    if (loginBean.getLoggedUser() == null) {
        System.out.println("❌ User NOT logged in");
        return;
    }

    System.out.println("✅ User ID = " + loginBean.getLoggedUser().getUserId());

    customerEJB.addOrUpdateCartItem(
        loginBean.getLoggedUser().getUserId(),
        medicineId,
        1
    );

    System.out.println("✅ EJB addOrUpdateCartItem called");

    CartBean.loadCart();
}
/// ---------------------------------------------------------
    // ➤ GETTERS & SETTERS
    // ---------------------------------------------------------

    // address getters
//    public Addresses getSelectedAddress() { return selectedAddress; }
    public Integer getSelectedAddressId() { return selectedAddressId; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZip() { return zip; }

    // address setters
    public void setStreet(String v) { street = v; }
    public void setCity(String v) { city = v; }
    public void setState(String v) { state = v; }
    public void setZip(String v) { zip = v; }

    // register setters/getters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
   public int quantity(Integer medicineId) {

    System.out.println("🔁 quantity() called for medicineId = " + medicineId);

    if (medicineId == null) return 0;
    if (loginBean.getLoggedUser() == null) return 0;

    int q = customerEJB.getCartItemQuantity(
        loginBean.getLoggedUser().getUserId(),
        medicineId
    );

    System.out.println("📦 Quantity from DB = " + q);
    return q;
}


public void increase(Integer medicineId) {

    if (loginBean.getLoggedUser() == null) return;

    customerEJB.increaseCartItemQuantity(
        loginBean.getLoggedUser().getUserId(),
        medicineId
    );

    CartBean.loadCart(); // 🔥 REQUIRED
}

public void decrease(Integer medicineId) {

    if (loginBean.getLoggedUser() == null) return;

    customerEJB.decreaseCartItemQuantity(
        loginBean.getLoggedUser().getUserId(),
        medicineId
    );

    CartBean.loadCart(); // 🔥 REQUIRED
}
// package beans;
// ... (imports) ...
// public class CustomerBean implements Serializable {

// ... (other methods) ...

public boolean showAddButton(Integer medicineId) {

    int qty = quantity(medicineId);
    System.out.println("🧠 showAddButton qty = " + qty);

    return qty == 0;
}

// ... (rest of the class) ...

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}