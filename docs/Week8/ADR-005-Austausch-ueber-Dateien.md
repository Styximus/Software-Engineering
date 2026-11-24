# ADR-005: Teilen von Quizzes und NPCs über geprüfte Export- und Importdateien

## Kontext und Problemstellung

Laut User Story 1 sollen Spieler Quizzes und NPCs mit anderen teilen können. Online-Funktionen sind für die erste Version nicht verbindlich. Importierte Dateien können von anderen Personen stammen und fehlerhaft sein, und laut Qualitätsszenario Q5 (Zuverlässigkeit) darf die Anwendung dabei nicht abstürzen. Wie tauschen Spieler Inhalte aus?

## Betrachtete Varianten

* Export und Import über Dateien  
Quizzes und NPCs werden in einem festgelegten, versionierten Format exportiert und beim Import vollständig geprüft.
* Online-Katalog mit eigenem Server  
Inhalte werden über eine HTTP-Schnittstelle veröffentlicht und heruntergeladen, wie im Workshop-PoC.
* Direkter Austausch der Datenbankdatei  
Spieler geben ihre gesamte SQLite-Datei weiter.

## Entscheidung

Gewählte Variante: "Export und Import über Dateien", denn sie funktioniert ohne Server, erfüllt die Anforderungen der ersten Version und erlaubt es, fremde Inhalte vor der Übernahme in die Datenbank zu prüfen.

Jede Datei enthält eine Formatversion. Fehlerhafte Dateien werden mit einer verständlichen Meldung abgelehnt. Der Online-Katalog aus dem Workshop-PoC bleibt eine mögliche Erweiterung für spätere Versionen.

Noch offen ist das konkrete Dateiformat, z. B. das `.qmap`-Paketformat aus dem Workshop-PoC oder CSV wie im Klassendiagramm vorgesehen. Die Entscheidung trifft das Team im nächsten Sprint.

## Status

vorgeschlagen

## Konsequenzen

* Gut, weil kein Server nötig ist und Teilen z. B. per E-Mail oder USB-Stick funktioniert.
* Gut, weil die Prüfung beim Import Datenbank und Anwendung vor fehlerhaften Inhalten schützt.
* Gut, weil ältere Dateien anhand der Formatversion erkannt werden können.
* Schlecht, weil Spieler Dateien selbst weitergeben müssen und es keine zentrale Übersicht gibt.
* Schlecht, weil das Format gepflegt werden muss, wenn sich das Datenmodell ändert.
