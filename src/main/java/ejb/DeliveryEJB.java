package ejb;

import entity.Delivery;
import entity.DeliveryPartners;
import entity.DeliveryTracking;
import entity.Orders;
import entity.Roles;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class DeliveryEJB implements DeliveryEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

    // 1️⃣ Add new delivery partner
    @Override
    public void addDeliveryPartner(String vehicleNo, Users user) {
        DeliveryPartners partner = new DeliveryPartners();
        partner.setVehicleNo(vehicleNo);
        partner.setUserId(user);
        em.persist(partner);
    }

    @Override
    public List<DeliveryPartners> getAllDeliveryPartners() {
        return em.createNamedQuery("DeliveryPartners.findAll", DeliveryPartners.class).getResultList();
    }

    @Override
    public DeliveryPartners getDeliveryPartnerById(Integer partnerId) {
        return em.find(DeliveryPartners.class, partnerId);
    }

    // 2️⃣ Assign delivery partner to an order
    @Override
    public void assignDeliveryPartner(Integer orderId, Integer partnerId, String deliveryAddress) {
        Orders order = em.find(Orders.class, orderId);
        DeliveryPartners partner = em.find(DeliveryPartners.class, partnerId);

        if (order != null && partner != null) {
            Delivery delivery = new Delivery();
            delivery.setOrderId(order);
            delivery.setDeliveryPartnerId(partner);
            delivery.setDeliveryAddress(deliveryAddress);
            delivery.setStatus("Assigned");

            em.persist(delivery);

            // Create tracking entry
            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setDeliveryId(delivery);
            tracking.setStatus("Assigned");
            tracking.setUpdateTime(new Date());
            em.persist(tracking);
        }
    }

    // 3️⃣ Update delivery status
    @Override
    public void updateDeliveryStatus(Integer deliveryId, String status) {
        Delivery delivery = em.find(Delivery.class, deliveryId);
        if (delivery != null) {
            delivery.setStatus(status);
            em.merge(delivery);

            // Add tracking entry
            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setDeliveryId(delivery);
            tracking.setStatus(status);
            tracking.setUpdateTime(new Date());
            em.persist(tracking);
        }
    }

    // 4️⃣ Get deliveries
    @Override
    public List<Delivery> getAllDeliveries() {
        return em.createNamedQuery("Delivery.findAll", Delivery.class).getResultList();
    }

    @Override
    public List<Delivery> getDeliveriesByPartner(Integer partnerId) {
        return em.createQuery(
            "SELECT d FROM Delivery d WHERE d.deliveryPartnerId.deliveryPartnerId = :pid",
            Delivery.class)
            .setParameter("pid", partnerId)
            .getResultList();
    }

    @Override
    public List<DeliveryTracking> getTrackingHistory(Integer deliveryId) {
        return em.createQuery(
            "SELECT t FROM DeliveryTracking t WHERE t.deliveryId.deliveryId = :did ORDER BY t.updateTime ASC",
            DeliveryTracking.class)
            .setParameter("did", deliveryId)
            .getResultList();
    }
    @Override
public void createUser(Users u) {
    em.persist(u);
}

@Override
public Roles getRole(String roleName) {
    return em.createNamedQuery("Roles.findByRoleName", Roles.class)
             .setParameter("roleName", roleName)
             .getSingleResult();
}

}