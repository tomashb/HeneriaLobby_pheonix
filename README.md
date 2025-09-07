# HeneriaLobby

HeneriaLobby est un plugin de lobby tout-en-un conçu pour les serveurs Minecraft modernes fonctionnant sous Paper et connectés via un proxy Velocity. Il vise à fournir une expérience complète et professionnelle pour les joueurs, de leur arrivée à leur sélection de jeu.

## ✨ Fonctionnalités

* **Système de Spawn Robuste :** Définissez un point de spawn, téléportez les joueurs à leur connexion et s'ils tombent dans le vide.
* **Sélecteur de Serveurs via GUI :** Un menu de 6 lignes, entièrement personnalisable avec des têtes de HeadDatabase, des descriptions riches et des statistiques de joueurs via PlaceholderAPI.
* **Protection Complète du Lobby :** Empêche le grief, les dégâts, la faim et verrouille l'inventaire des joueurs pour une expérience sécurisée.
* **Affichages Personnalisés :** Un scoreboard et une tablist entièrement configurables pour afficher les informations du joueur et du serveur.
* **Expérience Joueur Améliorée :** Inclut des plaques de saut (launchpads), un sélecteur de visibilité des joueurs et des messages de connexion/déconnexion personnalisés.
* **Système de Parkour :** Créez des parcours de parkour avec checkpoints, chronomètre, et records personnels.
* **Commandes Personnalisées :** Définissez vos propres commandes d'information (ex: `/discord`, `/site`) via un simple fichier de configuration.
* **Effets de Connexion :** Attribuez des effets cosmétiques (sons, particules) aux joueurs à leur connexion en fonction de leurs permissions.
* **Mini-Jeu : Mini-Foot :** Une arène de football interactive directement dans le lobby pour divertir les joueurs.
* **Et bien plus :** Titre de bienvenue, format de chat, intégrations PNJ...

## ⚙️ Commandes et Permissions

| Commande | Permission | Description |
| :--- | :--- | :--- |
| `/setlobby` | `heneria.lobby.admin` | Définit le spawn du lobby. |
| `/lobby` | (Aucune) | Se téléporte au spawn. |
| `/servers` | (Aucune) | Ouvre le menu des jeux. |
| `/parkouradmin` | `heneria.lobby.admin.parkour` | Gère les parcours de parkour. |
| `/parkour` | (Aucune) | Commandes pour le parkour. |
| `/lobbyadmin reload` | `heneria.lobby.admin.reload` | Recharge les configurations. |
| ... | `heneria.lobby.bypass.protection` | Ignore les protections du lobby. |
| ... | `heneria.lobby.canbeseen` | Permet d'être vu en mode VIP. |
| ... | `heneria.lobby.chatcolor` | Permet d'utiliser les couleurs dans le chat. |
| ... | `heneria.lobby.joineffect.<nom>` | Donne l'effet de connexion `<nom>`. |

## 📦 Dépendances

* **Requises :**
    * Paper (ou un fork comme Folia) 1.21+
    * PlaceholderAPI
* **Optionnelles (fortement recommandées) :**
    * HeadDatabase (pour les têtes personnalisées)
    * LuckPerms (pour les grades)
    * Un plugin d'économie (pour les récompenses)

## 🔧 Installation

1.  Téléchargez la dernière version du plugin depuis la section "Releases".
2.  Placez le fichier `HeneriaLobby.jar` dans le dossier `plugins` de votre serveur de lobby.
3.  Installez les dépendances requises.
4.  Démarrez le serveur. Les fichiers de configuration seront générés.
5.  Configurez les fichiers à votre guise et utilisez `/lobbyadmin reload`.

