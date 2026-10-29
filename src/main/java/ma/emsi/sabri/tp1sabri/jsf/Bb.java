
package ma.emsi.sabri.tp1sabri.jsf;

import ma.emsi.sabri.tp1sabri.llm.JsonAdapterPourGemini;
import ma.emsi.sabri.tp1sabri.llm.LlmInteraction;
import java.util.Arrays;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean pour la page JSF index.xhtml.
 *
 * Gère les interactions avec l'utilisateur, les messages
 * d'erreur et l'historique de la conversation.
 *
 * @ViewScoped permet de conserver l'état de la conversation
 * pendant plusieurs requêtes HTTP sur la même vue.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Rôle sélectionné par l'utilisateur.
     */
    private String roleSysteme;

    /**
     * Le rôle peut être modifié avant le premier message.
     */
    private boolean roleSystemeChangeable = true;

    /**
     * Liste des rôles disponibles.
     */
    private List<SelectItem> listeRolesSysteme;

    /**
     * Dernière question ou commande de l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse générée par le service.
     */
    private String reponse = "";

    /**
     * Historique de la conversation.
     */
    private StringBuilder conversation = new StringBuilder();

    // ==============================
    // AJOUT TP1 : MODE DEBUG
    // ==============================

    /**
     * Active ou désactive l'affichage du mode debug.
     */
    private boolean debug = false;

    /**
     * JSON envoyé dans la requête au LLM.
     */
    private String texteRequeteJson = "";

    /**
     * JSON retourné dans la réponse du LLM.
     */
    private String texteReponseJson = "";

    // ==============================
    // FIN AJOUT TP1
    // ==============================

    /**
     * Adaptateur CDI pour communiquer avec l'API Gemini.
     */
    @Inject
    private JsonAdapterPourGemini jsonAdapter;

    /**
     * Contexte JSF pour afficher les messages d'erreur.
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Constructeur sans argument.
     */
    public Bb() {
    }

    // ==============================
    // GETTERS ET SETTERS
    // ==============================

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    // ==============================
    // AJOUT TP1 : GETTERS ET SETTERS
    // ==============================

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }

    // ==============================
    // FIN AJOUT TP1
    // ==============================

    // ==============================
    // ENVOI DE LA QUESTION
    // ==============================

    /**
     * Traite une question saisie par l'utilisateur.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {

        // 1. Vérifier si la question est vide.
        if (question == null || question.isBlank()) {

            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question"
            );

            facesContext.addMessage(null, message);

            return null;
        }

        // 2. Récupérer le rôle au début de la conversation.
        if (this.conversation.isEmpty()) {

            // Envoyer le rôle système à JsonAdapter
            this.jsonAdapter.setSystemRole(this.roleSysteme);

            // Bloquer le rôle après le premier message
            this.roleSystemeChangeable = false;
        }

        // 3. Envoyer la question à l'API Gemini.
        // On utilise = au lieu de += pour ne conserver
        // que la réponse correspondant à la question actuelle.
        try {
            LlmInteraction interaction = jsonAdapter.envoyerRequete(question);

            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();

        } catch (Exception e) {

            this.reponse = "Erreur : " + e.getMessage();

            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Problème de connexion avec l'API du LLM"
                            + e.getMessage()
                            + "\n"
                            + Arrays.toString(e.getStackTrace())
            );

            facesContext.addMessage(null, message);
        }

        // 4. Ajouter la question et la réponse à l'historique.
        afficherConversation();

        // 5. Rester sur la page actuelle.
        return null;
    }

    // ==============================
    // NOUVEAU CHAT
    // ==============================

    /**
     * Démarre une nouvelle conversation.
     *
     * La navigation vers index.xhtml permet de
     * réinitialiser l'état du backing bean ViewScoped.
     *
     * @return "index" pour revenir à la page initiale.
     */
    public String nouveauChat() {
        return "index";
    }

    // ==============================
    // HISTORIQUE
    // ==============================

    /**
     * Ajoute la question et sa réponse à l'historique.
     */
    private void afficherConversation() {

        this.conversation
                .append("== User:\n")
                .append(this.question)
                .append("\n== Serveur:\n")
                .append(this.reponse)
                .append("\n\n");
    }

    // ==============================
    // RÔLES SYSTÈME
    // ==============================

    /**
     * Retourne les rôles disponibles dans la liste
     * déroulante de la page index.xhtml.
     */
    public List<SelectItem> getRolesSysteme() {

        if (this.listeRolesSysteme == null) {

            this.listeRolesSysteme = new ArrayList<>();

            this.listeRolesSysteme.add(
                    new SelectItem(
                            "Tu es un assistant utile. Réponds clairement aux questions de l'utilisateur.",
                            "Assistant"
                    )
            );

            this.listeRolesSysteme.add(
                    new SelectItem(
                            "Tu es un traducteur. Traduis les textes anglais en français.",
                            "Traducteur anglais-français"
                    )
            );

            this.listeRolesSysteme.add(
                    new SelectItem(
                            "Tu es un guide touristique. Aide l'utilisateur à découvrir des destinations et des lieux touristiques.",
                            "Guide touristique"
                    )
            );

            // ==============================
            // BONUS TP1 : RÔLE SYSTÈME ORIGINAL
            // ==============================

            this.listeRolesSysteme.add(
                    new SelectItem(
                            "Tu es un détective spécialisé dans les bugs informatiques. "
                                    + "Pour chaque problème, présente ton analyse comme une enquête "
                                    + "avec trois rubriques : Indices, Suspects et Verdict. "
                                    + "Explique les causes techniques de manière précise, "
                                    + "avec une touche d'humour, puis propose une solution concrète.",
                            "Détective des bugs"
                    )
            );
        }

        return this.listeRolesSysteme;
    }
}
