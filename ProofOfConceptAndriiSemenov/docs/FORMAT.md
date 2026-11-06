# Kartenformat `.qmap`, Version 1

Eine Karte ist eine UTF-8-Textdatei im Java-Properties-Format. Sie enthält nur Daten, keine Skripte, Archive, Dateipfade, Java-Objekte oder externen Ressourcen. Ein vollständiges bearbeitbares Beispiel liegt unter `samples/upload/kuestenpfad.qmap`.

## Metadaten

| Feld | Bedeutung / Grenze |
|---|---|
| `format` | Exakt `1` |
| `title` | Titel, 1-50 Zeichen |
| `author` | Angegebener Autor, 1-40 Zeichen; kein verifizierter Account |
| `category` | Thema, 1-30 Zeichen; z. B. Natur, Weltraum, Technik |
| `description` | Beschreibung, 1-350 Zeichen |
| `width`, `height` | 8-24 Spalten, 6-16 Zeilen |
| `row.0` bis `row.{height-1}` | Genau `width` Terrainzeichen pro Zeile |
| `spawn.x`, `spawn.y` | Startposition der Spielerfigur |
| `npc.x`, `npc.y` | Position des Quiz-NPCs |
| `npc.name` | Name des NPCs, 1-30 Zeichen |
| `questions` | Anzahl der Fragen, 1-10 |

Terrainzeichen: `.` begehbarer Boden, `T` Baum, `~` Wasser, `#` Felsen. Koordinaten beginnen bei **0**, x nach rechts, y nach unten. Spieler und NPC müssen auf verschiedenen begehbaren Feldern stehen. Vom Start muss ein Feld direkt neben dem NPC erreichbar sein. Wasser, Bäume, Felsen und das NPC-Feld sind beim Spielen nicht begehbar.

## Fragen

Für jede Frage `i` von 0 bis `questions-1`:

```properties
q.0.text=Welches Meer liegt zwischen Europa und Afrika?
q.0.a.0=Ostsee
q.0.a.1=Mittelmeer
q.0.a.2=Nordsee
q.0.a.3=Karibisches Meer
q.0.correct=1
q.0.explanation=Das Mittelmeer liegt zwischen Europa, Afrika und Asien.
```

Genau vier Antwortmöglichkeiten; `correct` ist der Index **0-3**. Frage maximal 130 Zeichen, Antwort maximal 65, Erklärung maximal 200. Es gibt einen Punkt für eine richtige Antwort und keine Teilpunkte. Pflichttexte dürfen nicht leer sein oder Steuerzeichen enthalten. Umlaute können direkt in UTF-8 gespeichert werden. Backslashes folgen den üblichen Java-Properties-Escaperegeln.

## Eigene Karte veröffentlichen

1. `samples/upload/kuestenpfad.qmap` unter einem neuen Dateinamen kopieren.
2. Titel, Autor, Beschreibung, Terrain und Fragen ändern. Die Zeilenlängen und Frageanzahl konsistent halten.
3. Im Desktop-Client **Karte veröffentlichen** wählen und die Datei öffnen.
4. Die Vorschau prüfen und den Upload bestätigen. Fehler werden vor dem Upload angezeigt; der Server validiert unabhängig nochmals.
5. Ein anderer Client aktualisiert seinen Workshop und lädt das Paket herunter.

Der Server weist die **SHA-256-Prüfsumme der exakten Dateibytes** als ID zu. Dateiname und Autorenname bestimmen keinen Speicherpfad. Identische Dateien ergeben dieselbe ID; auch eine Änderung von Kommentaren/Zeilenumbrüchen ergibt eine neue ID. Pakete sind unveränderlich: es gibt im PoC keinen Update- oder Lösch-Endpunkt.

## HTTP-Vertrag

| Anfrage | Erfolg | Fehler |
|---|---|---|
| `GET /api/maps` | 200, UTF-8-Properties mit `count` sowie `0.id`, `0.title`, `0.author`, `0.category`, `0.description`, `0.questions`, `0.size` usw. | 401 bei fehlendem/falschem konfiguriertem Schlüssel |
| `POST /api/maps` | 201, ID als Klartext; auch bei identischem erneutem Upload | 400 bei ungültigem/zu großem Paket oder vollem Speicher; 401 bei fehlendem Schlüssel |
| `GET /api/maps/{id}` | 200, exakte `.qmap`-Bytes | 404 bei unbekannter gültiger ID, 400 bei ungültiger ID, 401 bei fehlendem Schlüssel |

Wenn konfiguriert: `Authorization: Bearer <QUIZMASTER_TOKEN>`. Maximal 65.536 Bytes pro Paket und 100 Pakete je Speicher. Der Server schreibt erst nach erfolgreicher Validierung eine temporäre Datei und verschiebt sie dann an den Zielort. Clients prüfen die ID gegen den Download, bevor sie das Paket lokal speichern.
