package eu.toop.iface.servlet;

import eu.toop.iface.ToopInterfaceManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ToopInterfaceServletDC extends HttpServlet {
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        ToopInterfaceManager.getInterfaceDC().doPost(req, resp);
    }
}
