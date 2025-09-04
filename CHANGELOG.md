## [1.1.0] - Ajout du Sélecteur de Serveurs
### ✨ Ajouts
- Implémentation du GUI de sélection des serveurs, entièrement configurable via `server-selector.yml`.
- Ajout de l'item "Terre" dans l'inventaire des joueurs pour ouvrir le menu.
- Ajout de la commande `/servers` pour ouvrir le menu.
- Intégration avec PlaceholderAPI pour afficher le nombre de joueurs sur chaque serveur.
- Ajout de l'action `server:<nom>` pour envoyer les joueurs vers les serveurs du réseau Velocity.

## [1.2.2] - Formatage Final des Descriptions
### 🎨 Améliorations
- Restructuration des descriptions des jeux dans le GUI pour séparer les sections "Description" et "Informations".

## [1.2.4] - Ajustements Finaux du Design et de la Cosmétique
### 🎨 Améliorations
- Correction de la disposition des bordures dans le GUI pour une fidélité parfaite avec la maquette.
- Ajout d'un effet d'enchantement visuel sur l'item "Bedwars" pour indiquer son statut populaire.

## [1.3.0] - Finitions et Améliorations de l'Expérience
### 🎨 Améliorations
- Ajustement final de la disposition des bordures et des items dans le GUI.
- Mise en forme finale des titres des jeux (gras, majuscules, tags).
- Changement de la couleur du titre du menu.
### 🛡️ Protections
- Il est désormais impossible de poser l'item du sélecteur de jeux. L'événement de placement est annulé.

## [1.4.0] - Ajout du Module de Protection
### ✨ Ajouts
- Ajout d'un système complet de protection pour les mondes du lobby.
- Prévention du grief (casse/pose de blocs).
- Annulation de tous les dégâts aux joueurs et de la perte de faim.
- Verrouillage de l'inventaire des joueurs.
- Contrôle de la météo et du cycle jour/nuit.
- Ajout de la permission `heneria.lobby.bypass.protection` pour les administrateurs.

## [1.5.0] - Ajout du Scoreboard et de la Tablist
### ✨ Ajouts
- Ajout d'un scoreboard latéral personnalisable via `scoreboard.yml`, basé sur un design spécifique.
- Ajout d'un header et footer de Tablist personnalisables.
- Intégration complète avec PlaceholderAPI pour des affichages dynamiques, incluant les préfixes LuckPerms et le total de joueurs du réseau.
- Ajout d'options dans `config.yml` pour activer/désactiver ces fonctionnalités.
