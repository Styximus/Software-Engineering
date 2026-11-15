# Herkunft der UI-Ressourcen

Design: [Menü-Mockup (Copy)](https://www.figma.com/design/YE8YbEpodG8RoXhazn7OVf/?node-id=3-2474), gelesen am 13.11.2026. Ressourcen gehören zum bereitgestellten Projektentwurf; hier wird keine zusätzliche Lizenz für die Illustrationen behauptet.

| Lokale Datei | Figma-Quelle / Slot | Logische Größe und Verwendung |
| --- | --- | --- |
| `figma/island.png` | `3:2476`, Hintergrund der Dialogansicht | Seitenfüllend mit `object-cover`-entsprechender Skalierung, `QuestionUserInterface.paintComponent` |
| `figma/mila.png` | `3:2592`, NPC-Figur | 165 × 244, Dialogposition (12, 0); Demo-NPC liefert das Bild dynamisch über `Npc.skin` |
| `figma/leaf.svg` + `.png` | `3:2701`, Rollenhinweis | 15 × 15, `badge()` |
| `figma/star.svg` + `.png` | `3:2704`, Belohnung | 16 × 16, Dialog/Status/Ergebnis |
| `figma/play.svg` + `.png` | `3:2707`, Startaktion | 20 × 20, rechts im Startbutton |
| `figma/close.svg` + `.png` | `3:2710`, Dialog schließen | 17 × 17 in 32 × 32 Button |
| `figma/compass.svg` + `.png` | Markenbereich des Dialogframes `3:2474` | 30 × 30 im Header |

Die von Figma gelieferten PNGs/SVGs wurden mit `curl -L` lokal heruntergeladen. Die SVG-Originale sind unverändert; zusätzlich wurden sie für Swing mit Sharp bei 144 dpi gerastert (2×). Die PNG-Icons werden in ihrer ursprünglichen logischen Größe gezeichnet, nicht auf Containergröße gestreckt. Es werden keine temporären Asset-URLs zur Laufzeit verwendet. Auswahlkästchen und Radiobutton-Markierungen sind normale programmatisch gezeichnete Zustände der neuen Eingabefelder, keine ersetzten Figma-Grafiken.

## Schriften

Offizielle Quellen: [Fraunces in Google Fonts](https://github.com/google/fonts/tree/main/ofl/fraunces), [Nunito Sans in Google Fonts](https://github.com/google/fonts/tree/main/ofl/nunitosans). Lizenztexte liegen als `fonts/Fraunces-OFL.txt` und `fonts/NunitoSans-OFL.txt` bei.

Die variablen TTFs wurden für die Java-Font-API mit FontTools in statische Instanzen umgewandelt:

- Fraunces: `wght=600/700`, `opsz=25`, `SOFT=0`, `WONK=1`.
- Nunito Sans: `wght=400/700/800`, `wdth=100`, `opsz=12`, `YTLC=500`.

Die Transformation benötigt keine Bibliothek zur Laufzeit. `QuizTheme` lädt alle Schrift- und Bildressourcen über den Classpath, sodass sie auch in einem JAR verfügbar sind.
