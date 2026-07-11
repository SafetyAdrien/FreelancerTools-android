# FreelancerTools-android

Application tout-en-un pour freelances/designers : dashboard business (clients, finances,
factures) + une trentaine d'outils utilitaires, organisés dans un tiroir de navigation avec
recherche. Reconstruction native Android (Kotlin + Jetpack Compose) d'une app macOS existante.

## Stack

Kotlin, Jetpack Compose (Material 3), MVVM, Navigation Compose, Room, Hilt, DataStore, CameraX +
ML Kit (scan QR, segmentation), ZXing (génération QR), Media3 Transformer (découpe vidéo),
Foreground Service (Smart Timer), PdfDocument natif (factures), OkHttp (Speed Test), WHOIS brut
via socket TCP (Whois Lookup), Glance (widget écran d'accueil).

## Build

```
./gradlew assembleDebug
```

Nécessite Android Studio / un JDK 17+ et un accès réseau vers `google()` et `mavenCentral()`
pour résoudre le SDK Android, AGP et les dépendances AndroidX/Google Play services — ce dépôt n'a
pas été compilé dans l'environnement où il a été écrit (accès à `dl.google.com` bloqué par la
politique réseau du bac à sable), donc build/test en local est nécessaire avant toute mise en
production.

## Structure

```
app/src/main/java/com/freelancertools/app/
├── data/            # Room (entities/dao/db), repositories, DataStore, backup JSON
├── di/               # Modules Hilt
├── ui/
│   ├── theme/        # Couleurs, typographie, thème sombre par défaut
│   ├── navigation/    # Drawer, catalogue d'outils, NavHost
│   ├── common/        # Composants partagés (ToolScaffold, StatCard, boutons...)
│   ├── dashboard/ clients/ finances/ invoices/ settings/
│   └── tools/         # Un dossier par outil (23 au total)
└── widget/            # Widget Glance du Smart Timer
```

## État connu / limites

- Non compilé/testé sur device dans cette session (voir ci-dessus) — à valider en priorité dans
  Android Studio.
- Pas de gestion de réordonnancement/masquage des catégories du drawer depuis les Paramètres.
- Pas de tuile Quick Settings (marquée optionnelle dans le brief d'origine).
- Le widget écran d'accueil se rafraîchit sur les transitions play/pause et toutes les ~30s
  pendant que le minuteur tourne (pas à chaque seconde, pour rester raisonnable côté
  `AppWidgetManager`/batterie).
