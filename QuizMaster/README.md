# QuizMaster – Quizbibliothek und Editor

Im Modulmenü lässt sich Milas Beispielquiz öffnen oder ein eigenes Quiz erstellen. Für ein eigenes Quiz einen Namen eingeben, jede Frage mit „Frage hinzufügen“ bestätigen und „Quiz übernehmen“ wählen. Das neue Quiz ist dann zum Öffnen ausgewählt. Eigene Quizze stehen für die laufende Sitzung zur Verfügung. Sie verwendet die neuen `Question`-Unterklassen des Hauptprojekts. Der Dialog basiert auf dem Figma-Mockup; Fragen- und Ergebnisansicht führen dessen Gestaltung weiter.

## Starten

Im geöffneten IntelliJ-Projekt [Main.java](src/main/java/de/quizmaster/Main.java) öffnen und neben `main` auf ▶ klicken.

## Verhalten

- Dialog → Quiz starten → Antwort prüfen → nächste Frage → Ergebnis.
- Mehrfachauswahl erlaubt mehrere Häkchen; Wahr/Falsch genau eine Auswahl.
- Freitext und alle Lücken müssen ausgefüllt sein. Lücken verwenden `{0}, {1}, …` oder `{1}, {2}, …`; die Antworten werden nach Platzhalternummer zugeordnet. Das bestehende Backend nutzt `;` als Trennzeichen, deshalb ist es innerhalb einer Lückenantwort nicht erlaubt.
- Die Auswertung bleibt in `Question.checkAnswer()`, die Sterneschwellen bleiben in `Quiz.calculateStars()` (50/70/90 Prozent). Eine Antwort kann nur einmal Punkte erhalten.
- Wiederholen startet einen neuen Durchlauf. Zurück ruft den übergebenen Menü-Callback auf. Im Modulmenü bleibt das zuletzt geöffnete Quiz ausgewählt.
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

Der integrierte Editor erstellt die vier Fragetypen direkt. **Community Workshop** öffnet den Kartenkatalog im selben Fenster. Installierte Karten lassen sich offline spielen und exportieren. Der NPC auf der Karte öffnet die Quizansicht mit den Fragen aus dem Kartenpaket.

Beim ersten Öffnen startet ein lokaler Workshop mit vier Beispielkarten. Veröffentlichungen in diesem Katalog bleiben auf dem eigenen Rechner. Für einen gemeinsamen Katalog kann in der Startkonfiguration von Main die Umgebungsvariable `QUIZMASTER_SERVER_URL` auf die Adresse des Workshop-Servers gesetzt werden, bei Bedarf zusammen mit `QUIZMASTER_TOKEN`. Die installierten Karten liegen unter `.quizmaster/workshop/library` im Benutzerverzeichnis.

## Prüfen

```powershell
./QuizMaster/run.ps1 -Test
./QuizMaster/run.ps1 -Test -Screenshots
```

Der eigene Test-Runner prüft Modelle, Zustandsübergänge und einen vollständigen Durchlauf über Swing-Komponenten auf dem Event Dispatch Thread, inklusive falscher Antworten, Wiederholung und Rücknavigation. `-Screenshots` rendert die tatsächlichen Swing-Komponenten in `out/screenshots`: Dialog, vier Fragetypen, Feedback, Ergebnis und lange Inhalte bei 1440 × 960 sowie kleineren Ansichten. Das sind Offscreen-Renderings; ein automatisierter Test der nativen Windows-Tastatureingabe ist nicht enthalten. Der Test-Runner wird nicht automatisch von Maven Surefire ausgeführt.

Die mitgelieferten Grafiken, Schriftdateien und deren Herkunft sind im [Ressourcenverzeichnis](src/main/resources/ui/README.md) dokumentiert.
