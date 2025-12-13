package beans;

import ejb.AdminEJBLocal;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.*;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;

@Named("dashboardBean")
@ViewScoped
public class AdminDashboardBean implements Serializable {

    @Inject
    private AdminEJBLocal adminEJB;

    private BarChartModel ordersChart;

    @PostConstruct
    public void init() {
        loadOrdersChart();
    }

    private void loadOrdersChart() {

        ordersChart = new BarChartModel();
        ChartData data = new ChartData();

        // Fixed order of days
        Map<String, Integer> days = new LinkedHashMap<>();
        days.put("Monday", 0);
        days.put("Tuesday", 0);
        days.put("Wednesday", 0);
        days.put("Thursday", 0);
        days.put("Friday", 0);
        days.put("Saturday", 0);
        days.put("Sunday", 0);

        // DB values
        List<Object[]> result = adminEJB.getOrderCountPerDay();

        for (Object[] row : result) {
            String day = (String) row[0];
            Long count = (Long) row[1];
            days.put(day, count.intValue());
        }

        BarChartDataSet dataset = new BarChartDataSet();
        dataset.setLabel("Total Orders Placed");
        dataset.setData(new ArrayList<>(days.values()));
        dataset.setBackgroundColor("#01756e");

        data.setLabels(new ArrayList<>(days.keySet()));
        data.addChartDataSet(dataset);

        ordersChart.setData(data);
    }

    public BarChartModel getOrdersChart() {
        return ordersChart;
    }
}   