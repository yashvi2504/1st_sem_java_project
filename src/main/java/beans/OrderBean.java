

package beans;

import ejb.CustomerEJBLocal;
import ejb.DeliveryEJBLocal;
import entity.DeliveryPartners;
import entity.Orders;
import entity.Prescription;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import org.primefaces.model.file.UploadedFile;

@Named("orderBean")
@ViewScoped
public class OrderBean implements Serializable {

    @Inject
    private CustomerEJBLocal customerEJB;

    @Inject
    private LoginBean loginBean;
@Inject
DeliveryEJBLocal deliveryEJB;
private List<Prescription> prescriptions;

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
            
    prescriptions = customerEJB.getPrescriptionsByUser(selectedOrder.getUserId().getUserId());

        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void addMessage(String msg) {
    FacesContext.getCurrentInstance().addMessage(
        null,
        new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null)
    );
}

public List<Orders> getFilteredOrderList() {

    if (orderList == null || orderList.isEmpty()) {
        return orderList;
    }

    if (selectedStatus == null || selectedStatus.equals("ALL")) {
        return orderList;
    }

    return orderList.stream()
            .filter(o -> o.getStatus() != null &&
                         o.getStatus().equalsIgnoreCase(selectedStatus))
            .collect(Collectors.toList()); // ✅ WORKS IN JAVA 8+
}

//    private List<Orders> orderList;
private String selectedStatus = "ALL";
public String getSelectedStatus() {
    return selectedStatus;
}
public Prescription getPrescriptionByOrder(Integer orderId, Integer medicineId) {
    if (orderId == null || medicineId == null) return null;

    return customerEJB.getPrescriptionsByOrder(orderId)
            .stream()
            .filter(p -> p.getMedicineId() != null &&
                         p.getMedicineId().getMedicineId().equals(medicineId))
            .findFirst()
            .orElse(null);
}

public void setSelectedStatus(String selectedStatus) {
    this.selectedStatus = selectedStatus;
}
private UploadedFile uploadedPrescription;
private Integer uploadMedicineId;
private String popupMessage;
private boolean showPopup;
//private List<Prescription> prescriptions;
public void uploadPrescription() {

    try {
        if (selectedOrder == null || selectedOrder.getOrderId() == null) {
            addMessage("Order not found.");
            return;
        }

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
                selectedOrder.getOrderId(),
                uploadMedicineId,
                fileName,
                uploadedPrescription.getContentType()
        );

        customerEJB.updateOrderStatus(
                selectedOrder.getOrderId(),
                "WAITING_APPROVAL"
        );

        prescriptions = customerEJB.getPrescriptionsByOrder(
                selectedOrder.getOrderId()
        );

        uploadedPrescription = null;
        uploadMedicineId = null;

        popupMessage = "Prescription uploaded successfully!";
        showPopup = true;

    } catch (Exception e) {
        e.printStackTrace();
        addMessage("Upload failed!");
    }
}

public Prescription getPrescription(Integer medicineId) {

    if (prescriptions == null || medicineId == null) {
        return null;
    }

    return prescriptions.stream()
        .filter(p ->
            p.getMedicineId() != null &&              // ✅ NULL SAFE
            p.getMedicineId().getMedicineId() != null &&
            p.getMedicineId().getMedicineId().equals(medicineId)
        )
        .findFirst()
        .orElse(null);
}
//@Inject
//private DeliveryEJBLocal deliveryEJB;
public DeliveryPartners assignedPartner(Integer orderId) {
    return deliveryEJB.findPartnerByOrderId(orderId);
}

public String getPrescriptionStatus(Integer orderId) {
    return customerEJB.getPrescriptionStatusByOrder(orderId);
}

public String getPopupMessage() {
    return popupMessage;
}

public boolean isShowPopup() {
    return showPopup;
}
public Prescription getPrescription(Integer orderId, Integer medicineId) {

    return customerEJB.getPrescriptionByOrderAndMedicine(orderId, medicineId);
}


public void approvePrescription(Integer medicineId) {

    customerEJB.updatePrescriptionStatus(
        selectedOrder.getUserId().getUserId(),
        medicineId,
        "APPROVED"
    );

    // 🔴 REQUIRED: reload prescriptions so UI updates
    prescriptions = customerEJB.getPrescriptionsByUser(
        selectedOrder.getUserId().getUserId()
    );

    popupMessage = "✅ Prescription Approved Successfully!";
    showPopup = true;
}
public void rejectPrescription(Integer medicineId) {

    customerEJB.updatePrescriptionStatus(
        selectedOrder.getUserId().getUserId(),
        medicineId,
        "REJECTED"
    );

    // 🔴 REQUIRED
    prescriptions = customerEJB.getPrescriptionsByUser(
        selectedOrder.getUserId().getUserId()
    );

    popupMessage = "❌ Prescription Rejected!";
    showPopup = true;
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
