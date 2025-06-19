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
<p align="center">
  <img src="https://github.com/user-attachments/assets/237e8cf4-b4cc-430e-bc2a-93c87f4c2bb8" width="200"/>
  <img src="https://github.com/user-attachments/assets/7baf3950-022d-47d9-997c-ae3ddc877010" width="200"/>
  <img src="https://github.com/user-attachments/assets/076ab79b-23e3-471d-b1bb-81ec29d3d442" width="200"/>
  <img src="https://github.com/user-attachments/assets/cb79443d-2e92-45ef-a179-1900489a284a" width="200"/>
</p>

## À venir / pistes d’amélioration

- Traitement en temps réel sur flux caméra
- Amélioration de l’OCR (prétraitement, NER)
- Multi-utilisateur et sauvegarde cloud
- API alternatives (OpenLibrary, BNF, etc.)

## Rapport complet

Le rapport de projet est disponible en PDF (structure académique : introduction, état de l’art, entraînement modèle, architecture, tests...).

## Auteurs

- Luka MEUNIER — Interface utilisateur, navigation entre les écrans, ergonomie générale de l’application.
- Flora BENANE — Base de données Room, gestion des groupes de livres, filtres et tri, étude de l’existant.
- Andrianina ANDRIANTSIALONINA — Détection YOLOv8, OCR avec MLKit, traitement d’image, intégration Android, UI.


## Licence

Projet réalisé dans le cadre du Master 1 SIME — Université de Rouen.
