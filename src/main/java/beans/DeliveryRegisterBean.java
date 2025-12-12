package beans;

import ejb.DeliveryEJBLocal;
import entity.Users;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("deliveryRegisterBean")
@RequestScoped
public class DeliveryRegisterBean {

    @Inject
    private DeliveryEJBLocal deliveryEJB;

    private String username;
    private String email;
    private String phone;
    private String password;
    private String vehicleNo;
    private String statusMessage;

    // getters / setters for all fields
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    // called by the form
    public void registerPartner() {
        try {
            // 1) create user entity and assign role
            Users u = new Users();
            u.setUsername(username);
            u.setEmail(email);
            u.setPhone(phone);
            u.setPassword(password);

            // Make sure role "DELIVERY" exists, deliveryEJB.getRole must return it
            u.setRoleId(deliveryEJB.getRole("DELIVERY"));

            // Persist user and partner
            deliveryEJB.createUser(u);              // create user
            deliveryEJB.addDeliveryPartner(vehicleNo, u); // create partner row

            statusMessage = "Registration successful!";
            // optionally clear fields
            username = email = phone = password = vehicleNo = null;

        } catch (Exception ex) {
            statusMessage = "Registration failed: " + ex.getMessage();
            ex.printStackTrace();
        }
    }
}
