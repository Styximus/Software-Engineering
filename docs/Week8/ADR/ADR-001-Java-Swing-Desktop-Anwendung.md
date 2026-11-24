# ADR-001: Benutzeroberfläche als lokale Desktop-Anwendung mit Java Swing

## Kontext und Problemstellung

QuizMaster soll eine Spielwelt mit NPCs, einen Quiz-Editor und eine Quiz-Oberfläche bieten. Das Team hat Java-Kenntnisse aus dem Studium, und Online-Funktionen sind laut SRS für die erste Version nicht verbindlich. Mit welcher Technologie und als welche Art von Anwendung setzen wir die Benutzeroberfläche um?

## Betrachtete Varianten

* Desktop-Anwendung mit Java Swing  
Oberfläche mit der im JDK enthaltenen Swing-Bibliothek, Start als lokales Java-Programm.
* Desktop-Anwendung mit JavaFX  
Modernere Java-Oberflächenbibliothek, muss als zusätzliche Bibliothek eingebunden werden.
* Web-Anwendung mit HTML/JavaScript-Frontend  
Oberfläche im Browser, benötigt zusätzlich ein Backend. Wurde im Frontend-PoC als Hello World ausprobiert.

## Entscheidung

Gewählte Variante: "Desktop-Anwendung mit Java Swing", denn sie benötigt keine zusätzlichen Bibliotheken oder Server, das ganze Team kann in Java arbeiten, und die PoCs haben die Entwicklungsumgebung dafür bereits bestätigt.

## Status

angenommen

## Konsequenzen

* Gut, weil Swing Teil des JDK ist und keine Installation zusätzlicher Bibliotheken nötig ist.
* Gut, weil ohne Netzwerkzugriffe Risiken bei Verfügbarkeit und Sicherheit entfallen und die Reaktionszeiten kurz sind (Qualitätsszenarien Q1 und Q2, Effizienz).
* Schlecht, weil ein modernes Aussehen eigenen Gestaltungsaufwand erfordert (gelöst über `QuizTheme`, Figma-Ressourcen und eigene Schriftarten).
* Schlecht, weil die Anwendung nicht im Browser oder auf Smartphones läuft.
* Schlecht, weil der HTML-PoC für die Anwendung nicht weiterverwendet werden kann.
