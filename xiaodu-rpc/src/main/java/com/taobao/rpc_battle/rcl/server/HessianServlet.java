package com.taobao.rpc_battle.rcl.server;

import org.springframework.remoting.caucho.HessianServiceExporter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Date: 12/26/12
 *
 * @author shutong.dy
 */
public class HessianServlet extends HttpServlet
{
    HessianServiceExporter exporter = null;

    public HessianServlet(HessianServiceExporter exporter) {
        this.exporter = exporter;

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        exporter.handleRequest(request, response);
    }
}