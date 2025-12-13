package servlets;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import ejb.AdminEJBLocal;
import entity.Orders;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@WebServlet("/admin/weekly-report")
public class WeeklyReportServlet extends HttpServlet {

    @EJB
    private AdminEJBLocal adminEJB;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=Weekly_Sales_Report.pdf"
        );

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());

            document.open();

            /* -------- TITLE -------- */
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Logic Pharmacy - Weekly Sales Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Generated On: " + new Date()));
            document.add(new Paragraph(" "));

            /* -------- SUMMARY -------- */
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

//            Double weeklyIncome = adminEJB.getWeeklyIncome();
BigDecimal weeklyIncome = adminEJB.getWeeklyIncome();

            List<Object[]> orderCount = adminEJB.getOrderCountPerDay();

            long totalOrders = 0;
            for (Object[] row : orderCount) {
                totalOrders += (Long) row[1];
            }

            document.add(new Paragraph("Summary", bold));
            document.add(new Paragraph("Total Orders: " + totalOrders));
            document.add(new Paragraph("Total Income: ₹ " + weeklyIncome));
            document.add(new Paragraph(" "));
/* -------- MOST SOLD MEDICINE -------- */
List<Object[]> topMedicine = adminEJB.getMostSoldMedicine();

if (topMedicine != null && !topMedicine.isEmpty()) {
    Object[] row = topMedicine.get(0);
    String medicineName = row[0].toString();
    Long quantity = (Long) row[1];

    document.add(
        new Paragraph(
            "Most Sold Medicine: " + medicineName + " (" + quantity + " units)",
            bold
        )
    );
}

document.add(new Paragraph(" "));
            /* -------- ORDERS TABLE -------- */
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            table.addCell("Order ID");
            table.addCell("Date");
            table.addCell("Status");
            table.addCell("Items");   // ⭐ NEW
            table.addCell("Total");
            table.addCell("Payment");
            table.addCell("Address");

            // ⚠️ IMPORTANT:
            // Replace this with your admin method if you have getAllOrders()
          List<Orders> orders = adminEJB.getAllOrders();
if (orders != null && !orders.isEmpty()) {

   for (Orders o : orders) {

    table.addCell(String.valueOf(o.getOrderId()));
    table.addCell(String.valueOf(o.getOrderDate()));
    table.addCell(o.getStatus());

    /* -------- ITEMS COLUMN -------- */
    StringBuilder items = new StringBuilder();

    if (o.getOrderItemsCollection() != null) {
        o.getOrderItemsCollection().forEach(oi -> {
            items.append(oi.getMedicineId().getName())
                 .append(" x ")
                 .append(oi.getQuantity())
                 .append("\n");
        });
    }

    table.addCell(items.toString()); // ⭐ ITEMS SHOWN HERE

    table.addCell("₹ " + o.getTotalAmount());
    table.addCell(o.getPaymentMethod());

    String address = o.getAddressId().getCity() + ", "
                   + o.getAddressId().getState();
    table.addCell(address);
}

}


            document.add(table);

            document.close();

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
