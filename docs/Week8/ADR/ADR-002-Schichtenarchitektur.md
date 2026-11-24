# ADR-002: Struktur des Projekts als Schichtenarchitektur mit vier Schichten

## Kontext und Problemstellung

Die PoCs der ersten Demo waren unabhängige Programme mit jeweils eigenen Klassen. Im gemeinsamen Projekt arbeiten sechs Personen parallel, und Änderungen an einem Teil, z. B. an der Oberfläche, sollen möglichst wenige andere Teile betreffen (Qualitätsszenario Q3, Änderbarkeit, hohe Priorität). Wie strukturieren wir den Code, damit das möglich ist?

## Betrachtete Varianten

* Schichtenarchitektur  
Aufteilung in Präsentation, Geschäftslogik, Domänenmodell und Persistenz. Eine Schicht greift nur auf die Schicht darunter zu.
* Monolith ohne feste Struktur  
Oberflächenklassen enthalten auch Logik und Datenzugriff.
* Verteilte Architektur  
Client-Server mit eigenem Backend-Dienst, wie im Workshop-PoC mit HTTP-Schnittstelle.

## Entscheidung

Gewählte Variante: "Schichtenarchitektur", denn sie trennt Oberfläche, Logik und Datenhaltung, lässt sich ohne Server umsetzen und erlaubt eine klare Aufgabenverteilung im Team.

| Schicht | Aufgabe | Beispiele |
| --- | --- | --- |
| Präsentation | Darstellung und Benutzereingaben (Swing) | `QuestionUserInterface`, `QuizEditorDialog`, `NpcMenu` |
| Geschäftslogik | Abläufe und Regeln | `QuizSession`, `QuizEditorService`, `Map` |
| Domänenmodell | Fachliche Objekte aus dem Klassendiagramm | `Question` und Unterklassen, `Quiz`, `Npc` |
| Persistenz | Speichern und Laden | SQLite-Zugriff, `Savestate` |

## Status

angenommen

## Konsequenzen

* Gut, weil sich die Aufgaben klar nach Schichten auf das Team verteilen lassen.
* Gut, weil Logik und Modell ohne Oberfläche getestet werden können.
* Gut, weil die Oberfläche umgestaltet werden kann, ohne Logik oder Datenzugriff zu ändern.
* Schlecht, weil für einfache Funktionen mehr Klassen entstehen.
* Schlecht, weil die Gefahr besteht, dass Schichten Aufrufe nur durchreichen (Architektur-Sinkhole-Anti-Pattern). Logikklassen sollen deshalb echte Aufgaben übernehmen, z. B. Prüfung und Ablaufsteuerung.
