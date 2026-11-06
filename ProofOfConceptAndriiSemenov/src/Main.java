import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;
import javax.swing.JOptionPane;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("Button.font", QuizUI.font(15, true));
            JFrame window = new JFrame("QuizMaster | Community Workshop");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            try {
                String address = args.length > 0 ? args[0] : "http://127.0.0.1:8765";
                Path folder = Path.of(args.length > 1 ? args[1] : "data/client/library");
                WorkshopUI workshop = new WorkshopUI(new WorkshopClient(address, System.getenv("QUIZMASTER_TOKEN")), new WorkshopStore(folder));
                window.setContentPane(workshop);
                workshop.reload();
            } catch (Exception error) {
                JOptionPane.showMessageDialog(null, error.getMessage(), "Start fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
                window.dispose(); return;
            }
            window.setMinimumSize(new Dimension(1100, 850));
            window.setSize(1280, 900);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
