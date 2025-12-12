package rest;

import ejb.DeliveryEJBLocal;
import entity.Users;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("delivery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DeliveryREST {

    @EJB
    private DeliveryEJBLocal deliveryEJB;

    // 1️⃣ Add a Delivery Partner
    @POST
    @Path("/add-partner")
    public String addPartner(@QueryParam("vehicleNo") String vehicleNo, @QueryParam("userId") Integer userId) {
        Users user = new Users();
        user.setUserId(userId);
        deliveryEJB.addDeliveryPartner(vehicleNo, user);
        return "Delivery partner added successfully.";
    }

    // 2️⃣ Get all delivery partners
    @GET
    @Path("/partners")
    public List getAllPartners() {
        return deliveryEJB.getAllDeliveryPartners();
    }

    // 3️⃣ Assign Delivery Partner to Order
    @POST
    @Path("/assign")
    public String assignDelivery(@QueryParam("orderId") Integer orderId,
                                 @QueryParam("partnerId") Integer partnerId,
                                 @QueryParam("address") String address) {
        deliveryEJB.assignDeliveryPartner(orderId, partnerId, address);
        return "Delivery assigned successfully.";
    }

    // 4️⃣ Update Delivery Status
    @PUT
    @Path("/update-status")
    public String updateStatus(@QueryParam("deliveryId") Integer deliveryId,
                               @QueryParam("status") String status) {
        deliveryEJB.updateDeliveryStatus(deliveryId, status);
        return "Delivery status updated to: " + status;
    }

    // 5️⃣ Get all deliveries
    @GET
    @Path("/all")
    public List getAllDeliveries() {
        return deliveryEJB.getAllDeliveries();
    }

    // 6️⃣ Get deliveries by partner
    @GET
    @Path("/partner/{id}")
    public List getByPartner(@PathParam("id") Integer id) {
        return deliveryEJB.getDeliveriesByPartner(id);
    }

    // 7️⃣ Get delivery tracking history
    @GET
    @Path("/tracking/{deliveryId}")
    public List getTracking(@PathParam("deliveryId") Integer deliveryId) {
        return deliveryEJB.getTrackingHistory(deliveryId);
    }
}
