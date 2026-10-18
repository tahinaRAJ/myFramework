<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Exemples d'upload</title></head>
<body>
  <h2>Exemples d'upload</h2>
  <% String ctx = request.getContextPath(); %>
  <p>Important : chaque formulaire doit avoir <code>enctype="multipart/form-data"</code> et <code>method="post"</code>.</p>

  <h3>1. Un fichier (POST /upload/one) - paramètre Java : FileUpload photo</h3>
  <form action="<%= ctx %>/upload/one" method="post" enctype="multipart/form-data">
    <input type="file" name="photo"> <button>Envoyer</button>
  </form>

  <h3>2. Plusieurs fichiers (POST /upload/many) - paramètre Java : FileUpload[] docs</h3>
  <form action="<%= ctx %>/upload/many" method="post" enctype="multipart/form-data">
    <input type="file" name="docs" multiple> <button>Envoyer</button>
  </form>

  <h3>3. Texte + fichier (POST /upload/mix) - String description, FileUpload file</h3>
  <form action="<%= ctx %>/upload/mix" method="post" enctype="multipart/form-data">
    description <input name="description" value="Ma photo">
    <input type="file" name="file"> <button>Envoyer</button>
  </form>

  <h3>4. Objet complet (POST /upload/document) - Document avec auteur imbriqué</h3>
  <form action="<%= ctx %>/upload/document" method="post" enctype="multipart/form-data">
    title <input name="title" value="Rapport"><br>
    auteur.nom <input name="auteur.nom" value="Rakoto"> auteur.age <input name="auteur.age" value="30"><br>
    file <input type="file" name="file"><br>
    attachments <input type="file" name="attachments" multiple><br>
    <button>Envoyer</button>
  </form>
</body>
</html>
