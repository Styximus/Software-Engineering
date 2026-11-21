# ADR-003: Erweiterbare Fragetypen durch eine abstrakte Klasse `Question` mit Unterklassen

## Kontext und Problemstellung

QuizMaster unterstützt mehrere Fragetypen (Multiple Choice, Wahr/Falsch, Freitext, Lückentext), und weitere sollen später ohne großen Aufwand ergänzt werden können. Jeder Typ prüft Antworten auf seine eigene Weise. In den PoCs gab es drei unterschiedliche `Question`-Klassen. Wie bilden wir die Fragetypen so ab, dass alle Komponenten dasselbe Modell nutzen und neue Typen leicht hinzukommen?

## Betrachtete Varianten

* Abstrakte Oberklasse mit Unterklassen  
`Question` ist abstrakt, jede Unterklasse setzt `checkAnswer()` für ihren Fragetyp selbst um.
* Eine Klasse mit Typfeld  
Eine einzige Klasse `Question` mit einem Feld für den Typ. Die Logik unterscheidet die Typen mit `switch`.
* Getrennte Modelle je Komponente  
Editor, Oberfläche und Datenbank verwenden jeweils eigene Klassen und wandeln untereinander um.

## Entscheidung

Gewählte Variante: "Abstrakte Oberklasse mit Unterklassen", denn ein neuer Fragetyp kann ergänzt werden, ohne `Quiz` oder `QuizSession` zu ändern (Open-Closed-Prinzip), und Editor und Oberfläche arbeiten mit denselben Objekten.

Die Unterklassen sind `MultipleChoiceQuestion`, `TrueFalseQuestion`, `TextQuestion` und `ClozeTestQuestion`. Sie implementieren `checkAnswer(String)` und `getType()`.

## Status

angenommen

## Konsequenzen

* Gut, weil ein neuer Fragetyp nur eine neue Unterklasse ist.
* Gut, weil ein im Editor erstelltes Quiz direkt in der Quiz-Oberfläche gespielt werden kann.
* Gut, weil die Bewertungslogik an einer Stelle pro Fragetyp liegt und einzeln getestet werden kann.
* Schlecht, weil Antworten als `String` übergeben werden (z. B. `"0,2"` bei Multiple Choice) und das Format deshalb überall einheitlich sein muss.
* Schlecht, weil Oberfläche und Datenbank trotzdem eine eigene Darstellung bzw. Speicherung pro Fragetyp brauchen. In der Datenbank werden die Typen in einer Tabelle über ein Typfeld unterschieden.
