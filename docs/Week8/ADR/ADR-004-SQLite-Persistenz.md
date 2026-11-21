# ADR-004: Dauerhafte Speicherung in einer lokalen SQLite-Datenbank über JDBC

## Kontext und Problemstellung

Quizzes, NPCs (inklusive PNG-Bild) und der Spielstand müssen dauerhaft gespeichert werden. Die Anwendung läuft lokal ohne Server, und gespeicherte Daten dürfen bei Fehlern nicht beschädigt werden (Qualitätsszenario Zuverlässigkeit). Wo und wie speichern wir die Daten?

## Betrachtete Varianten

* Lokale SQLite-Datenbank über JDBC  
Eine Datenbank in einer einzelnen Datei, angebunden über einen JDBC-Treiber. Wurde im Datenbank-PoC erprobt.
* Dateien  
Jedes Quiz und jeder NPC wird als eigene Datei gespeichert, z. B. im JSON- oder CSV-Format.
* Server-Datenbank  
Eine zentrale Datenbank wie PostgreSQL auf einem Server.

## Entscheidung

Gewählte Variante: "Lokale SQLite-Datenbank über JDBC", denn sie benötigt keinen Server, schützt Daten durch Transaktionen und wurde im Datenbank-PoC bereits erfolgreich getestet.

Die Datenbankdatei wird beim ersten Start automatisch angelegt. Die Tabellen `quiz`, `question` und `npc` sind aus dem Klassendiagramm abgeleitet. Eine PostgreSQL-Variante bleibt als spätere Erweiterung möglich.

## Status

angenommen

## Konsequenzen

* Gut, weil kein Server und keine Installation nötig sind und die Datenbank eine einzelne Datei ist.
* Gut, weil Transaktionen verhindern, dass ein Quiz bei einem Fehler nur halb gespeichert wird.
* Gut, weil Datenbankregeln die Konsistenz sichern, z. B. kann auf einer Position nur ein NPC stehen (vgl. UC-04, Fall „Position belegt“).
* Schlecht, weil eine zusätzliche Bibliothek (SQLite-JDBC-Treiber) benötigt wird.
* Schlecht, weil zwischen Objekten und Tabellen umgewandelt werden muss, vor allem für die verschiedenen Fragetypen.
* Schlecht, weil die Daten auf einem Gerät bleiben und zum Teilen zusätzlich ein Dateiformat nötig ist (siehe ADR-005).
