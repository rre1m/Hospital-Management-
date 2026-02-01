/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hospital_management;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;


public class AdminNotificationsController {
    private final JTable  jTableNotifications;
    private final JButton jButtonSend;

    public AdminNotificationsController(JTable jTableNotifications, JButton jButtonSend) {
        this.jTableNotifications = jTableNotifications;
        this.jButtonSend         = jButtonSend;
        init();
    }

    private void init() {
        loadNotifications();
        jButtonSend.addActionListener(e -> sendNotification());
    }

    
    private void loadNotifications() {
        DefaultTableModel model = new DefaultTableModel();
        jTableNotifications.setModel(model);

        String sql =
          "SELECT notif_id AS ID, " +
          "       CONCAT(d.first_name,' ',d.last_name) AS `Doctor Name`, " +
          "       message AS Message, sent_at AS `Sent At`, status AS Status " +
          "FROM notifications n " +
          "JOIN doctors d ON n.user_id = d.user_id " +
          "WHERE type = 'admin' " +
          "ORDER BY sent_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            Vector<String> cols = new Vector<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                cols.add(meta.getColumnLabel(i));
            }
            model.setColumnIdentifiers(cols);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.add(rs.getObject(i));
                }
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error loading admin notifications:\n" + ex.getMessage());
        }
    }

    /**
     * فتح حوار لإرسال إشعار جديد للطبيب.
     */
    private void sendNotification() {
        // تجهيز حوار الاختيار والكتابة
        JComboBox<String> cbDoctors = new JComboBox<>();
        JTextArea txtMessage = new JTextArea(5, 20);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                 "SELECT national_id, CONCAT(first_name,' ',last_name) AS name FROM doctors");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                cbDoctors.addItem(rs.getString("national_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.add(cbDoctors, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtMessage), BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(null, panel,
            "Send Notification to Doctor", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String selected = (String) cbDoctors.getSelectedItem();
        String doctorNat = selected.split(" - ")[0];
        String message   = txtMessage.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Message cannot be empty.");
            return;
        }

        // جلب user_id للطبيب
        int doctorUserId;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                 "SELECT user_id FROM doctors WHERE national_id = ?")) {
            pst.setString(1, doctorNat);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) throw new SQLException("Doctor not found: " + doctorNat);
            doctorUserId = rs.getInt("user_id");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error fetching doctor user_id:\n" + ex.getMessage());
            return;
        }

        // إدخال الإشعار
        String insSql =
          "INSERT INTO notifications (user_id, appointment_id, type, message, sent_at, status) " +
          "VALUES (?, NULL, 'admin', ?, NOW(), 'pending')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(insSql)) {
            pst.setInt(1, doctorUserId);
            pst.setString(2, message);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Notification sent.");
            loadNotifications();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error sending notification:\n" + ex.getMessage());
        }
    }
}