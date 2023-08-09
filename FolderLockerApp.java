import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FolderLockerApp extends JFrame {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345"; // Change this to your desired secret key

    private JTextField folderPathField;
    private JPasswordField passwordField;
    private JButton chooseFolderBtn;
    private JButton lockBtn;
    private JButton unlockBtn;

    public FolderLockerApp() {
        setTitle("Folder Locker");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        folderPathField = new JTextField();
        folderPathField.setEditable(false);

        passwordField = new JPasswordField();

        chooseFolderBtn = new JButton("Choose Folder");
        chooseFolderBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(FolderLockerApp.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFolder = fileChooser.getSelectedFile();
                    folderPathField.setText(selectedFolder.getAbsolutePath());
                }
            }
        });

        lockBtn = new JButton("Lock Folder");
        lockBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = folderPathField.getText();
                String password = new String(passwordField.getPassword());

                if (!folderPath.isEmpty() && !password.isEmpty()) {
                    try {
                        lockFolder(folderPath, password);
                        JOptionPane.showMessageDialog(FolderLockerApp.this, "Folder locked successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(FolderLockerApp.this, "Error locking the folder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(FolderLockerApp.this, "Please select a folder and enter a password.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        unlockBtn = new JButton("Unlock Folder");
        unlockBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = folderPathField.getText();
                String password = new String(passwordField.getPassword());

                if (!folderPath.isEmpty() && !password.isEmpty()) {
                    try {
                        unlockFolder(folderPath, password);
                        JOptionPane.showMessageDialog(FolderLockerApp.this, "Folder unlocked successfully!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(FolderLockerApp.this, "Error unlocking the folder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(FolderLockerApp.this, "Please select a folder and enter a password.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(new JLabel("Select a folder to lock/unlock:"), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.add(folderPathField);
        centerPanel.add(passwordField);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(chooseFolderBtn);
        buttonPanel.add(lockBtn);
        buttonPanel.add(unlockBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void lockFolder(String folderPath, String password) throws Exception {
        File folder = new File(folderPath);

        if (!folder.exists()) {
            throw new IllegalArgumentException("Folder does not exist.");
        }

        File[] files = folder.listFiles();

        // Encrypt the files in the folder
        for (File file : files) {
            if (file.isFile()) {
                encryptFile(file, password);
                file.delete(); // Remove the original file after encryption
            }
        }

        // Rename the folder to hide its content
        String renamedFolderPath = folder.getParent() + File.separator + "locked_" + folder.getName();
        folder.renameTo(new File(renamedFolderPath));
    }

    private void unlockFolder(String folderPath, String password) throws Exception {
        File folder = new File(folderPath);

        if (!folder.exists()) {
            throw new IllegalArgumentException("Folder does not exist.");
        }

        File[] files = folder.listFiles();

        // Decrypt the files in the folder
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".locked")) {
                decryptFile(file, password);
                String originalFileName = file.getName().replace(".locked", "");
                File newFile = new File(file.getParent(), originalFileName);
                file.renameTo(newFile);
            }
        }

        // Rename the folder back to its original name
        String originalFolderPath = folder.getParent() + File.separator + folder.getName().replace("locked_", "");
        folder.renameTo(new File(originalFolderPath));
    }

    private void encryptFile(File file, String password) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file.getPath() + ".locked");
        SecretKeySpec secretKey = new SecretKeySpec(password.getBytes(), ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            cos.write(buffer, 0, bytesRead);
        }

        cos.close();
        fis.close();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FolderLockerApp().setVisible(true);
            }
        });
    }
}


