# Olafon

Application Android de réveil « anti-triche » : l'alarme ne s'arrête qu'après un défi (schéma, question ou calcul), puis affiche un briefing du matin construit à partir de l'agenda du téléphone.

## Prérequis

- Android Studio (version récente) ou un JDK 21 en ligne de commande. Le daemon Gradle est épinglé sur JDK 21 via `gradle/gradle-daemon-jvm.properties` et le télécharge au besoin.
- SDK Android 37 (`compileSdk` et `targetSdk`), `minSdk` 29.
- Un téléphone ou un émulateur sous Android 10 ou plus.

## Démarrer

```bash
git clone https://github.com/justinadjassem/olafon.git
cd olafon
./gradlew :app:assembleDebug        # construit l'APK debug
./gradlew :app:installDebug         # installe sur l'appareil connecté
```

Android Studio : ouvrir le dossier, laisser la synchronisation Gradle se terminer, lancer la configuration `app`.

## Vérifications

```bash
./gradlew ktlintCheck detekt        # style et analyse statique
./gradlew ktlintFormat              # corrige automatiquement le style
./gradlew test                      # tests unitaires
```

La CI GitHub Actions exécute ces trois étapes puis construit l'APK debug sur chaque pull request vers `develop` et `main`.

## Structure

| Module | Rôle |
| --- | --- |
| `:app` | Point d'entrée, navigation, injection Hilt |
| `:core:domain` | Entités, use cases, contrats de repositories (Kotlin pur) |
| `:core:data` | Room, DataStore, implémentations des repositories |
| `:core:designsystem` | Thème, couleurs, typographie, composants partagés |
| `:feature:alarm` | Programmation, réception et sonnerie des alarmes |
| `:feature:challenges` | Générateurs de défis et écrans associés |
| `:feature:home` | Liste des alarmes et éditeur |

Les versions des bibliothèques sont centralisées dans `gradle/libs.versions.toml`.

## Conventions

- Code en anglais, interface en français, package racine `com.olafon`.
- Clean architecture multi-module, MVVM avec `StateFlow`.
- Branches `feature/*` ouvertes depuis `develop`, pull request vers `develop`, releases `release/x.y.z` fusionnées dans `main` et taguées.
- Messages de commit au format Conventional Commits : `type(scope): résumé impératif`.

## Sécurité et données

- Aucune sauvegarde cloud (`android:allowBackup="false"`) : les alarmes et le cache du briefing restent sur l'appareil.
- Les clés de signature (`*.jks`, `keystore.properties`) et `local.properties` sont ignorés par Git et ne doivent jamais être committés.
