package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet("/PrescriptionServlet")
public class PrescriptionServlet extends HttpServlet {

    private static final String BASE_PATH = "D:/java/yasi/prescriptions/";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String file = req.getParameter("file");

        if (file == null || file.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File parameter missing");
            return;
        }

        // SECURITY: prevent ../ access
        file = file.replace("..", "");

        File f = new File(BASE_PATH + file);

        if (!f.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        String mime = getServletContext().getMimeType(f.getName());
        resp.setContentType(mime != null ? mime : "application/octet-stream");

        try (FileInputStream in = new FileInputStream(f);
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int len;

            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
