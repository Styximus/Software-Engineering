# QuizMaster – Quiz UI (SCRUM-53)

Die ausführbare Swing-Demo zeigt den NPC-Dialog mit Mila, alle vier Fragetypen und das Ergebnis. Sie verwendet die neuen `Question`-Unterklassen des Hauptprojekts. Der Dialog basiert auf dem Figma-Mockup; Fragen- und Ergebnisansicht führen dessen Gestaltung weiter.

## Starten

Voraussetzung: JDK 17 oder neuer, `java` und `javac` im PATH. Im Repository-Verzeichnis:

```powershell
./QuizMaster/run.ps1
```

Der Launcher kompiliert nach `QuizMaster/out/classes` und kopiert die lokalen Ressourcen. Es werden keine Netzwerkdienste oder zusätzlichen Bibliotheken benötigt. Getestet mit JDK 21.0.7. Die bestehende Maven-Konfiguration fordert weiterhin JDK 25; sie wurde nicht geändert. Der direkte Launcher kompiliert kompatibel zu Java 17.

## Verhalten

- Dialog → Quiz starten → Antwort prüfen → nächste Frage → Ergebnis.
- Mehrfachauswahl erlaubt mehrere Häkchen; Wahr/Falsch genau eine Auswahl.
- Freitext und alle Lücken müssen ausgefüllt sein. Lücken verwenden `{0}, {1}, …` oder `{1}, {2}, …`; die Antworten werden nach Platzhalternummer zugeordnet. Das bestehende Backend nutzt `;` als Trennzeichen, deshalb ist es innerhalb einer Lückenantwort nicht erlaubt.
- Die Auswertung bleibt in `Question.checkAnswer()`, die Sterneschwellen bleiben in `Quiz.calculateStars()` (50/70/90 Prozent). Eine Antwort kann nur einmal Punkte erhalten.
- Wiederholen startet einen neuen Durchlauf. Zurück ruft den übergebenen Menü-Callback auf. Die eigenständige Demo öffnet dabei wieder Milas Einstieg; ein vollständiges Modulmenü ist hier noch nicht implementiert.
- Tab wechselt die Bedienelemente, Leertaste markiert, Enter bestätigt, Esc öffnet beim laufenden Quiz eine Rückfrage zum Verlassen. Lange Texte/Antwortlisten sind scrollbar. Die Hauptaktion bleibt bei der Mindestfenstergröße 1100 × 800 sichtbar.

## Einbindung

Auf dem Swing Event Dispatch Thread erstellen und in den vorhandenen Container einsetzen:

```java
QuestionUserInterface view = new QuestionUserInterface(
    1, npc, npc.getQuiz(), "Biologie entdecken", "Der Waldgarten", showModuleMenu);
frame.setContentPane(view);
frame.revalidate();
frame.repaint();
```

`showModuleMenu` ist ein `Runnable` der aufrufenden Navigation. NPC-Name, Rolle, Begrüßung und Porträt stammen aus `Npc`; Mila ist nur der Demo-Datensatz. Die bisherigen öffentlichen Einstiegsmethoden der UI bleiben vorhanden. Die Modelle sollten während eines Durchlaufs nicht parallel bearbeitet werden.

Workshop-Paketformat, SQLite-Persistenz und Editor sind noch eigenständige PoCs. Für die gemeinsame Anwendung fehlen noch Adapter zu den neuen `Question`-Unterklassen und die Anbindung an das Modulmenü.

## Prüfen

```powershell
./QuizMaster/run.ps1 -Test
./QuizMaster/run.ps1 -Test -Screenshots
```

Der eigene Test-Runner prüft Modelle, Zustandsübergänge und einen vollständigen Durchlauf über Swing-Komponenten auf dem Event Dispatch Thread, inklusive falscher Antworten, Wiederholung und Rücknavigation. `-Screenshots` rendert die tatsächlichen Swing-Komponenten in `out/screenshots`: Dialog, vier Fragetypen, Feedback, Ergebnis und lange Inhalte bei 1440 × 960 sowie kleineren Ansichten. Das sind Offscreen-Renderings; ein automatisierter Test der nativen Windows-Tastatureingabe ist nicht enthalten. Der Test-Runner wird nicht automatisch von Maven Surefire ausgeführt.

Die mitgelieferten Grafiken, Schriftdateien und deren Herkunft sind im [Ressourcenverzeichnis](src/main/resources/ui/README.md) dokumentiert.
