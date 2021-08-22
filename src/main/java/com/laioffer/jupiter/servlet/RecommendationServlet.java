package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.recommendation.ItemRecommender;
import com.laioffer.jupiter.recommendation.RecommendationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RecommendationServlet", value = "/recommendation")
public class RecommendationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        ItemRecommender itemRecommender = new ItemRecommender();
        HttpSession session = request.getSession(false);
        try {
            if (session == null) {
                itemMap = itemRecommender.recommendItemByDefault();
            } else {
                String userId = (String)session.getAttribute("user_id");
                itemMap = itemRecommender.recommendItemByUser(userId);
            }
        } catch (RecommendationException e) {
            throw new ServletException(e);
        }
        response.getWriter().print(new ObjectMapper().writeValueAsString(itemMap));
    }

}
