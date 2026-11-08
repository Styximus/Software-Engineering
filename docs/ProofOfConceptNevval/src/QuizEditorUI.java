import javax.swing.*;
import java.awt.*;

public class QuizEditorUI extends JFrame {

    private final QuizEditorService service = new QuizEditorService();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();

    public QuizEditorUI() {
        setTitle("Hello Quiz-Editor - Java Swing POC");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField questionField = new JTextField();
        JButton addButton = new JButton("Hinzufügen");

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("Frage eingeben:"), BorderLayout.WEST);
        inputPanel.add(questionField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        addButton.addActionListener(e -> {
            if (service.addQuestion(questionField.getText())) {
                listModel.addElement(service.getQuestions().size() + ". " + questionField.getText().trim());
                questionField.setText("");
            }
        });

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        content.add(inputPanel, BorderLayout.NORTH);
        content.add(new JScrollPane(new JList<>(listModel)), BorderLayout.CENTER);

        setContentPane(content);
        setVisible(true);
    }
}
