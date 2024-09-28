package com.example.coffeeOrderService.common.util;

import com.example.coffeeOrderService.model.order.Order;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtForOrderUtil {

    private static final String SECRET_KEY = "mysecretkey";

    public static String generateOrderToken(Order order) {
        return Jwts.builder()
                .claim("orderId", order.getOrderId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))  // 1시간 유효
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Long getOrderIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("orderId", Long.class);
    }
}
