package test.example2.model;

import dev.framework.util.FileUpload;

/**
 * Objet bindé depuis un formulaire multipart :
 *  - title                    : champ texte simple
 *  - auteur.nom / auteur.age  : objet imbriqué (préfixe "auteur.")
 *  - file                     : un fichier
 *  - attachments              : plusieurs fichiers (même nom de champ répété)
 */
public class Document {
    private String title;
    private Auteur auteur;
    private FileUpload file;
    private FileUpload[] attachments;

    public Document() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Auteur getAuteur() { return auteur; }
    public void setAuteur(Auteur auteur) { this.auteur = auteur; }

    public FileUpload getFile() { return file; }
    public void setFile(FileUpload file) { this.file = file; }

    public FileUpload[] getAttachments() { return attachments; }
    public void setAttachments(FileUpload[] attachments) { this.attachments = attachments; }
}
