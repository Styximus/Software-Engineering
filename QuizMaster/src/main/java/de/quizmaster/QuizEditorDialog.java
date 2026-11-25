package de.quizmaster;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Quiz-Editor als modaler Dialog (schließt nicht die ganze Anwendung).
 * Beim Speichern wird onSave mit dem Service aufgerufen; dort kann der Aufrufer
 * service.buildQuiz(id) abholen.
 */
public class QuizEditorDialog extends JDialog {

    public QuizEditorDialog(Frame owner, Consumer<QuizEditorService> onSave) {
        super(owner, "Quiz-Editor", true);
        setContentPane(new QuizEditorPanel((title, service) -> {
            if (onSave != null) onSave.accept(service);
            dispose();
        }, this::dispose));
        setSize(900, 620);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /** Zum schnellen Testen ohne das restliche Spiel. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuizEditorDialog dialog = new QuizEditorDialog(null, s -> {
                Quiz quiz = s.buildQuiz(1);
                System.out.println(quiz.getQuestions().length + " Fragen, " + quiz.getMaxPoints() + " Punkte");
            });
            dialog.setVisible(true);
        });
    }
}
