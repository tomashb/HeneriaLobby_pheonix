# HeneriaLobby

Plugin de lobby central pour le réseau Heneria.

## Fonctionnalités

* **Système de Spawn :** Définissez un point de spawn unique pour le lobby avec `/setlobby` et permettez aux joueurs d'y retourner avec `/lobby`.
* **Sélecteur de Serveurs :** Un GUI entièrement personnalisable permet aux joueurs de naviguer facilement entre vos serveurs de jeu.
* **Protection des Items :** Les items du lobby (comme le sélecteur de jeux) ne peuvent être jetés ni placés.

## Commandes et Permissions

| Commande    | Permission            | Description                                |
| :---------- | :-------------------- | :----------------------------------------- |
| `/setlobby` | `heneria.lobby.admin` | Définit le point de spawn du lobby.        |
| `/lobby`    | (Aucune)              | Téléporte le joueur au spawn.              |
| `/servers`  | (Aucune)              | Ouvre le menu de sélection des serveurs.   |

## Dépendances

* **PlaceholderAPI :** Requis pour afficher les informations dynamiques comme le nombre de joueurs dans le menu.

## Compilation

Ce projet nécessite Java 21 et Maven.

```sh
mvn package
```
