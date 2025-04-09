import Frames.LoginFrame;

import javax.swing.*;

public class ClinicSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}