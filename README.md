# ScanLib — Application mobile de reconnaissance de tranches de livres

ScanLib est une application Android permettant d’identifier automatiquement les livres à partir d’une photo d’étagère, en détectant les tranches et en extrayant les titres/auteurs par OCR, puis en enrichissant les informations via l’API Google Books.

## Structure du dépôt

```
Groupe_A_ScanLib/
├── Application/                 # Application Android complète (MVVM + Hilt)
├── BDD/                         # Scripts et tests liés à la base de données locale (Room)
└── ComputerVision/
    └── Spine_Book_Detection/
        ├── modèle/             # Modèles entraînés : YOLOv8 (.pt), ONNX, TF, TFLite
        ├── yolov8.ipynb        # Notebook d'entraînement YOLOv8
        ├── TFLite.ipynb        # Conversion vers TFLite
        ├── Detection_tranches_livres.ipynb # Test du modèle entraîné
        └── biblio1.jpg         # Image réelle d'étagère testée
```

## Fonctionnalités principales

- Prise de photo d’étagère (via CameraX)
- Détection de tranches de livres (YOLOv8)
- OCR avec Google MLKit
- Requête d’enrichissement via Google Books API
- Base de données locale Room (SQLite)
- Recherche, filtres, favoris et gestion manuelle

## Modèle IA utilisé

- Modèle YOLOv8n (Nano) entraîné sur Roboflow `Book Spine Detection`
- Conversion complète :
  - `best-v1.pt` → `best-v1.onnx` → `best_tf/` → `best-v1.tflite`

## Technologies

- Android / Kotlin
- YOLOv8 (Ultralytics)
- ML Kit (OCR)
- Google Books API
- Room / SQLite
- Jetpack (CameraX, ViewModel, LiveData, Hilt)

## Captures d’écran (application)

*À insérer : home_before_example.jpg, scan_example.jpg, etc.*

## À venir / pistes d’amélioration

- Traitement en temps réel sur flux caméra
- Amélioration de l’OCR (prétraitement, NER)
- Multi-utilisateur et sauvegarde cloud
- API alternatives (OpenLibrary, BNF, etc.)

## Rapport complet

Le rapport de projet est disponible en PDF (structure académique : introduction, état de l’art, entraînement modèle, architecture, tests...).

## Auteurs

- Luka MEUNIER — Interface utilisateur, base Room, navigation
- Flora BENANE — Étude de l’existant, conception, validation
- Andrianina ANDRIANTSIALONINA — YOLOv8, OCR, traitement d’image

## Licence

Projet réalisé dans le cadre du Master 1 SIME — Université de Rouen.
