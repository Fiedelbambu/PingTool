package exec;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Ping {
    private JFrame frame;
    private JTextField ipTextField;
    private JButton pingButton;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Ping();
            }
        });
    }

    public Ping() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Ping GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 1));

        // Textfeld zum Eingeben der IP
        ipTextField = new JTextField(15);

        // Button zum Ausführen des Pings
        pingButton = new JButton("Ping ausführen!");
        pingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ping();
            }
        });

        // Action Listener für das JTextField, um auf Enter-Taste zu reagieren
        ipTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ping();
            }
        });

        // Tabelle zum Anzeigen der Ausgabe
        tableModel = new DefaultTableModel(new Object[]{"Ping-Ergebnis"}, 0);
        resultTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(resultTable);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("IP-Adresse: "), gbc);
        gbc.gridx = 1;
        inputPanel.add(ipTextField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(pingButton);

        frame.add(inputPanel);
        frame.add(buttonPanel);
        frame.add(scrollPane);

        frame.setSize(400, 450);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private boolean isValidIPAddress(String address) {
        try {
            // Versuche, die IP-Adresse zu einem InetAddress-Objekt zu konvertieren
            InetAddress inetAddress = InetAddress.getByName(address);
            // Überprüfe, ob es sich um eine IPv4- oder IPv6-Adresse handelt
            return inetAddress instanceof java.net.Inet4Address || inetAddress instanceof java.net.Inet6Address;
        } catch (UnknownHostException e) {
            // Wenn die IP-Adresse ungültig ist oder nicht gefunden werden kann
            return false;
        }
    }

    private void ping() {
        String address = ipTextField.getText().trim();

        StringBuffer result = new StringBuffer();
        BufferedReader rdr = null;
        Runtime r = Runtime.getRuntime();

        try {
            Process p = r.exec("ping " + address);
            p.waitFor();

            rdr = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream()), "cp850"));

            String line;
            while ((line = rdr.readLine()) != null) {
                if (!line.isBlank()) {
                    // Aufteilen des Ergebnisses und Hinzufügen jeder Zeile als separate Einträge in die Tabelle
                    String[] lines = line.split("\\r?\\n");
                    for (String splitLine : lines) {
                        tableModel.addRow(new Object[]{splitLine});
                    }
                }
            }

            // Überprüfe den Exit-Code des Prozesses, um zu prüfen, ob das Pingen erfolgreich war
            int exitCode = p.exitValue();
            if (exitCode != 0) {
                JOptionPane.showMessageDialog(frame, "IP-Adresse nicht gefunden.", "Fehler", JOptionPane.ERROR_MESSAGE);
            } else {
                // Überprüfe, ob die eingegebene IP-Adresse gültig ist
                if (!isValidIPAddress(address)) {
                    JOptionPane.showMessageDialog(frame, "Ungültige IP-Adresse.", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (rdr != null) {
                    rdr.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
