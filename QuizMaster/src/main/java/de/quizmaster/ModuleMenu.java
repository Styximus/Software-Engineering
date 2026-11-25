package de.quizmaster;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import de.quizmaster.workshop.WorkshopConnection;
import java.nio.file.Path;

/** Navigation and quiz library for one desktop session. */
final class ModuleMenu extends JPanel {
    private record Module(String title, String topic, Npc npc) {
        @Override public String toString() { return title; }
    }

    private final DefaultComboBoxModel<Module> modules = new DefaultComboBoxModel<>();
    private WorkshopConnection workshop;
    private final JComboBox<Module> selector = new JComboBox<>(modules);
    private final JPanel library = new JPanel(new GridBagLayout()) {
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            QuizTheme.paintCover((Graphics2D) graphics, QuizTheme.image("island"), 0, 0, getWidth(), getHeight());
        }
    };

    ModuleMenu() {
        super(new BorderLayout());
        modules.addElement(new Module("Biologie entdecken", "Der Waldgarten", DemoQuizzes.biology()));
        selector.setName("modules");
        selector.setFont(QuizTheme.body(18));

        JPanel card = new QuizTheme.Card(new BorderLayout(0, 18), QuizTheme.CREAM, 26);
        card.setBorder(new EmptyBorder(30, 34, 34, 34));
        JLabel heading = QuizTheme.label("Deine Quizbibliothek", QuizTheme.heading(32), QuizTheme.INK);
        heading.setIcon(QuizTheme.icon("compass", 30, 30));
        heading.setIconTextGap(14);
        card.add(heading, BorderLayout.NORTH);

        JPanel selection = QuizTheme.clear(new BorderLayout(0, 8));
        JLabel caption = QuizTheme.label("Quiz auswählen", QuizTheme.bold(15), QuizTheme.GREEN);
        caption.setLabelFor(selector);
        selection.add(caption, BorderLayout.NORTH);
        selection.add(selector, BorderLayout.CENTER);
        selection.add(QuizTheme.label("Eigene Quizze bleiben bis zum Schließen der App verfügbar.",
                QuizTheme.body(14), QuizTheme.MUTED), BorderLayout.SOUTH);
        card.add(selection, BorderLayout.CENTER);

        JPanel actions = QuizTheme.clear(new GridLayout(1, 2, 12, 0));
        JButton create = QuizTheme.button("Quiz erstellen", "create", false);
        create.addActionListener(event -> showScreen(new QuizEditorPanel(this::addQuiz, this::showLibrary)));
        JButton play = QuizTheme.button("Quiz öffnen", "play", true);
        play.addActionListener(event -> open((Module) selector.getSelectedItem()));
        actions.add(create);
        actions.add(play);
        JButton community = QuizTheme.button("Community Workshop", "workshop", false);
        community.addActionListener(event -> openWorkshop(community));
        actions.add(community);
        card.add(actions, BorderLayout.SOUTH);
        library.add(card);
        showLibrary();
    }

    private void openWorkshop(JButton button) {
        if (workshop != null) {
            showScreen(workshop.view(this::showScreen, this::showLibrary));
            return;
        }
        button.setEnabled(false);
        button.setText("Workshop lädt …");
        new SwingWorker<WorkshopConnection, Void>() {
            protected WorkshopConnection doInBackground() throws Exception {
                return new WorkshopConnection(Path.of(System.getProperty("user.home"), ".quizmaster", "workshop"),
                        System.getenv("QUIZMASTER_SERVER_URL"), System.getenv("QUIZMASTER_TOKEN"));
            }
            protected void done() {
                button.setEnabled(true);
                button.setText("Community Workshop");
                try {
                    workshop = get();
                    Runtime.getRuntime().addShutdownHook(new Thread(workshop::close));
                    if (library.getParent() == ModuleMenu.this)
                        showScreen(workshop.view(ModuleMenu.this::showScreen, ModuleMenu.this::showLibrary));
                } catch (Exception error) {
                    Throwable cause = error.getCause() == null ? error : error.getCause();
                    JOptionPane.showMessageDialog(ModuleMenu.this, cause.getMessage(),
                            "Workshop konnte nicht geöffnet werden", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void addQuiz(String title, QuizEditorService editor) {
        int id = modules.getSize() + 1;
        Quiz quiz = editor.buildQuiz(id);
        Npc guide = new Npc(id, new Position(0, 0), "Deine Quizbegleitung",
                "Bereit für dein selbst erstelltes Quiz?", "Mila", QuizTheme.image("mila"), quiz);
        Module module = new Module(title, "Eigenes Quiz", guide);
        modules.addElement(module);
        modules.setSelectedItem(module);
        showLibrary();
    }

    private void open(Module module) {
        Quiz quiz = module.npc().getQuiz();
        showScreen(new QuestionUserInterface(quiz.getId(), module.npc(), quiz,
                module.title(), module.topic(), this::showLibrary));
    }

    private void showLibrary() { showScreen(library); }

    private void showScreen(JPanel screen) {
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) root.setDefaultButton(null);
        removeAll();
        add(screen, BorderLayout.CENTER);
        revalidate();
        repaint();
        if (screen == library) selector.requestFocusInWindow();
    }
}
