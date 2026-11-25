package de.quizmaster;

/** Local demonstration entry point for the shared quiz view. */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JFrame frame = new javax.swing.JFrame("Wissensinsel · QuizMaster");
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ModuleMenu());
            frame.setSize(1440, 960); frame.setMinimumSize(new java.awt.Dimension(1100, 800));
            frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
}
