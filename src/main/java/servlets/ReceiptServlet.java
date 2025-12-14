package servlets;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import ejb.CustomerEJBLocal;
import entity.OrderItems;
import entity.Orders;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@WebServlet("/ReceiptServlet")
public class ReceiptServlet extends HttpServlet {

    @Inject
    private CustomerEJBLocal customerEJB;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null) {
            response.getWriter().write("Order ID missing!");
            return;
        }

        int orderId = Integer.parseInt(orderIdStr);

        // ✅ Load order from DB
        Orders order = customerEJB.getOrderById(orderId);

        if (order == null) {
            response.getWriter().write("Order not found!");
            return;
        }

        // ✅ PDF response settings
        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=Receipt_Order_" + orderId + ".pdf"
        );

        try {
            Document pdf = new Document();
            PdfWriter.getInstance(pdf, response.getOutputStream());
            pdf.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font textFont  = new Font(Font.FontFamily.HELVETICA, 12);

            // ---------------- TITLE ----------------
            Paragraph title = new Paragraph("Logic Pharmacy - Order Receipt", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            pdf.add(title);
            pdf.add(new Paragraph("\n"));

            // ---------------- ORDER INFO ----------------
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

            pdf.add(new Paragraph("Order ID: " + order.getOrderId(), textFont));
            pdf.add(new Paragraph("Order Date: " + sdf.format(order.getOrderDate()), textFont));
            pdf.add(new Paragraph("Customer: " + order.getUserId().getUsername(), textFont));
            pdf.add(new Paragraph("Email: " + order.getUserId().getEmail(), textFont));
            pdf.add(new Paragraph("\n"));

            // ---------------- ITEMS TABLE ----------------
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            table.addCell("Medicine");
            table.addCell("Quantity");
            table.addCell("Price");
            table.addCell("Total");

            for (OrderItems item : order.getOrderItemsCollection()) {

                BigDecimal lineTotal =
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

                table.addCell(item.getMedicineId().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell("₹" + item.getPrice());
                table.addCell("₹" + lineTotal);
            }

            pdf.add(table);

            // ---------------- GRAND TOTAL (FROM DB) ----------------
            pdf.add(new Paragraph("\n"));
            pdf.add(new Paragraph(
                    "Grand Total: ₹" + order.getTotalAmount(),
                    titleFont
            ));

            pdf.add(new Paragraph(
                    "\n\nThank you for shopping with Logic Pharmacy!",
                    textFont
            ));

            pdf.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
