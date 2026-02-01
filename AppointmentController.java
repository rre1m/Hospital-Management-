/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hospital_management;

import com.toedter.calendar.JDateChooser;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
//في الادمن 
public class AppointmentController {

    private final JTable            jTable3;
    private final JComboBox<String> jComboBox9;    // City
    private final JComboBox<String> jComboBox10;   // Department
    private final JComboBox<String> jComboBox11;   // Specialty
    private final JComboBox<String> jComboBox8;    // Status
    private final JDateChooser      jDateChooser1; // Date
    private final JButton           jButtonAdd;    // Add
    private final JButton           jButtonDelete; // Delete

    public AppointmentController(
        JTable jTable3,
        JComboBox<String> jComboBox9,
        JComboBox<String> jComboBox10,
        JComboBox<String> jComboBox11,
        JComboBox<String> jComboBox8,
        JDateChooser jDateChooser1,
        JButton jButtonAdd,
        JButton jButtonDelete
    ) {
        this.jTable3       = jTable3;
        this.jComboBox9    = jComboBox9;
        this.jComboBox10   = jComboBox10;
        this.jComboBox11   = jComboBox11;
        this.jComboBox8    = jComboBox8;
        this.jDateChooser1 = jDateChooser1;
        this.jButtonAdd    = jButtonAdd;
        this.jButtonDelete = jButtonDelete;

        init();
    }

    private void init() {
        // 1. تحميل خيارات الفلاتر
        loadCities();
        loadDepartments();
        loadSpecialties();
        loadStatus();

        // 2. اعادة تحميل الجدول عند تغير أي فلتر أو التاريخ
        ActionListener reload = e -> loadAppointments();
        jComboBox9 .addActionListener(reload);
        jComboBox10.addActionListener(reload);
        jComboBox11.addActionListener(reload);
        jComboBox8 .addActionListener(reload);
        jDateChooser1.addPropertyChangeListener(
            (PropertyChangeListener) e -> {
                if ("date".equals(e.getPropertyName())) loadAppointments();
            }
        );

        // 3. ربط أزرار الإضافة والحذف
        jButtonAdd   .addActionListener(e -> addAppointment());
        jButtonDelete.addActionListener(e -> deleteAppointment());

        // 4. أول تحميل للجدول
        loadAppointments();
    }

    private void loadCities() {
        jComboBox9.removeAllItems();
        jComboBox9.addItem("All");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name FROM cities");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                jComboBox9.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadDepartments() {
        jComboBox10.removeAllItems();
        jComboBox10.addItem("All");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name FROM departments");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                jComboBox10.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadSpecialties() {
        jComboBox11.removeAllItems();
        jComboBox11.addItem("All");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT name FROM specialties");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                jComboBox11.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadStatus() {
        jComboBox8.removeAllItems();
        jComboBox8.addItem("All");
        jComboBox8.addItem("booked");
        jComboBox8.addItem("completed");
        jComboBox8.addItem("cancelled");
    }

    private void loadAppointments() {
        DefaultTableModel model = new DefaultTableModel();
        jTable3.setModel(model);

        String sql =
          "SELECT " +
          "  a.appointment_id AS `Appointment ID`, " +
          "  CONCAT(p.first_name,' ',p.last_name) AS `Patient Name`, " +
          "  CONCAT(d.first_name,' ',d.last_name) AS `Doctor Name`, " +
          "  dept.name AS Department, " +
          "  spec.name AS Specialty, " +
          "  ts.slot_date AS `Date`, " +
          "  ts.start_time AS `From`, " +
          "  ts.end_time AS `To`, " +
          "  c.name AS City, " +
          "  a.status AS Status " +
          "FROM appointments a " +
          "JOIN patients p    ON a.patient_national_id = p.national_id " +
          "JOIN doctors d     ON a.doctor_national_id  = d.national_id " +
          "JOIN specialties spec ON d.specialty_id    = spec.specialty_id " +
          "JOIN departments dept ON d.department_id   = dept.department_id " +
          "JOIN time_slots ts ON a.slot_id           = ts.slot_id " +
          "JOIN cities c     ON d.city_id            = c.city_id " +
          "WHERE (? = 'All' OR c.name    = ?) " +
          "  AND (? = 'All' OR dept.name = ?) " +
          "  AND (? = 'All' OR spec.name = ?) " +
          "  AND (? = 'All' OR a.status = ?) " +
          "  AND (? IS NULL       OR ts.slot_date = ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            String city   = (String) jComboBox9.getSelectedItem();
            String dept   = (String) jComboBox10.getSelectedItem();
            String spec   = (String) jComboBox11.getSelectedItem();
            String status = (String) jComboBox8.getSelectedItem();
            java.sql.Date date = jDateChooser1.getDate() == null
                                ? null
                                : new java.sql.Date(jDateChooser1.getDate().getTime());

            pst.setString(1, city); pst.setString(2, city);
            pst.setString(3, dept); pst.setString(4, dept);
            pst.setString(5, spec); pst.setString(6, spec);
            pst.setString(7, status); pst.setString(8, status);
            pst.setDate(9, date); pst.setDate(10, date);

            ResultSet rs = pst.executeQuery();
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
        }
    }

    private void addAppointment() {
        // مثال بسيط على حجز موعد جديد:
        // فتح حوار لاختيار patient_national_id و doctor_national_id و slot_id
        JTextField txtPat = new JTextField();
        JTextField txtDoc = new JTextField();
        JTextField txtSlot = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0,2,5,5));
        panel.add(new JLabel("Patient National ID:")); panel.add(txtPat);
        panel.add(new JLabel("Doctor  National ID:")); panel.add(txtDoc);
        panel.add(new JLabel("Time Slot ID:"));         panel.add(txtSlot);

        int res = JOptionPane.showConfirmDialog(
            null, panel, "New Appointment",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) return;

        String pNat = txtPat.getText().trim();
        String dNat = txtDoc.getText().trim();
        int slotId;
        try {
            slotId = Integer.parseInt(txtSlot.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid Slot ID.");
            return;
        }

        String checkSql =
          "SELECT booked_cnt, capacity FROM time_slots WHERE slot_id = ?";
        String dupSql =
          "SELECT COUNT(*) FROM appointments WHERE patient_national_id=? AND slot_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement chk = con.prepareStatement(checkSql);
             PreparedStatement dup = con.prepareStatement(dupSql)) {

            // 1) تحقق التوفر
            chk.setInt(1, slotId);
            ResultSet r1 = chk.executeQuery();
            if (!r1.next()) {
                JOptionPane.showMessageDialog(null, "The slot does not exist.");
                return;
            }
            int booked   = r1.getInt("booked_cnt");
            int capacity = r1.getInt("capacity");
            if (booked >= capacity) {
                JOptionPane.showMessageDialog(null, "The booking is full.");
                return;
            }

            // 2) تحقق من حجز مكرر
            dup.setString(1, pNat);
            dup.setInt(2, slotId);
            ResultSet r2 = dup.executeQuery();
            if (r2.next() && r2.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "This slot has already been booked.");
                return;
            }

            // 3) إجراء الحجز داخل معاملة
            con.setAutoCommit(false);
            try (PreparedStatement ins = con.prepareStatement(
                     "INSERT INTO appointments " +
                     "(patient_national_id, doctor_national_id, slot_id, status) " +
                     "VALUES (?,?,?,?)");
                 PreparedStatement upd = con.prepareStatement(
                     "UPDATE time_slots SET booked_cnt = booked_cnt + 1 WHERE slot_id = ?")
            ) {
                ins.setString(1, pNat);
                ins.setString(2, dNat);
                ins.setInt   (3, slotId);
                ins.setString(4, "booked");
                ins.executeUpdate();

                upd.setInt(1, slotId);
                upd.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(null, "The appointment has been booked successfully.");
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

            loadAppointments();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                null,"Error while booking:\n" + ex.getMessage()
            );
        }
    }

    private void deleteAppointment() {
        int row = jTable3.getSelectedRow();
        if (row < 0) return;
        int id = (int) jTable3.getValueAt(row, 0);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                 "DELETE FROM appointments WHERE appointment_id = ?")
        ) {
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null,"The appointment has been deleted.");
            loadAppointments();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while deleting:\n" + ex.getMessage());
        }
    } 
}
