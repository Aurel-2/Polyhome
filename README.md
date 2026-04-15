# PolyHome - Application de domotique

PolyHome est une application Android permettant de gérer des chalets domotiques et de gérer les accès des utilisateurs.

## Structure du projet

Ce projet a suivi au maximum ce que suggère Google pour la conception d'application Android.


```text
├── data/               # Couche donnée
│   ├── local/          # Stockage local 
│   └── remote/         # Communication avec l'API
│       ├── api/        # Définition des interfaces Retrofit
│       └── repository/ # Implémentations des dépôts (accès aux données réseau)
│
├── domain/             # Couche métier
│   ├── model/          # Entités (Device, House, User, etc.)
│   └── repository/     # Interfaces des dépôts (contrats de données)
│
├── ui/                 # Couche présentation (UI)
│   ├── devices/        # Activité pour le contrôle des appareils
│   ├── home/           # Activité principale (liste des maisons)
│   ├── house_users/    # Activité pour la gestion des membres d'une maison
│   ├── login/          # Activité pour la connexion
│   └── register/       # Activité pour l'inscription
│
├── utils/              # Classes utilitaires (Gestion d'erreurs, Result wrapper)
└── MainActivity.kt     # Point d'entrée pour de l'application (Login ou Home)
```

## Technologies utilisées

- Architecture MVVM pour l'orga du code
- Retrofit pour les appels API
- StateFlow pour la gestion des états
- DataStore pour la persistance du token
- WebView pour l'affichage du plan de la maison

## Sujet du projet et documentation de l'API

https://www.lamarmotte.info/wp-content/uploads/2023/01/Android-Projet-PolyHome-5.pdf