<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
    <h2>WorldTest View</h2>
    <p>test  : <%= request.getAttribute("map") != null ? ((java.util.Map)request.getAttribute("map")).get("test")  : "" %></p>
    <p>test2 : <%= request.getAttribute("map") != null ? ((java.util.Map)request.getAttribute("map")).get("test2") : "" %></p>
    <p>test3 : <%= request.getAttribute("map") != null ? ((java.util.Map)request.getAttribute("map")).get("test3") : "" %></p>
</body>
</html>
