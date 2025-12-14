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
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.optionconfig.title.Title;
import jakarta.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.primefaces.model.charts.bar.BarChartOptions;

@Named("deliveryBean")
@SessionScoped
public class deliveryBean implements Serializable {
private BarChartModel deliveredPerDayChart;

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
public BarChartModel getDeliveredPerDayChart() {

    if (deliveredPerDayChart == null) {
        createDeliveredChart();
    }

    return deliveredPerDayChart;
}public void createDeliveredChart() {

    deliveredPerDayChart = new BarChartModel();

    // SAFETY: user may not be logged in yet
    if (loginBean == null || loginBean.getLoggedUser() == null) {
        return;
    }

    ChartData data = new ChartData();
    BarChartDataSet dataset = new BarChartDataSet();
    dataset.setLabel("Delivered Orders");

    List<Number> values = new ArrayList<>();
    List<String> labels = new ArrayList<>();

    DeliveryPartners loggedPartner = getLoggedPartner();
    if (loggedPartner != null) {

        List<Object[]> result =
            deliveryEJB.getDeliveredCountPerDay(loggedPartner.getDeliveryPartnerId());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");

        for (Object[] row : result) {
            if (row[0] != null && row[1] != null) {
                labels.add(sdf.format((Date) row[0]));
                values.add((Long) row[1]);
            }
        }
    }

    dataset.setData(values);
    data.addChartDataSet(dataset);
    data.setLabels(labels);
    deliveredPerDayChart.setData(data);

    // ✅ THIS WAS MISSING
    BarChartOptions options = new BarChartOptions();
    Title title = new Title();
    title.setDisplay(true);
    title.setText("Delivered Orders Per Day");
    options.setTitle(title);

    deliveredPerDayChart.setOptions(options);
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