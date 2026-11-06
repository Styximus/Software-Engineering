import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Dependency-free regression checks. Screenshots render the real Swing components. */
public final class QuizTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        QuizService quiz = QuizService.demo();
        check(quiz.state() == QuizService.State.READY, "Initial state");
        rejects(() -> quiz.submit(1), IllegalStateException.class);
        rejects(quiz::next, IllegalStateException.class);
        rejects(() -> new QuizService(List.of()), IllegalArgumentException.class);
        quiz.start();
        rejects(() -> quiz.submit(-1), IllegalArgumentException.class);
        rejects(() -> quiz.submit(4), IllegalArgumentException.class);
        check(quiz.score() == 0 && quiz.state() == QuizService.State.QUESTION, "Invalid input does not mutate state");
        check(quiz.submit(1), "Correct answer");
        rejects(() -> quiz.submit(1), IllegalStateException.class);
        check(quiz.score() == 1, "No duplicate points");
        quiz.next();
        check(!quiz.submit(0), "Wrong answer");
        check(quiz.score() == 1, "No points for wrong answer");
        quiz.next();
        quiz.submit(0);
        quiz.next();
        check(quiz.state() == QuizService.State.FINISHED && quiz.score() == 2 && quiz.answered() == 3, "Mixed result");
        rejects(quiz::next, IllegalStateException.class);
        rejects(() -> quiz.submit(0), IllegalStateException.class);
        quiz.start();
        check(quiz.score() == 0 && quiz.index() == 0 && quiz.answered() == 0, "Restart clears progress");
        for (int i = 0; i < quiz.total(); i++) {
            quiz.submit(quiz.currentQuestion().correctIndex());
            quiz.next();
        }
        check(quiz.score() == 3, "Perfect result");
        quiz.start();
        for (int i = 0; i < quiz.total(); i++) {
            quiz.submit((quiz.currentQuestion().correctIndex() + 1) % 4);
            quiz.next();
        }
        check(quiz.score() == 0, "Zero score result");
        SwingUtilities.invokeAndWait(() -> {
            try { testUI(args.length == 0 ? null : Path.of(args[0])); }
            catch (Exception error) { throw new RuntimeException(error); }
        });
        System.out.println("PASS: " + checks + " checks (quiz logic + Swing interaction).");
    }

    private static void testUI(Path screenshots) throws Exception {
        QuizService service = QuizService.demo();
        QuizUI ui = new QuizUI(service);
        ui.setSize(1080, 740);
        capture(ui, screenshots, "01-start");
        click(ui, "primary");
        check(!button(ui, "primary").isEnabled(), "Checking requires selection");
        capture(ui, screenshots, "02-question");
        click(ui, "answer-0");
        click(ui, "answer-1");
        click(ui, "primary");
        check(service.score() == 1, "Changed selection is submitted");
        check(!button(ui, "answer-0").isEnabled(), "Answers locked after submission");
        capture(ui, screenshots, "03-correct");
        click(ui, "primary");
        check(!button(ui, "primary").isEnabled(), "Selection cleared for next question");
        click(ui, "answer-0");
        click(ui, "primary");
        capture(ui, screenshots, "04-incorrect");
        click(ui, "primary");
        click(ui, "answer-0");
        click(ui, "primary");
        click(ui, "primary");
        check(((JLabel) find(ui, "score")).getText().equals("2 / 3"), "Result shown in UI");
        capture(ui, screenshots, "05-result");
        click(ui, "primary");
        check(service.score() == 0 && service.index() == 0, "UI restart");
        ui.setSize(944, 681); // Minimum window size minus approximate Windows decorations.
        click(ui, "answer-1");
        click(ui, "primary");
        capture(ui, screenshots, "06-minimum-size");
    }

    private static void capture(JPanel panel, Path directory, String name) throws Exception {
        layout(panel);
        if (directory == null) return;
        Files.createDirectories(directory);
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        panel.printAll(graphics);
        graphics.dispose();
        ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container child) layout(child);
        }
    }

    private static Component find(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) return component;
            if (component instanceof Container child) {
                Component found = find(child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JButton button(Container root, String name) {
        Component component = find(root, name);
        if (!(component instanceof JButton button)) throw new AssertionError("Button missing: " + name);
        return button;
    }

    private static void click(Container root, String name) {
        JButton button = button(root, name);
        check(button.isEnabled(), "Button enabled: " + name);
        button.doClick(0);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void rejects(Runnable action, Class<? extends RuntimeException> type) {
        try { action.run(); }
        catch (RuntimeException error) { check(type.isInstance(error), "Expected " + type.getSimpleName()); return; }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
