
import javax.swing.*;
import java.awt.*;
import java.net.URL;


public class HelloWorldUI extends JFrame {

    private HelloWorldService service;

    public HelloWorldUI() {

        service = new HelloWorldService();

        setTitle("Hello World - Java Swing POC");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        URL imageURL = getClass().getResource("/Hello-World-Wallpaper.jpg");

        if (imageURL == null) {
            throw new IllegalStateException(
                    "hello-world.png not found in resources"
            );
        }

        ImageIcon background = new ImageIcon(imageURL);

        JPanel backgroundPanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Image image = background.getImage();

                g.drawImage(
                        image,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        this
                );
            }
        };

        backgroundPanel.setLayout(new BorderLayout());

        setContentPane(backgroundPanel);

        setVisible(true);
    }
}
