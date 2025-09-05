# HeneriaLobby

Plugin de lobby central pour le réseau Heneria.

## Fonctionnalités

* **Système de Spawn :** Définissez un point de spawn unique pour le lobby avec `/setlobby` et permettez aux joueurs d'y retourner avec `/lobby`.
* **Sélecteur de Serveurs :** Un GUI entièrement personnalisable permet aux joueurs de naviguer facilement entre vos serveurs de jeu.
* **Protection des Items :** Les items du lobby (comme le sélecteur de jeux) ne peuvent être jetés ni placés.
* **Protection Complète :** Un module de protection robuste empêche le grief, les dégâts, et verrouille l'inventaire des joueurs pour une expérience propre et sécurisée dans le lobby.
* **Contrôle de l'Environnement :** Maintient un temps clair et un jour permanent dans les mondes du lobby.
* **Affichages Personnalisés :** Configurez un scoreboard et une Tablist uniques avec des informations dynamiques. Le scoreboard est conçu pour afficher le rang (via LuckPerms), les monnaies, et le nombre total de joueurs sur le réseau.
* **Interactivité :** Améliorez l'expérience des joueurs avec des plaques de saut, un sélecteur de visibilité et des messages de bienvenue personnalisés.
* **Effets de Connexion :** Offrez des effets cosmétiques (sons, particules, feux d'artifice) uniques aux joueurs à leur connexion, avec des effets différents pour chaque grade.
* **Accueil Personnalisé :** Accueillez vos joueurs avec un grand titre à l'écran.
* **Format de Chat :** Gérez le format du chat de votre lobby pour afficher les grades et améliorer la lisibilité.

## Commandes et Permissions

| Commande    | Permission            | Description                                |
| :---------- | :-------------------- | :----------------------------------------- |
| `/setlobby` | `heneria.lobby.admin` | Définit le point de spawn du lobby.        |
| `/lobby`    | (Aucune)              | Téléporte le joueur au spawn.              |
| `/servers`  | (Aucune)              | Ouvre le menu de sélection des serveurs.   |
| (Bypass)    | `heneria.lobby.bypass.protection`| Ignore toutes les protections du lobby.   |
| (Visibilité)| `heneria.lobby.canbeseen`        | Permet d'être vu par les autres joueurs en mode "VIPs". |
| (Effets)    | `heneria.lobby.joineffect.<nom>` | Déclenche l'effet de connexion `<nom>`.        |
| (Chat)      | `heneria.lobby.chatcolor`        | Permet d'utiliser les codes couleurs dans le chat. |

## Dépendances

* **PlaceholderAPI :** Requis. Pour une expérience complète, vous devez installer les extensions (`/papi ecloud download ...`) suivantes :
    * `player` (inclus par défaut)
    * `server` (pour `%server_name%`)
    * `Vault` (pour la compatibilité avec `%luckperms_prefix%`)
    * `LuckPerms` (pour des placeholders plus spécifiques si besoin)
    * Les extensions de vos plugins d'économie (ex: `PlayerPoints`).

## Compilation

Ce projet nécessite Java 21 et Maven.

```sh
mvn package
```
