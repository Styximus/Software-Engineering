package de.quizmaster;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/** Question form shared by the module menu and the standalone modal editor. */
final class QuizEditorPanel extends JPanel {
    private static final String[] TYPES = {"Text", "Wahr/Falsch", "Multiple Choice", "Lückentext"};

    private final QuizEditorService service = new QuizEditorService();
    private final BiConsumer<String, QuizEditorService> onSave;
    private final Runnable onCancel;
    private final JTextField titleField = new JTextField();

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> questionList = new JList<>(listModel);

    private final JComboBox<String> typeBox = new JComboBox<>(TYPES);
    private final JTextField pointsField = new JTextField("1", 4);
    private final JTextField questionField = new JTextField();

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = QuizTheme.clear(cards);
    private final JTextField textAnswerField = new JTextField();
    private final JComboBox<String> trueFalseBox = new JComboBox<>(new String[]{"Wahr", "Falsch"});
    private final JTextArea optionsArea = new JTextArea(4, 20);
    private final JTextField correctField = new JTextField();
    private final JTextField clozeAnswersField = new JTextField();

    private final JLabel statusLabel = new JLabel(" ");

    QuizEditorPanel(BiConsumer<String, QuizEditorService> onSave, Runnable onCancel) {
        super(new BorderLayout(12, 16));
        this.onSave = onSave;
        this.onCancel = onCancel;
        setBackground(QuizTheme.CREAM);
        statusLabel.setFont(QuizTheme.body(15));
        statusLabel.setForeground(QuizTheme.INK);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        JPanel header = QuizTheme.clear(new BorderLayout(16, 8));
        header.add(QuizTheme.label("Quiz erstellen", QuizTheme.heading(32), QuizTheme.INK), BorderLayout.NORTH);
        JLabel titleLabel = QuizTheme.label("Quizname", QuizTheme.bold(15), QuizTheme.GREEN);
        titleLabel.setLabelFor(titleField);
        titleField.setName("editor-title");
        titleField.setFont(QuizTheme.body(18));
        header.add(titleLabel, BorderLayout.WEST);
        header.add(titleField, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildListPanel(), buildFormPanel());
        split.setBackground(QuizTheme.CREAM);
        split.setBorder(null);
        split.setResizeWeight(0.35);
        add(split, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildListPanel() {
        JButton removeButton = QuizTheme.button("Frage entfernen", "editor-remove", false);
        removeButton.setEnabled(false);
        questionList.addListSelectionListener(e -> removeButton.setEnabled(!questionList.isSelectionEmpty()));
        questionList.setFont(QuizTheme.body(16));
        removeButton.addActionListener(e -> removeSelected());

        JPanel p = QuizTheme.clear(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        p.setBackground(QuizTheme.FIELD);
        p.setOpaque(true);
        p.add(QuizTheme.label("Fragen im Quiz", QuizTheme.heading(22), QuizTheme.INK), BorderLayout.NORTH);
        p.add(new JScrollPane(questionList), BorderLayout.CENTER);
        p.add(removeButton, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        questionField.setName("editor-question");
        typeBox.setName("editor-type");
        optionsArea.setName("editor-options");
        optionsArea.setFont(QuizTheme.body(16));
        correctField.setName("editor-correct");
        cardPanel.add(singleFieldCard("Richtige Antwort:", textAnswerField), TYPES[0]);
        cardPanel.add(singleFieldCard("Richtige Antwort:", trueFalseBox), TYPES[1]);
        cardPanel.add(buildMultipleChoiceCard(), TYPES[2]);
        JPanel cloze = singleFieldCard("Antworten (mit ; trennen):", clozeAnswersField);
        JPanel clozeCard = QuizTheme.clear(new BorderLayout(0, 6));
        clozeCard.add(cloze, BorderLayout.NORTH);
        clozeCard.add(QuizTheme.label("Lücken im Fragetext mit {1}, {2}, … markieren.", QuizTheme.body(14), QuizTheme.MUTED), BorderLayout.SOUTH);
        cardPanel.add(clozeCard, TYPES[3]);

        typeBox.addActionListener(e -> cards.show(cardPanel, (String) typeBox.getSelectedItem()));

        JButton addButton = QuizTheme.button("Frage hinzufügen", "editor-add", false);
        addButton.addActionListener(e -> addQuestion());

        JPanel top = QuizTheme.clear(new GridBagLayout());
        addRow(top, 0, "Fragetyp:", typeBox);
        addRow(top, 1, "Punkte:", pointsField);
        addRow(top, 2, "Fragetext:", questionField);


        JPanel p = QuizTheme.clear(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        p.setBackground(QuizTheme.FIELD);
        p.setOpaque(true);
        JPanel fields = QuizTheme.clear(new BorderLayout(0, 12));
        fields.add(QuizTheme.label("Neue Frage", QuizTheme.heading(22), QuizTheme.INK), BorderLayout.NORTH);
        fields.add(top, BorderLayout.CENTER);
        p.add(fields, BorderLayout.NORTH);
        JPanel middle = QuizTheme.clear(new BorderLayout(5, 5));
        middle.add(cardPanel, BorderLayout.NORTH);
        p.add(middle, BorderLayout.CENTER);
        p.add(addButton, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildMultipleChoiceCard() {
        optionsArea.setLineWrap(true);
        JPanel p = QuizTheme.clear(new GridBagLayout());
        addRow(p, 0, "Optionen (eine pro Zeile):", new JScrollPane(optionsArea));
        addRow(p, 1, "Richtig (z. B. 1,3):", correctField);
        return p;
    }

    private JPanel singleFieldCard(String label, JComponent field) {
        JPanel p = QuizTheme.clear(new GridBagLayout());
        addRow(p, 0, label, field);
        return p;
    }

    private JPanel buildFooter() {
        JButton saveButton = QuizTheme.button("Quiz übernehmen", "editor-save", true);
        saveButton.setToolTipText("Für diese Sitzung ins Modulmenü übernehmen");
        saveButton.addActionListener(e -> save());
        JButton cancelButton = QuizTheme.button("Abbrechen", "editor-cancel", false);
        cancelButton.addActionListener(e -> onCancel.run());

        JPanel buttons = QuizTheme.clear(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        JPanel p = QuizTheme.clear(new BorderLayout());
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
            String title = titleField.getText().strip();
            if (title.isEmpty()) throw new IllegalArgumentException("Bitte gib deinem Quiz einen Namen.");
            if (!questionField.getText().isBlank()) throw new IllegalArgumentException("Bitte die aktuelle Frage zuerst hinzufügen.");
            if (service.getQuestions().isEmpty()) throw new IllegalArgumentException("Füge zuerst eine Frage hinzu.");
            onSave.accept(title, service);
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
        JLabel caption = QuizTheme.label(label, QuizTheme.bold(14), QuizTheme.INK);
        field.setFont(QuizTheme.body(16));
        caption.setLabelFor(field);
        panel.add(caption, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

}
