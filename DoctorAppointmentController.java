
package com.mycompany.hospital_management;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
//مهم
public class DoctorAppointmentController {
    private final JFrame parent;
    private final String doctorNid;
    private final JTable table;
    private final JComboBox<String> cbStatusFilter;
    private final JTextField txtPatientSearch;
    private final JButton btnAdd, btnEdit, btnComplete, btnCancel;

    public DoctorAppointmentController(
        JFrame parent,
        String doctorNid,
        JTable table,
        JComboBox<String> cbStatusFilter,
        JTextField txtPatientSearch,
        JButton btnAdd,
        JButton btnEdit,
        JButton btnComplete,
        JButton btnCancel
    ) {
        this.parent = parent;
        this.doctorNid = doctorNid;
        this.table = table;
        this.cbStatusFilter = cbStatusFilter;
        this.txtPatientSearch = txtPatientSearch;
        this.btnAdd = btnAdd;
        this.btnEdit = btnEdit;
        this.btnComplete = btnComplete;
        this.btnCancel = btnCancel;
        initListeners();
        loadAppointmentsTable();
    }

    private void initListeners() {
        cbStatusFilter.addActionListener(e -> loadAppointmentsTable());
        txtPatientSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadAppointmentsTable(); }
            public void removeUpdate(DocumentEvent e) { loadAppointmentsTable(); }
            public void changedUpdate(DocumentEvent e) { loadAppointmentsTable(); }
        });
        btnAdd.addActionListener(e -> showAppointmentDialog(null));
        btnEdit.addActionListener(e -> editSelectedAppointment());
        btnComplete.addActionListener(e -> completeSelectedAppointment());
        btnCancel.addActionListener(e -> cancelSelectedAppointment());
    }

    private void loadAppointmentsTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Appointment ID","Patient Name","Date","From","To","Status","SlotId"}, 0
        );
        table.setModel(model);
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(6));

        String status = (String) cbStatusFilter.getSelectedItem();
        String patFilter = txtPatientSearch.getText().trim();

        StringBuilder sb = new StringBuilder(
            "SELECT a.appointment_id, CONCAT(p.first_name,' ',p.last_name) AS patient, " +
            "ts.slot_date AS date, ts.start_time, ts.end_time, a.status, ts.slot_id " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_national_id = p.national_id " +
            "JOIN time_slots ts ON a.slot_id = ts.slot_id " +
            "WHERE a.doctor_national_id = ?"
        );
        if (!"All".equalsIgnoreCase(status)) sb.append(" AND a.status = ?");
        if (!patFilter.isEmpty()) sb.append(" AND p.national_id LIKE ?");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sb.toString())) {
            int idx = 1;
            pst.setString(idx++, doctorNid);
            if (!"All".equalsIgnoreCase(status)) pst.setString(idx++, status.toLowerCase());
            if (!patFilter.isEmpty()) pst.setString(idx++, "%"+patFilter+"%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("appointment_id"),
                        rs.getString("patient"),
                        rs.getDate("date"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time"),
                        rs.getString("status"),
                        rs.getInt("slot_id")
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                "Error loading appointments:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAppointmentDialog(Appointment existing) {
        JTextField txtPatNid = new JTextField();
        JDateChooser dcDate = new JDateChooser();
        DefaultListModel<String> slotsModel = new DefaultListModel<>();
        JList<String> listSlots = new JList<>(slotsModel);

        if (existing != null) {
            txtPatNid.setText(existing.patientNid);
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(
                     "SELECT slot_date FROM time_slots WHERE slot_id = ?")) {
                pst.setInt(1, existing.slotId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        dcDate.setDate(rs.getDate("slot_date"));
                    }
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        dcDate.getDateEditor().addPropertyChangeListener("date", evt -> loadSlots(dcDate, slotsModel));
        if (existing != null) {
            loadSlots(dcDate, slotsModel);
            String prefix = existing.slotId + ":";
            for (int i = 0; i < slotsModel.getSize(); i++) {
                if (slotsModel.get(i).startsWith(prefix)) {
                    listSlots.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel panel = new JPanel(new GridLayout(0,1,5,5));
        panel.add(new JLabel("Patient NID:")); panel.add(txtPatNid);
        panel.add(new JLabel("Date:")); panel.add(dcDate);
        panel.add(new JLabel("Available Slots:")); panel.add(new JScrollPane(listSlots));

        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnOk); btnPanel.add(btnCancel);

        JDialog dialog = new JDialog(parent,
            existing == null ? "Add Appointment" : "Edit Appointment", true);
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(parent);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnOk.addActionListener(e -> {
            String patNid = txtPatNid.getText().trim();
            if (patNid.isEmpty() || listSlots.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(dialog,
                    "Enter patient NID and select a slot.");
                return;
            }
            int slotId = Integer.parseInt(listSlots.getSelectedValue().split(":")[0]);
            if (existing == null) {
                insertAppointment(patNid, slotId);
            } else {
                updateAppointment(existing.id, patNid, slotId, existing.slotId);
            }
            loadAppointmentsTable();
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void loadSlots(JDateChooser dcDate, DefaultListModel<String> model) {
        model.clear();
        if (dcDate.getDate() == null) return;
        String sql =
          "SELECT slot_id, start_time, end_time FROM time_slots " +
          "WHERE doctor_national_id=? AND slot_date=? AND booked_cnt < capacity";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, doctorNid);
            pst.setDate(2, new java.sql.Date(dcDate.getDate().getTime()));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addElement(
                      rs.getInt("slot_id") + ": " +
                      rs.getTime("start_time") + " - " + rs.getTime("end_time")
                    );
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void cancelSelectedAppointment() {
        int vr = table.getSelectedRow(); if (vr < 0) return;
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        int mr = table.convertRowIndexToModel(vr);
        int apId = (int) m.getValueAt(mr, 0);
        int slotId = (int) m.getValueAt(mr, 6);
        if (JOptionPane.showConfirmDialog(parent, "Cancel appointment?",
            "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement pst = con.prepareStatement(
                    "DELETE FROM appointments WHERE appointment_id=?")) {
                pst.setInt(1, apId); pst.executeUpdate();
            }
            try (PreparedStatement up = con.prepareStatement(
                    "UPDATE time_slots SET booked_cnt=GREATEST(booked_cnt-1,0) WHERE slot_id=?")) {
                up.setInt(1, slotId); up.executeUpdate();
            }
            m.removeRow(mr);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void completeSelectedAppointment() {
        int vr = table.getSelectedRow(); if (vr < 0) return;
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        int mr = table.convertRowIndexToModel(vr);
        int apId = (int) m.getValueAt(mr, 0);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                 "UPDATE appointments SET status='completed' WHERE appointment_id=?")) {
            pst.setInt(1, apId); pst.executeUpdate();
            loadAppointmentsTable();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void editSelectedAppointment() {
        int vr = table.getSelectedRow(); if (vr < 0) return;
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        int mr = table.convertRowIndexToModel(vr);
        int apId = (int) m.getValueAt(mr, 0);
        int slotId = (int) m.getValueAt(mr, 6);
        String patNid = (String) m.getValueAt(mr, 1);
        showAppointmentDialog(new Appointment(apId, patNid, slotId));
    }

    private void insertAppointment(String patNid, int slotId) {
        String sql = "INSERT INTO appointments(patient_national_id,doctor_national_id,slot_id) VALUES(?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, patNid);
            pst.setString(2, doctorNid);
            pst.setInt(3, slotId);
            pst.executeUpdate();
            try (PreparedStatement up = con.prepareStatement(
                 "UPDATE time_slots SET booked_cnt=booked_cnt+1 WHERE slot_id=?")) {
                up.setInt(1, slotId); up.executeUpdate();
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void updateAppointment(int apptId, String patNid, int newSlot, int oldSlot) {
        String sql = "UPDATE appointments SET patient_national_id=?,slot_id=? WHERE appointment_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, patNid);
            pst.setInt(2, newSlot);
            pst.setInt(3, apptId);
            pst.executeUpdate();
            try (PreparedStatement d1 = con.prepareStatement(
                    "UPDATE time_slots SET booked_cnt=GREATEST(booked_cnt-1,0) WHERE slot_id=?")) {
                d1.setInt(1, oldSlot); d1.executeUpdate();
            }
            try (PreparedStatement i1 = con.prepareStatement(
                    "UPDATE time_slots SET booked_cnt=booked_cnt+1 WHERE slot_id=?")) {
                i1.setInt(1, newSlot); i1.executeUpdate();
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private static class Appointment {
        int id;
        String patientNid;
        int slotId;
        Appointment(int id, String patientNid, int slotId) {
            this.id = id;
            this.patientNid = patientNid;
            this.slotId = slotId;
        }
    }
}
