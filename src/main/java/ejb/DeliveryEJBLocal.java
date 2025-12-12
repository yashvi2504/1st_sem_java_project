package ejb;

import entity.Delivery;
import entity.DeliveryPartners;
import entity.DeliveryTracking;
import entity.Orders;
import entity.Roles;
import entity.Users;
import jakarta.ejb.Local;
import java.util.List;
import java.util.Date;

@Local
public interface DeliveryEJBLocal {

    // 1️⃣ Manage Delivery Partners
    void addDeliveryPartner(String vehicleNo, Users user);
    List<DeliveryPartners> getAllDeliveryPartners();
    DeliveryPartners getDeliveryPartnerById(Integer partnerId);

    // 2️⃣ Assign a partner to an order
    void assignDeliveryPartner(Integer orderId, Integer partnerId, String deliveryAddress);

    // 3️⃣ Update delivery status
    void updateDeliveryStatus(Integer deliveryId, String status);

    // 4️⃣ View deliveries
    List<Delivery> getAllDeliveries();
    List<Delivery> getDeliveriesByPartner(Integer partnerId);
    List<DeliveryTracking> getTrackingHistory(Integer deliveryId);
    void createUser(Users u);
Roles getRole(String roleName);

}
