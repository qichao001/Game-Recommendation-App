package com.laioffer.jupiter.servlet;

import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "RegisterServlet", value = "/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.readRequestBody(User.class, request);
        try {
            user.setPassword(ServletUtil.encryptPassword(user.getUserId(), user.getPassword()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        MySQLConnection connection = new MySQLConnection();
        boolean isAdded = connection.addUser(user);
        if (!isAdded) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        connection.close();
    }
}
