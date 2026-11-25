# ADR-006 – QuizMaster mit GitHub Actions bauen und als JAR bereitstellen

## Kontext und Problemstellung

QuizMaster liegt auf GitHub. Mehrere Teammitglieder ändern Editor, Quiz und Workshop. Beim Zusammenführen soll erkennbar sein, ob sich die Desktop-App aus dem jeweiligen Commit bauen lässt und ob die Karten und Schriften im ausgelieferten Paket verfügbar sind. Welcher Dienst kann diesen Nachweis bei jedem Push liefern? [SCRUM-63](https://softwareengineeringsite.atlassian.net/browse/SCRUM-63)

## Betrachtete Varianten

- **GitHub Actions.** Workflow und Quellcode liegen im selben Repository. Das Team sieht das Ergebnis direkt am Pull Request und kann das geprüfte Paket aus dem Lauf herunterladen.
- **Jenkins auf einem eigenen Server.** Das Team hätte Kontrolle über die Umgebung, müsste aber den Server betreiben, aktualisieren und mit GitHub verbinden.
- **GitLab CI/CD.** Eine Pipeline mit Runnern und Build-Artefakten wäre möglich. Für das bestehende GitHub-Repository käme die Anbindung an einen zweiten Dienst hinzu.

## Entscheidung

Wir wählen GitHub Actions mit einem von GitHub betriebenen Windows-Runner. Das passt zum bestehenden Review-Prozess und zur Desktop-Umgebung des Teams. Die Build-Regeln stehen in der Maven-POM und lassen sich lokal ausführen. [GitHub-Dokumentation zu Java mit Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)

Der Workflow startet bei jedem Push und bei Pull Requests nach `main`. Maven kompiliert mit Temurin 21 für Java 17 und packt Anwendung, Schriften und Karten in ein ausführbares JAR. Anschließend läuft der vorhandene Test-Runner gegen genau dieses Paket. Swing rendert ohne sichtbares Fenster, der Workshop-Test verwendet einen temporären Katalog und HTTP auf der Loopback-Adresse. Nach erfolgreicher Prüfung steht das JAR im Workflow-Lauf zum Download bereit. [Workflow](../../../.github/workflows/quizmaster.yml)ы

## Konsequenzen

- Fehler beim Kompilieren, Laden der Ressourcen oder Durchspielen eines Quiz werden am betroffenen Commit sichtbar.
- Das Team kann dieselbe geprüfte Datei für eine Demo herunterladen. GitHub bewahrt das Artefakt 14 Tage auf.
- Die Verfügbarkeit der Builds hängt von GitHub und den verfügbaren Actions-Kontingenten ab. Ein Push in einen offenen Pull Request kann zwei Läufe auslösen.
- Die automatisierte Prüfung deckt die Swing-Komponenten und den lokalen Kartenaustausch ab. Die Bedienung mit Maus und Tastatur im sichtbaren Fenster bleibt Teil der manuellen Abnahme.
