package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.entity.LoginRequestBody;
import com.laioffer.jupiter.entity.LoginResponseBody;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        MySQLConnection connection = new MySQLConnection();
        String userId = body.getUserId();
        String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
        String userName = connection.checkUser(userId, password);
        connection.close();

        if (userName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            // create a session
            HttpSession session = request.getSession();
            session.setAttribute("user_id", body.getUserId());
            session.setAttribute("user_name", userName);
            session.setMaxInactiveInterval(600);

            LoginResponseBody responseBody = new LoginResponseBody(userId, userName);
            response.getWriter().print(new ObjectMapper().writeValueAsString(responseBody));
//            Cookie cookie = new Cookie("JSESSION", session.getId());
//            cookie.setHttpOnly(true);
//            response.addCookie(cookie);
        }
    }
}
