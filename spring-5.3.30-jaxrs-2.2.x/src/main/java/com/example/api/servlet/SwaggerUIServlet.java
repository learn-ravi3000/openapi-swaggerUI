package com.example.api.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SwaggerUIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        // Default to index.html if no specific file requested
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            response.setContentType("text/html");
            response.getWriter().write(getSwaggerUIHtml(request));
            return;
        }
        
        // Serve static resources from webjars
        String resourcePath = "/META-INF/resources/webjars/swagger-ui/5.10.3" + pathInfo;
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Set content type based on file extension
        String contentType = getServletContext().getMimeType(pathInfo);
        if (contentType != null) {
            response.setContentType(contentType);
        }
        
        // Copy resource to response
        try (OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
        }
    }
    
    private String getSwaggerUIHtml(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String openApiUrl = contextPath + "/api/openapi.json";
        
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>API Documentation</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"" + contextPath + "/swagger-ui/swagger-ui.css\" />\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"" + contextPath + "/swagger-ui/index.css\" />\n" +
                "    <link rel=\"icon\" type=\"image/png\" href=\"" + contextPath + "/swagger-ui/favicon-32x32.png\" sizes=\"32x32\" />\n" +
                "    <link rel=\"icon\" type=\"image/png\" href=\"" + contextPath + "/swagger-ui/favicon-16x16.png\" sizes=\"16x16\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"swagger-ui\"></div>\n" +
                "    <script src=\"" + contextPath + "/swagger-ui/swagger-ui-bundle.js\" charset=\"UTF-8\"></script>\n" +
                "    <script src=\"" + contextPath + "/swagger-ui/swagger-ui-standalone-preset.js\" charset=\"UTF-8\"></script>\n" +
                "    <script>\n" +
                "        window.onload = function() {\n" +
                "            window.ui = SwaggerUIBundle({\n" +
                "                url: \"" + openApiUrl + "\",\n" +
                "                dom_id: '#swagger-ui',\n" +
                "                deepLinking: true,\n" +
                "                presets: [\n" +
                "                    SwaggerUIBundle.presets.apis,\n" +
                "                    SwaggerUIStandalonePreset\n" +
                "                ],\n" +
                "                plugins: [\n" +
                "                    SwaggerUIBundle.plugins.DownloadUrl\n" +
                "                ],\n" +
                "                layout: \"StandaloneLayout\"\n" +
                "            });\n" +
                "        };\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
