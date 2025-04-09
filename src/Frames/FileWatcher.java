package Frames;

import java.io.IOException;
import java.nio.file.*;
import javax.swing.*;
import java.awt.*;

public class FileWatcher {
    private JFrame frame;
    private JLabel statusLabel;

    public FileWatcher() {
        // Create UI
        frame = new JFrame("File Watcher Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        statusLabel = new JLabel("Waiting for file changes...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(statusLabel, BorderLayout.CENTER);

        frame.setVisible(true);

        // Start file watcher
        startFileWatcher("src/PatientFiles");
    }

    private void startFileWatcher(String directoryPath) {
        new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path path = Paths.get(directoryPath);
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path fileName = (Path) event.context();

                        // Update UI on the Event Dispatch Thread
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("File " + fileName + " was " + kind.name().toLowerCase());
                        });
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileWatcher::new);
    }
}