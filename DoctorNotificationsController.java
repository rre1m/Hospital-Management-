/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hospital_management;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class DoctorNotificationsController {
    private final JTable  jTable4;
    private final String  doctorNationalId;

    /**
     * @param jTable4           الجدول في واجهة الطبيب لعرض الرسائل (عمود واحد: Message)
     * @param doctorNationalId  national_id للطبيب (CHAR(10))
     */
    public DoctorNotificationsController(JTable jTable4, String doctorNationalId) {
        this.jTable4 = jTable4;
        this.doctorNationalId = doctorNationalId;
        loadNotifications();
    }

    /** يحمل رسائل الأدمن من جدول notifications ويعرضها في jTable4 */
    private void loadNotifications() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Message"}, 0);
        jTable4.setModel(model);

        String sql =
          "SELECT n.message " +
          "FROM notifications n " +
          "JOIN doctors d ON n.user_id = d.user_id " +
          "WHERE n.type = 'admin' " +
          "  AND d.national_id = ? " +
          "ORDER BY n.sent_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, doctorNationalId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{ rs.getString("message") });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "Error loading doctor notifications:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
