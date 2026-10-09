<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Exemples de binding</title></head>
<body>
  <h2>Exemples de binding</h2>
  <% String ctx = request.getContextPath(); %>

  <h3>1. Objet Product (POST /bind/product)</h3>
  <form action="<%= ctx %>/bind/product" method="post">
    name <input name="name" value="Clavier"> price <input name="price" value="29.9">
    <button>Envoyer</button>
  </form>

  <h3>2. Paramètres simples (GET /bind/hello)</h3>
  <form action="<%= ctx %>/bind/hello" method="get">
    name <input name="name" value="Rina"> age <input name="age" value="20">
    vip <input name="vip" value="true">
    <button>Envoyer</button>
  </form>

  <h3>3. Somme (GET /bind/sum)</h3>
  <form action="<%= ctx %>/bind/sum" method="get">
    a <input name="a" value="3"> b <input name="b" value="4"> <button>Calculer</button>
  </form>

  <h3>4. LocalDate + enum, réponse JSON (POST /bind/event)</h3>
  <form action="<%= ctx %>/bind/event" method="post">
    title <input name="title" value="Soirée"> attendees <input name="attendees" value="50">
    date <input type="date" name="date" value="2026-10-31">
    level <select name="level"><option>LOW</option><option>MEDIUM</option><option selected>HIGH</option></select>
    <button>Envoyer</button>
  </form>

  <h3>5. Mélange request + objet + simple (POST /bind/mix)</h3>
  <form action="<%= ctx %>/bind/mix" method="post">
    name <input name="name" value="Souris"> price <input name="price" value="14.5">
    quantity <input name="quantity" value="3"> <button>Envoyer</button>
  </form>
</body>
</html>
