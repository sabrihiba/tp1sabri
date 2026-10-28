
    package ma.emsi.sabri.tp1sabri.jsf;

    import ma.emsi.sabri.tp1sabri.service.GenerateurReponse;
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
     * Le traitement de chiffrement et de déchiffrement César
     * est réalisé par le service GenerateurReponse.
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

        /**
         * Service CDI responsable du traitement César.
         */
        @Inject
        private GenerateurReponse generateurReponse;

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
        // ENVOI DE LA QUESTION
        // ==============================

        /**
         * Traite une commande saisie par l'utilisateur.
         *
         * Exemples :
         * encrypt: BONJOUR
         * decrypt: ERQMRXU
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
            String roleSystemePourModification = null;

            if (this.conversation.isEmpty()) {

                roleSystemePourModification = this.roleSysteme;

                // Bloquer le rôle après le premier message.
                this.roleSystemeChangeable = false;
            }

            // 3. Appeler le service de chiffrement/déchiffrement.
            // On utilise = au lieu de += pour ne conserver
            // que la réponse correspondant à la question actuelle.
            this.reponse = this.generateurReponse.genererReponse(
                    this.question,
                    roleSystemePourModification
            );

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
                                "Assistant de chiffrement Cesar",
                                "Assistant"
                        )
                );

                this.listeRolesSysteme.add(
                        new SelectItem(
                                "Specialiste du chiffrement de messages",
                                "Cryptographe"
                        )
                );

                this.listeRolesSysteme.add(
                        new SelectItem(
                                "Specialiste du dechiffrement de messages",
                                "Decrypteur"
                        )
                );
            }

            return this.listeRolesSysteme;
        }
    }
