<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Produits</title>
</head>
<body>
    <h2>Liste des produits</h2>

    <table border="1" cellpadding="6">
        <tr>
            <th>Id</th>
            <th>Nom</th>
            <th>Prix</th>
            <th></th>
        </tr>
        <c:forEach var="p" items="${products}">
            <tr>
                <td>${p.id}</td>
                <td>${p.name}</td>
                <td>${p.price}</td>
                <td>
                    <form action="${pageContext.request.contextPath}/products/delete" method="post" style="margin:0">
                        <input type="hidden" name="id" value="${p.id}" />
                        <button type="submit">Supprimer</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>

    <h3>Ajouter un produit</h3>
    <form action="${pageContext.request.contextPath}/products/add" method="post">
        Nom : <input type="text" name="name" required />
        Prix : <input type="number" step="0.01" name="price" required />
        <button type="submit">Ajouter</button>
    </form>
</body>
</html>
