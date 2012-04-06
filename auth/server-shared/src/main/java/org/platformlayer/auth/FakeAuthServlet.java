package org.platformlayer.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FakeAuthServlet extends HttpServlet {
    private static final long serialVersionUID = -8270345309937119194L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // GET /v1.0 HTTP/1.1
        // Host: auth.api.rackspacecloud.com
        // X-Auth-User: jdoe
        // X-Auth-Key: a86850deb2742ec3cb41518e26aa2d89

        String user = req.getHeader("X-Auth-User");
        String secret = req.getHeader("X-Auth-Key");

        if (user != null && user.startsWith("USER-")) {
            int userId = Integer.parseInt(user.substring(5));
            String correctSecret = "SECRET-" + userId;
            if (correctSecret.equals(secret)) {
                sendCorrectAuth(userId, resp);
                return;
            }
        }

        // Return 401 unauthorized
        resp.setStatus(401);
    }

    void sendCorrectAuth(int userId, HttpServletResponse response) {
        /*
         * If authentication is successful, an HTTP status 204 No Content is returned with three cloud service headers, X-Server-Management-Url, X-Storage-Url, X-CDN-Management-Url, as well as
         * X-Auth-Token
         */

        String xaasUrl = "http://127.0.0.1:8082/" + userId;

        String authToken = "DEV-TOKEN-" + userId;
        response.setHeader("X-Auth-Token", authToken);

        response.setHeader("X-Server-Management-Url", "");
        response.setHeader("X-Storage-Url", "");
        response.setHeader("X-CDN-Management-Url", "");
        response.setHeader("X-PlatformLayer-Url", xaasUrl);

        response.setStatus(204);
    }
}
