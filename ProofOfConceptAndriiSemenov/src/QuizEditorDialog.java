
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Quiz editor as a modal dialog.
 * The caller receives the QuizEditorService when the user saves.
 */
public class QuizEditorDialog extends JDialog {

    private static final String[] TYPES = {
            "Text",
            "Wahr/Falsch",
            "Multiple Choice",
            "Lückentext"
    };

    private final QuizEditorService service = new QuizEditorService();
    private final Consumer<QuizEditorService> onSave;

    private final DefaultListModel<String> listModel =
            new DefaultListModel<>();

    private final JList<String> questionList =
            new JList<>(listModel);

    private final JComboBox<String> typeBox =
            new JComboBox<>(TYPES);

    private final JTextField pointsField = new JTextField("1", 4);
    private final JTextField questionField = new JTextField();

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);

    private final JTextField textAnswerField = new JTextField();

    private final JComboBox<String> trueFalseBox =
            new JComboBox<>(new String[]{"Wahr", "Falsch"});

    private final JTextArea optionsArea = new JTextArea(4, 20);
    private final JTextField correctField = new JTextField();
    private final JTextField clozeAnswersField = new JTextField();

    private final JLabel statusLabel = new JLabel(" ");

    public QuizEditorDialog(
            Frame owner,
            Consumer<QuizEditorService> onSave
    ) {
        super(owner, "Quiz-Editor", true);
        this.onSave = onSave;

        setSize(820, 520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildListPanel(),
                buildFormPanel()
        );

        split.setResizeWeight(0.4);

        content.add(split, BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    private JPanel buildListPanel() {
        JButton removeButton = new JButton("Entfernen");
        removeButton.addActionListener(e -> removeSelected());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(
                BorderFactory.createTitledBorder("Fragen im Quiz")
        );

        panel.add(new JScrollPane(questionList), BorderLayout.CENTER);
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildFormPanel() {
        cardPanel.add(
                singleFieldCard("Richtige Antwort:", textAnswerField),
                TYPES[0]
        );

        cardPanel.add(
                singleFieldCard("Richtige Antwort:", trueFalseBox),
                TYPES[1]
        );

        cardPanel.add(buildMultipleChoiceCard(), TYPES[2]);

        cardPanel.add(
                singleFieldCard(
                        "Antworten (mit ; trennen):",
                        clozeAnswersField
                ),
                TYPES[3]
        );

        typeBox.addActionListener(
                e -> cards.show(
                        cardPanel,
                        (String) typeBox.getSelectedItem()
                )
        );

        JButton addButton = new JButton("Frage hinzufügen");
        addButton.addActionListener(e -> addQuestion());

        JPanel top = new JPanel(new GridBagLayout());

        addRow(top, 0, "Fragetyp:", typeBox);
        addRow(top, 1, "Punkte:", pointsField);
        addRow(top, 2, "Fragetext:", questionField);

        JLabel hint = new JLabel(
                "<html><small>"
                        + "Lückentext: Lücken im Fragetext als "
                        + "{1}, {2}, ... schreiben."
                        + "</small></html>"
        );

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(
                BorderFactory.createTitledBorder("Neue Frage")
        );

        panel.add(top, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(5, 5));
        middle.add(cardPanel, BorderLayout.NORTH);
        middle.add(hint, BorderLayout.SOUTH);

        panel.add(middle, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildMultipleChoiceCard() {
        optionsArea.setLineWrap(true);

        JPanel panel = new JPanel(new GridBagLayout());

        addRow(
                panel,
                0,
                "Optionen (eine pro Zeile):",
                new JScrollPane(optionsArea)
        );

        addRow(
                panel,
                1,
                "Richtig (z. B. 1,3):",
                correctField
        );

        return panel;
    }

    private JPanel singleFieldCard(
            String label,
            JComponent field
    ) {
        JPanel panel = new JPanel(new GridBagLayout());
        addRow(panel, 0, label, field);
        return panel;
    }

    private JPanel buildFooter() {
        JButton saveButton = new JButton("Quiz speichern");
        saveButton.addActionListener(e -> save());

        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        buttons.add(cancelButton);
        buttons.add(saveButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);

        return panel;
    }

    private void addQuestion() {
        try {
            int points = Integer.parseInt(
                    pointsField.getText().trim()
            );

            String text = questionField.getText();

            switch (typeBox.getSelectedIndex()) {
                case 0:
                    service.addTextQuestion(
                            text,
                            textAnswerField.getText(),
                            points
                    );
                    break;

                case 1:
                    service.addTrueFalseQuestion(
                            text,
                            trueFalseBox.getSelectedIndex() == 0,
                            points
                    );
                    break;

                case 2:
                    service.addMultipleChoiceQuestion(
                            text,
                            optionsArea.getText(),
                            correctField.getText(),
                            points
                    );
                    break;

                case 3:
                    service.addClozeQuestion(
                            text,
                            clozeAnswersField.getText(),
                            points
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Ungültiger Fragetyp."
                    );
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
            service.removeQuestion(
                    questionList.getSelectedIndex()
            );

            refreshList();
            setStatus("Frage entfernt.");

        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void save() {
        try {
            service.buildQuestions();

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

        int index = 1;

        for (Question question : service.getQuestions()) {
            listModel.addElement(
                    index++
                            + ". [" + question.getType() + "] "
                            + question.getQuestionText()
                            + " (" + question.getPoints() + " P.)"
            );
        }
    }

    private void clearForm() {
        questionField.setText("");
        textAnswerField.setText("");
        optionsArea.setText("");
        correctField.setText("");
        clozeAnswersField.setText("");
        pointsField.setText("1");
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private static void addRow(
            JPanel panel,
            int row,
            String label,
            JComponent field
    ) {
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

    /**
     * Quick standalone test without the rest of the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuizEditorDialog dialog = new QuizEditorDialog(
                    null,
                    service -> {
                        var questions = service.buildQuestions();

                        int totalPoints = questions.stream()
                                .mapToInt(Question::getPoints)
                                .sum();

                        System.out.println(
                                questions.size()
                                        + " Fragen, "
                                        + totalPoints
                                        + " Punkte"
                        );
                    }
            );

            dialog.setVisible(true);
        });
    }
}
