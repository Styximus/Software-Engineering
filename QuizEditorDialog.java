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

    private static final String[] TYPES = {"Text", "Wahr/Falsch", "Multiple Choice", "Lückentext"};

    private final QuizEditorService service = new QuizEditorService();
    private final Consumer<QuizEditorService> onSave;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> questionList = new JList<>(listModel);

    private final JComboBox<String> typeBox = new JComboBox<>(TYPES);
    private final JTextField pointsField = new JTextField("1", 4);
    private final JTextField questionField = new JTextField();

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JTextField textAnswerField = new JTextField();
    private final JComboBox<String> trueFalseBox = new JComboBox<>(new String[]{"Wahr", "Falsch"});
    private final JTextArea optionsArea = new JTextArea(4, 20);
    private final JTextField correctField = new JTextField();
    private final JTextField clozeAnswersField = new JTextField();

    private final JLabel statusLabel = new JLabel(" ");

    public QuizEditorDialog(Frame owner, Consumer<QuizEditorService> onSave) {
        super(owner, "Quiz-Editor", true);
        this.onSave = onSave;

        setSize(820, 520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildListPanel(), buildFormPanel());
        split.setResizeWeight(0.4);
        content.add(split, BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    private JPanel buildListPanel() {
        JButton removeButton = new JButton("Entfernen");
        removeButton.addActionListener(e -> removeSelected());

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Fragen im Quiz"));
        p.add(new JScrollPane(questionList), BorderLayout.CENTER);
        p.add(removeButton, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        cardPanel.add(singleFieldCard("Richtige Antwort:", textAnswerField), TYPES[0]);
        cardPanel.add(singleFieldCard("Richtige Antwort:", trueFalseBox), TYPES[1]);
        cardPanel.add(buildMultipleChoiceCard(), TYPES[2]);
        cardPanel.add(singleFieldCard("Antworten (mit ; trennen):", clozeAnswersField), TYPES[3]);

        typeBox.addActionListener(e -> cards.show(cardPanel, (String) typeBox.getSelectedItem()));

        JButton addButton = new JButton("Frage hinzufügen");
        addButton.addActionListener(e -> addQuestion());

        JPanel top = new JPanel(new GridBagLayout());
        addRow(top, 0, "Fragetyp:", typeBox);
        addRow(top, 1, "Punkte:", pointsField);
        addRow(top, 2, "Fragetext:", questionField);

        JLabel hint = new JLabel("<html><small>Lückentext: Lücken im Fragetext als {1}, {2}, ... schreiben.</small></html>");

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Neue Frage"));
        p.add(top, BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(5, 5));
        middle.add(cardPanel, BorderLayout.NORTH);
        middle.add(hint, BorderLayout.SOUTH);
        p.add(middle, BorderLayout.CENTER);
        p.add(addButton, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildMultipleChoiceCard() {
        optionsArea.setLineWrap(true);
        JPanel p = new JPanel(new GridBagLayout());
        addRow(p, 0, "Optionen (eine pro Zeile):", new JScrollPane(optionsArea));
        addRow(p, 1, "Richtig (z. B. 1,3):", correctField);
        return p;
    }

    private JPanel singleFieldCard(String label, JComponent field) {
        JPanel p = new JPanel(new GridBagLayout());
        addRow(p, 0, label, field);
        return p;
    }

    private JPanel buildFooter() {
        JButton saveButton = new JButton("Quiz speichern");
        saveButton.addActionListener(e -> save());
        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        JPanel p = new JPanel(new BorderLayout());
        p.add(statusLabel, BorderLayout.CENTER);
        p.add(buttons, BorderLayout.EAST);
        return p;
    }

    private void addQuestion() {
        try {
            int points = Integer.parseInt(pointsField.getText().trim());
            String text = questionField.getText();
            switch (typeBox.getSelectedIndex()) {
                case 0:
                    service.addTextQuestion(text, textAnswerField.getText(), points);
                    break;
                case 1:
                    service.addTrueFalseQuestion(text, trueFalseBox.getSelectedIndex() == 0, points);
                    break;
                case 2:
                    service.addMultipleChoiceQuestion(text, optionsArea.getText(), correctField.getText(), points);
                    break;
                default:
                    service.addClozeQuestion(text, clozeAnswersField.getText(), points);
                    break;
            }
            refreshList();
            clearForm();
            setStatus("Frage hinzugefügt.");
        } catch (NumberFormatException ex) {
            setStatus("Die Punktzahl muss eine ganze Zahl sein.");
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void removeSelected() {
        try {
            service.removeQuestion(questionList.getSelectedIndex());
            refreshList();
            setStatus("Frage entfernt.");
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void save() {
        try {
            service.buildQuiz(1); // nur zur Validierung (mind. eine Frage)
            if (onSave != null) {
                onSave.accept(service);
            }
            dispose();
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void refreshList() {
        listModel.clear();
        int i = 1;
        for (Question q : service.getQuestions()) {
            listModel.addElement(i++ + ". [" + q.getType() + "] " + q.getQuestionText() + " (" + q.getPoints() + " P.)");
        }
    }

    private void clearForm() {
        questionField.setText("");
        textAnswerField.setText("");
        optionsArea.setText("");
        correctField.setText("");
        clozeAnswersField.setText("");
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private static void addRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
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
