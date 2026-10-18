package test.example2;

import java.nio.file.Path;
import java.nio.file.Paths;

import dev.framework.annotation.Controller;
import dev.framework.annotation.UrlMapping;
import dev.framework.util.FileUpload;
import dev.framework.util.ViewUtil;
import test.example2.model.Document;

/**
 * Exemples d'upload. Ouvre /upload/form.
 * Les fichiers sont enregistrés dans le dossier temporaire du système, sous "myframework-uploads".
 */
@Controller("/upload")
public class UploadExampleController {

    private static final Path DIR = Paths.get(System.getProperty("java.io.tmpdir"), "myframework-uploads");

    @UrlMapping(value = "/form", method = "GET")
    public ViewUtil form() {
        ViewUtil view = new ViewUtil();
        view.setView("uploadform");
        return view;
    }

    // EXEMPLE 1 : un seul fichier. Le nom du paramètre Java ("photo") = name de l'<input type="file">
    @UrlMapping(value = "/one", method = "POST")
    public String one(FileUpload photo) throws Exception {
        if (photo == null) {
            return "Aucun fichier reçu";
        }
        Path saved = photo.saveTo(DIR);
        return "Fichier : " + photo.getFileName() + " (nom=" + photo.getName() + ", ext=" + photo.getExtension()
                + ", type=" + photo.getContentType() + ", " + photo.getSize() + " octets)\nEnregistré : " + saved;
    }

    // EXEMPLE 2 : plusieurs fichiers (<input type="file" name="docs" multiple>)
    @UrlMapping(value = "/many", method = "POST")
    public String many(FileUpload[] docs) throws Exception {
        StringBuilder sb = new StringBuilder(docs.length + " fichier(s) :\n");
        for (FileUpload f : docs) {
            f.saveTo(DIR);
            sb.append("- ").append(f.getFileName()).append(" (").append(f.getSize()).append(" octets)\n");
        }
        return sb.toString();
    }

    // EXEMPLE 3 : fichier + champ texte dans la même méthode
    @UrlMapping(value = "/mix", method = "POST")
    public String mix(String description, FileUpload file) throws Exception {
        if (file == null) {
            return "Description : " + description + " (pas de fichier)";
        }
        file.saveTo(DIR);
        return "Description : " + description + "\nFichier : " + file.getFileName() + " (" + file.getSize() + " octets)";
    }

    // EXEMPLE 4 : tout dans un objet (texte + objet imbriqué auteur.* + fichier + fichiers)
    @UrlMapping(value = "/document", method = "POST")
    public String document(Document doc) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Titre : ").append(doc.getTitle()).append("\n");
        if (doc.getAuteur() != null) {
            sb.append("Auteur : ").append(doc.getAuteur().getNom())
              .append(" (").append(doc.getAuteur().getAge()).append(" ans)\n");
        }
        if (doc.getFile() != null) {
            doc.getFile().saveTo(DIR);
            sb.append("Fichier principal : ").append(doc.getFile().getFileName()).append("\n");
        }
        if (doc.getAttachments() != null) {
            for (FileUpload f : doc.getAttachments()) {
                f.saveTo(DIR);
                sb.append("Pièce jointe : ").append(f.getFileName()).append("\n");
            }
        }
        return sb.toString();
    }
}
