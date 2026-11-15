package de.quizmaster;

import java.awt.*;
import java.awt.font.*;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicRadioButtonUI;

/** UI adapters encode answers for the existing polymorphic checkAnswer(String) contract. */
final class AnswerForm extends JPanel implements Scrollable {
    private final Question question;
    private final List<AbstractButton> choices = new ArrayList<>();
    private final List<JTextField> fields = new ArrayList<>();
    private boolean valid = true;
    private String instructions;
    private int wrappedWidth = -1;

    AnswerForm(Question question, Runnable changed) {
        this.question = question; setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        switch (question.getType()) {
            case MULTIPLE_CHOICE -> {
                String[] options = ((MultipleChoiceQuestion) question).getOptions();
                instructions = "Wähle alle richtigen Antworten aus.";
                for (int i = 0; i < options.length; i++) choice(new JCheckBox(options[i]), "answer-" + i, changed);
                valid = options.length > 0;
            }
            case TRUE_FALSE -> {
                instructions = "Ist diese Aussage richtig oder falsch?";
                ButtonGroup group = new ButtonGroup();
                for (String value : List.of("Wahr", "Falsch")) {
                    JRadioButton radio = new JRadioButton(value); group.add(radio);
                    choice(radio, "answer-" + choices.size(), changed);
                }
            }
            case TEXT -> {
                instructions = "Schreibe deine Antwort in das Textfeld.";
                field("Deine Antwort", "text-answer", changed);
            }
            case CLOZE_TEST -> {
                instructions = "Fülle jede Lücke aus. Bitte verwende kein Semikolon (;).";
                int gaps = ((ClozeTestQuestion) question).getAnswer().length;
                TreeSet<Integer> numbers = new TreeSet<>();
                Matcher m = Pattern.compile("\\{(\\d+)\\}").matcher(question.getQuestionText());
                try { while (m.find()) numbers.add(Integer.parseInt(m.group(1))); }
                catch (NumberFormatException invalidNumber) { valid = false; }
                int first = numbers.isEmpty() ? 1 : numbers.first();
                valid = valid && gaps > 0 && numbers.size() == gaps && (first == 0 || first == 1)
                    && numbers.last() == first + gaps - 1;
                if (valid) for (int i = 0; i < gaps; i++) field("Lücke {" + (first + i) + "}", "gap-" + i, changed);
                else add(QuizTheme.text("Diese Lückentext-Vorlage ist unvollständig. Bitte prüfe die Platzhalter und Antworten im Editor.", QuizTheme.body(15), QuizTheme.ERROR));
            }
        }
    }

    private void choice(AbstractButton button, String name, Runnable changed) {
        button.setName(name); button.setFont(QuizTheme.bold(16)); button.setForeground(QuizTheme.INK);
        button.putClientProperty("html.disable", true);
        button.getAccessibleContext().setAccessibleName(button.getText());
        button.setOpaque(false); button.setBackground(QuizTheme.FIELD); button.setIconTextGap(12);
        button.setIcon(new SelectionIcon(button instanceof JRadioButton));
        button.setUI(new BasicRadioButtonUI() {
            @Override public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(button.isSelected() ? QuizTheme.SOFT : QuizTheme.FIELD);
                g.fillRoundRect(1, 1, button.getWidth() - 3, button.getHeight() - 3, 16, 16);
                g.setColor(button.hasFocus() ? QuizTheme.GREEN : QuizTheme.LINE);
                g.setStroke(new BasicStroke(button.hasFocus() ? 2 : 1));
                g.drawRoundRect(1, 1, button.getWidth() - 3, button.getHeight() - 3, 16, 16);
                button.getIcon().paintIcon(button, g, 14, 18);
                g.setColor(QuizTheme.INK);
                float y = 14;
                for (TextLayout line : lines(button, Math.max(120, button.getWidth() - 70))) {
                    y += line.getAscent(); line.draw(g, 46, y); y += line.getDescent() + line.getLeading();
                }
                g.dispose();
            }
        });
        button.setAlignmentX(LEFT_ALIGNMENT); button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        button.setBorder(new EmptyBorder(14, 14, 14, 14)); button.setFocusPainted(false);
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { scrollRectToVisible(button.getBounds()); }
        });
        button.addActionListener(e -> {
            for (AbstractButton choice : choices) choice.setBackground(choice.isSelected() ? QuizTheme.SOFT : QuizTheme.FIELD);
            changed.run();
        });
        choices.add(button); add(button); add(Box.createVerticalStrut(10));
    }

    /** Standard control state glyphs; the Figma illustrations/icons remain unmodified assets. */
    private record SelectionIcon(boolean radio) implements Icon {
        public int getIconWidth() { return 20; }
        public int getIconHeight() { return 20; }
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            AbstractButton button = (AbstractButton)c; Graphics2D g = (Graphics2D)graphics.create();
            g.translate(x, y); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(button.isSelected() ? QuizTheme.GREEN : QuizTheme.CREAM);
            if (radio) g.fillOval(1, 1, 18, 18); else g.fillRoundRect(1, 1, 18, 18, 5, 5);
            g.setColor(QuizTheme.GREEN); g.setStroke(new BasicStroke(1.5f));
            if (radio) g.drawOval(1, 1, 18, 18); else g.drawRoundRect(1, 1, 18, 18, 5, 5);
            if (button.isSelected()) {
                g.setColor(QuizTheme.CREAM); g.setStroke(new BasicStroke(2));
                if (radio) g.fillOval(7, 7, 6, 6);
                else { g.drawLine(5, 10, 8, 13); g.drawLine(8, 13, 15, 6); }
            } g.dispose();
        }
    }
    @Override public void doLayout() {
        if (getWidth() > 0 && wrappedWidth != getWidth()) {
            wrappedWidth = getWidth();
            for (AbstractButton button : choices) {
                int height = 28;
                for (TextLayout line : lines(button, Math.max(120, getWidth() - 70))) height += (int)Math.ceil(line.getAscent() + line.getDescent() + line.getLeading());
                button.setPreferredSize(new Dimension(getWidth(), Math.max(58, height)));
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(58, height)));
            }
            ((BoxLayout)getLayout()).invalidateLayout(this);
        }
        super.doLayout();
    }
    private static List<TextLayout> lines(AbstractButton button, int width) {
        List<TextLayout> result = new ArrayList<>();
        FontRenderContext context = new FontRenderContext(null, true, true);
        for (String paragraph : button.getText().split("\n", -1)) {
            AttributedString value = new AttributedString(paragraph.isEmpty() ? " " : paragraph);
            value.addAttribute(TextAttribute.FONT, button.getFont());
            LineBreakMeasurer measure = new LineBreakMeasurer(value.getIterator(), context);
            while (measure.getPosition() < value.getIterator().getEndIndex()) result.add(measure.nextLayout(width));
        }
        return result;
    }
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 18; }
    public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return r.height - 18; }
    public boolean getScrollableTracksViewportWidth() { return true; }
    public boolean getScrollableTracksViewportHeight() { return false; }

    private void field(String caption, String name, Runnable changed) {
        JLabel label = QuizTheme.label(caption, QuizTheme.bold(13), QuizTheme.GREEN); label.setAlignmentX(LEFT_ALIGNMENT); add(label);
        add(Box.createVerticalStrut(7));
        JTextField field = new JTextField(); field.setName(name); field.getAccessibleContext().setAccessibleName(caption);
        field.setFont(QuizTheme.body(18)); field.setForeground(QuizTheme.INK); field.setBackground(QuizTheme.FIELD);
        field.setCaretColor(QuizTheme.GREEN); field.setAlignmentX(LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(QuizTheme.LINE), new EmptyBorder(12, 14, 12, 14)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52)); field.setPreferredSize(new Dimension(450, 52));
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { changed.run(); }
            public void removeUpdate(DocumentEvent e) { changed.run(); }
            public void changedUpdate(DocumentEvent e) { changed.run(); }
        });
        fields.add(field); add(field); add(Box.createVerticalStrut(16));
    }
    String instructions() { return instructions; }
    boolean ready() {
        if (!valid) return false;
        if (!fields.isEmpty()) return fields.stream().allMatch(f -> !f.getText().isBlank()
            && (question.getType() != QuestionType.CLOZE_TEST || !f.getText().contains(";")));
        return choices.stream().anyMatch(AbstractButton::isSelected);
    }
    String answer() {
        return switch (question.getType()) {
            case MULTIPLE_CHOICE -> java.util.stream.IntStream.range(0, choices.size()).filter(i -> choices.get(i).isSelected())
                .mapToObj(Integer::toString).collect(Collectors.joining(","));
            case TRUE_FALSE -> choices.get(0).isSelected() ? "true" : "false";
            case TEXT -> fields.get(0).getText();
            case CLOZE_TEST -> fields.stream().map(JTextField::getText).collect(Collectors.joining(";"));
        };
    }
    void lock() {
        choices.forEach(b -> { b.setEnabled(false); b.setForeground(QuizTheme.INK); });
        fields.forEach(f -> f.setEditable(false));
    }
    void restore(String answer) {
        if (answer == null) return;
        switch (question.getType()) {
            case MULTIPLE_CHOICE -> {
                for (String index : answer.split(",")) {
                    try { int i = Integer.parseInt(index.trim()); if (i >= 0 && i < choices.size()) choices.get(i).setSelected(true); }
                    catch (NumberFormatException ignored) { /* An external caller may submit malformed input. */ }
                }
            }
            case TRUE_FALSE -> choices.get(Boolean.parseBoolean(answer.trim()) ? 0 : 1).setSelected(true);
            case TEXT -> fields.get(0).setText(answer);
            case CLOZE_TEST -> { String[] values = answer.split(";", -1); for (int i = 0; i < Math.min(values.length, fields.size()); i++) fields.get(i).setText(values[i]); }
        }
    }
    void focusFirst() {
        if (!fields.isEmpty()) fields.get(0).requestFocusInWindow();
        else if (!choices.isEmpty()) choices.get(0).requestFocusInWindow();
    }
}
