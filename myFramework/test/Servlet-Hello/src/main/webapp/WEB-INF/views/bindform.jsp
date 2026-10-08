<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Exemples de binding</title></head>
<body>
  <h2>Exemples de binding (simple)</h2>
  <% String ctx = request.getContextPath(); %>

  <h3>1. Objet Product (POST /bind/product)</h3>
  <form action="<%= ctx %>/bind/product" method="post">
    name <input name="name" value="Clavier"> price <input name="price" value="29.9">
    <button>Envoyer</button>
  </form>

  <h3>2. request + objet (POST /bind/mix)</h3>
  <form action="<%= ctx %>/bind/mix" method="post">
    name <input name="name" value="Souris"> price <input name="price" value="14.5">
    <button>Envoyer</button>
  </form>
</body>
</html>
