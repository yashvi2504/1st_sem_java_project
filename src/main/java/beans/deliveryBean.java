package beans;

import ejb.DeliveryEJBLocal;
import entity.Delivery;
import entity.DeliveryPartners;
import entity.Users;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("deliveryBean")
@SessionScoped
public class deliveryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DeliveryEJBLocal deliveryEJB;

    @Inject
    private LoginBean loginBean;

    private DeliveryPartners partner;

    // ---------------------------
    // 1️⃣ Get logged delivery partner
    // ---------------------------
    
    public List<DeliveryPartners> getAllPartners() {
    return deliveryEJB.getAllDeliveryPartners();
}

    public DeliveryPartners getLoggedPartner() {

        if (partner == null && loginBean.getLoggedUser() != null) {

            Users u = loginBean.getLoggedUser();

            // find partner by its user
            List<DeliveryPartners> list = deliveryEJB.getAllDeliveryPartners();

for (DeliveryPartners p : list) {
    if (p.getUserId() != null &&
        p.getUserId().getUserId() != null &&
        p.getUserId().getUserId().equals(u.getUserId())) 
    {
        partner = p;
        break;
    }
}

        }

        return partner;
    }

    // ---------------------------
    // 2️⃣ Get deliveries assigned to logged partner
    // ---------------------------
    public List<Delivery> getAssignedOrders() {

        if (getLoggedPartner() == null) {
            return null;
        }

        return deliveryEJB.getDeliveriesByPartner(getLoggedPartner().getDeliveryPartnerId());
    }

    // ---------------------------
    // 3️⃣ Advance Delivery Status
    // ---------------------------
    public void advanceStatus(Integer deliveryId) {

    Delivery d = null;

    for (Delivery x : getAssignedOrders()) {
        if (x.getDeliveryId().equals(deliveryId)) {
            d = x;
            break;
        }
    }

    if (d == null) return;

    String current = d.getStatus();
    String next;

    switch (current) {

        case "Assigned":
            next = "Out for Delivery";
            break;

        case "Out for Delivery":
            next = "Delivered";
            break;

        default:
            next = current;
            break;
    }

    deliveryEJB.updateDeliveryStatus(deliveryId, next);

    // Refresh list
    getAssignedOrders();
}

public Integer getPartnerId() {
    return getLoggedPartner() != null 
            ? getLoggedPartner().getDeliveryPartnerId() 
            : null;
}

public String getUsername() {
    return getLoggedPartner() != null && getLoggedPartner().getUserId() != null
            ? getLoggedPartner().getUserId().getUsername()
            : "N/A";
}

public String getEmail() {
    return getLoggedPartner() != null && getLoggedPartner().getUserId() != null
            ? getLoggedPartner().getUserId().getEmail()
            : "N/A";
}

public String getPhone() {
    return getLoggedPartner() != null && getLoggedPartner().getUserId() != null
            ? getLoggedPartner().getUserId().getPhone()
            : "N/A";
}

public String getVehicleNo() {
    return getLoggedPartner() != null 
            ? getLoggedPartner().getVehicleNo()
            : "N/A";
}

public String getRole() {
    return getLoggedPartner() != null &&
           getLoggedPartner().getUserId() != null &&
           getLoggedPartner().getUserId().getRoleId() != null
            ? getLoggedPartner().getUserId().getRoleId().getRoleName()
            : "N/A";
}

}
