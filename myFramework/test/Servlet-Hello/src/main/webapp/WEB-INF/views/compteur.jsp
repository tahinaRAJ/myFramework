<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<html>
<body>
    <h2>Compteur partagé</h2>
    <p>Compteur : <strong><%= ((List<?>) request.getAttribute("compteur")).get(0) %></strong></p>
    <p>Instance du controller : <%= ((List<?>) request.getAttribute("instance")).get(0) %></p>
    <p>Votre session : <%= ((List<?>) request.getAttribute("sessionId")).get(0) %></p>
    <p><a href="">Actualiser</a></p>
    <p>Ouvrez cette page dans un autre navigateur : la session change, mais pas l'instance, et le compteur continue.</p>
</body>
</html>
