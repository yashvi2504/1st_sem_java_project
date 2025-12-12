package beans;

import ejb.CustomerEJBLocal;
import ejb.DeliveryEJBLocal;
import entity.Orders;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Named("orderBean")
@ViewScoped
public class OrderBean implements Serializable {

    @Inject
    private CustomerEJBLocal customerEJB;

    @Inject
    private LoginBean loginBean;
@Inject
DeliveryEJBLocal deliveryEJB;

    private List<Orders> orderList;  // All orders
    private Orders selectedOrder;    // Single order details

 @PostConstruct
public void init() {

    // If Admin → load all orders
    if (loginBean.getLoggedUser() != null 
            && loginBean.getLoggedUser().getRoleId().getRoleName().equalsIgnoreCase("Admin")) {

        orderList = customerEJB.getAllOrders();
    }
    else {
        // Normal customer → load only their orders
        if (loginBean.getLoggedUser() != null) {
            orderList = customerEJB.getOrderHistory(loginBean.getLoggedUser().getUserId());
        }
    }

    // Load selected order if orderId exists
    try {
        String orderIdParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("orderId");

        if (orderIdParam != null) {
            Integer orderId = Integer.valueOf(orderIdParam);
            selectedOrder = customerEJB.getOrderById(orderId);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
    
    public void updateStatus() {
    try {
        if (selectedOrder != null) {
            customerEJB.updateOrderStatus(
                    selectedOrder.getOrderId(),
                    selectedOrder.getStatus()
            );

            FacesContext.getCurrentInstance()
                    .addMessage(null, 
                        new FacesMessage("Order status updated successfully!")
                    );
        }
    } catch (Exception e) {
        FacesContext.getCurrentInstance()
                .addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error updating status: " + e.getMessage(), null));
    }
}

    public List<Orders> getOrderList() {
        return orderList;
    }

    public Orders getSelectedOrder() {
        return selectedOrder;
    }
    private Integer selectedPartnerId; 
public Integer getSelectedPartnerId() { return selectedPartnerId; }
public void setSelectedPartnerId(Integer id) { this.selectedPartnerId = id; }


public void assignDeliveryPartner() {

    if (selectedPartnerId == null || selectedOrder == null) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "Please select a delivery partner!", null));
        return;
    }

    deliveryEJB.assignDeliveryPartner(
            selectedOrder.getOrderId(),
            selectedPartnerId,
            selectedOrder.getAddressId().getAddressLine()
    );

    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage("Delivery Partner Assigned Successfully!"));
}
    public void cancelOrder(Integer orderId) {
    try {
        customerEJB.updateOrderStatus(orderId, "Cancelled");

        FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO, 
                "Order Cancelled Successfully!", null));

        // Refresh list
        if (loginBean.getLoggedUser().getRoleId().getRoleName().equalsIgnoreCase("Admin")) {
            orderList = customerEJB.getAllOrders();
        } else {
            orderList = customerEJB.getOrderHistory(loginBean.getLoggedUser().getUserId());
        }

    } catch (Exception e) {
        FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, 
                "Cancel Failed: " + e.getMessage(), null));
    }
}

}