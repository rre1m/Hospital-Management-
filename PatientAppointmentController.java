/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hospital_management;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeListener;
import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Vector;

public class PatientAppointmentController {
    private final JFrame parent;
    private final String patientNid;
    private final JTable appointmentTable;
    private final JTable notificationTable;
    private final JButton btnAdd, btnCancel, btnEdit;

    public PatientAppointmentController(
        JFrame parent,
        String patientNid,
        JTable appointmentTable,
        JTable notificationTable,
        JButton btnAdd,
        JButton btnCancel,
        JButton btnEdit
    ) {
        this.parent = parent;
        this.patientNid = patientNid;
        this.appointmentTable = appointmentTable;
        this.notificationTable = notificationTable;
        this.btnAdd = btnAdd;
        this.btnCancel = btnCancel;
        this.btnEdit = btnEdit;
        init();
        loadAppointmentsTable();
        loadNotificationsTable();
    }

    private void init() {
        btnAdd.addActionListener(e -> {
            showAppointmentDialog(null);
            loadAppointmentsTable();
            loadNotificationsTable();
        });
        btnCancel.addActionListener(e -> {
            cancelSelectedAppointment();
            loadAppointmentsTable();
            loadNotificationsTable();
        });
        btnEdit.addActionListener(e -> {
            editSelectedAppointment();
            loadAppointmentsTable();
            loadNotificationsTable();
        });
    }

    private void loadAppointmentsTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","Doctor","Department","Specialty","Date","From","To","Status","SlotId"}, 0
        );
        appointmentTable.setModel(model);
        appointmentTable.getColumnModel().removeColumn(
            appointmentTable.getColumnModel().getColumn(8)
        );

        String sql =
            "SELECT a.appointment_id, CONCAT(d.first_name,' ',d.last_name) AS doctor, " +
            "dept.name AS department, s.name AS specialty, ts.slot_date AS date, " +
            "ts.start_time AS from_time, ts.end_time AS to_time, a.status, ts.slot_id " +
            "FROM appointments a " +
            "JOIN doctors d ON a.doctor_national_id = d.national_id " +
            "JOIN departments dept ON d.department_id = dept.department_id " +
            "JOIN specialties s ON d.specialty_id = s.specialty_id " +
            "JOIN time_slots ts ON a.slot_id = ts.slot_id " +
            "WHERE a.patient_national_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, patientNid);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("appointment_id"),
                        rs.getString("doctor"),
                        rs.getString("department"),
                        rs.getString("specialty"),
                        rs.getDate("date"),
                        rs.getTime("from_time"),
                        rs.getTime("to_time"),
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

    private void loadNotificationsTable() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Message"}, 0);
        notificationTable.setModel(model);

        String sql =
            "SELECT message FROM notifications n " +
            "JOIN patients p ON n.user_id = p.user_id " +
            "WHERE p.national_id = ? " +
            "ORDER BY n.sent_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, patientNid);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString("message")});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                "Error loading notifications:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAppointmentDialog(Appointment existing) {
        JComboBox<String> cbDept = new JComboBox<>();
        JComboBox<String> cbSpec = new JComboBox<>();
        JComboBox<String> cbDoc  = new JComboBox<>();
        JDateChooser       dcDate = new JDateChooser();
        DefaultListModel<String> slotsModel = new DefaultListModel<>();
        JList<String>      listSlots  = new JList<>(slotsModel);

        // load departments and specialties
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT name FROM departments");
            while (rs.next()) cbDept.addItem(rs.getString(1));
            rs = st.executeQuery("SELECT name FROM specialties");
            while (rs.next()) cbSpec.addItem(rs.getString(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error loading departments/specialties.");
            return;
        }

        ItemListener reloadDocs = e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) return;
            cbDoc.removeAllItems(); slotsModel.clear();
            String dept = (String) cbDept.getSelectedItem();
            String spec = (String) cbSpec.getSelectedItem();
            String sql =
              "SELECT d.national_id, CONCAT(d.first_name,' ',d.last_name) AS name " +
              "FROM doctors d " +
              "JOIN departments dept ON d.department_id = dept.department_id " +
              "JOIN specialties s ON d.specialty_id = s.specialty_id " +
              "WHERE dept.name=? AND s.name=?";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, dept);
                ps.setString(2, spec);
                try (ResultSet r = ps.executeQuery()) {
                    while (r.next()) cbDoc.addItem(r.getString(1) + " - " + r.getString(2));
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        cbDept.addItemListener(reloadDocs);
        cbSpec.addItemListener(reloadDocs);
        if (cbDept.getItemCount()>0) cbDept.setSelectedIndex(0);
        if (cbSpec.getItemCount()>0) cbSpec.setSelectedIndex(0);

        ChangeListener reloadSlots = e -> {
            slotsModel.clear();
            if (dcDate.getDate()==null || cbDoc.getSelectedItem()==null) return;
            String docNid = ((String) cbDoc.getSelectedItem()).split(" - ")[0];
            Date date = new Date(dcDate.getDate().getTime());
            if (date.before(new Date(System.currentTimeMillis()))) {
                JOptionPane.showMessageDialog(parent, "Cannot book past dates.");
                return;
            }
            String sql =
              "SELECT slot_id, start_time, end_time FROM time_slots " +
              "WHERE doctor_national_id=? AND slot_date=? AND booked_cnt<capacity";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, docNid);
                ps.setDate(2, date);
                try (ResultSet r = ps.executeQuery()) {
                    while (r.next()) {
                        slotsModel.addElement(r.getInt("slot_id") + ": " + r.getTime("start_time") + " - " + r.getTime("end_time"));
                    }
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        dcDate.getDateEditor().addPropertyChangeListener("date", evt -> reloadSlots.stateChanged(null));
        cbDoc.addItemListener(e -> reloadSlots.stateChanged(null));

        JPanel panel = new JPanel(new GridLayout(0,2,5,5));
        panel.add(new JLabel("Department:")); panel.add(cbDept);
        panel.add(new JLabel("Specialty:"));  panel.add(cbSpec);
        panel.add(new JLabel("Doctor:"));     panel.add(cbDoc);
        panel.add(new JLabel("Date:"));       panel.add(dcDate);
        panel.add(new JLabel("Available Slots:")); panel.add(new JScrollPane(listSlots));

        JButton btnSave = new JButton("Save");
        JButton btnCancelDlg = new JButton("Cancel");
        JPanel buttons = new JPanel(); buttons.add(btnSave); buttons.add(btnCancelDlg);

        JDialog dialog = new JDialog(parent, existing == null ? "Add Appointment" : "Edit Appointment", true);
        dialog.setLayout(new BorderLayout(10,10));
        dialog.add(panel, BorderLayout.CENTER); dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setSize(450,400); dialog.setLocationRelativeTo(parent);

        btnCancelDlg.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String docSel = (String) cbDoc.getSelectedItem();
            int slotId = listSlots.getSelectedIndex()>=0
                ? Integer.parseInt(listSlots.getSelectedValue().split(":")[0]) : -1;
            if (docSel==null || slotId<0) {
                JOptionPane.showMessageDialog(dialog, "Please select doctor and slot."); return;
            }
            String docNid = docSel.split(" - ")[0];
            if (existing == null) insertAppointment(docNid, slotId);
            else updateAppointment(existing.id, docNid, slotId, existing.slotId);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void insertAppointment(String docNid, int slotId) {
        try (Connection con = DBConnection.getConnection()) {
            // insert appointment
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO appointments(patient_national_id, doctor_national_id, slot_id) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patientNid); ps.setString(2, docNid); ps.setInt(3, slotId); ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); int apptId = keys.next()? keys.getInt(1) : -1;
            // update slot count
            con.prepareStatement("UPDATE time_slots SET booked_cnt = booked_cnt + 1 WHERE slot_id = " + slotId).executeUpdate();
            if (apptId > 0) {
                // fetch details for message
                String detailsSql =
                  "SELECT d.first_name, d.last_name, ts.slot_date, ts.start_time, ts.end_time, dept.name " +
                  "FROM time_slots ts " +
                  "JOIN doctors d ON ts.doctor_national_id = d.national_id " +
                  "JOIN departments dept ON d.department_id = dept.department_id " +
                  "WHERE ts.slot_id = ?";
                PreparedStatement dp = con.prepareStatement(detailsSql);
                dp.setInt(1, slotId);
                ResultSet dr = dp.executeQuery();
                String msg = "";
                if (dr.next()) {
                    msg = String.format(
                        "Appointment booked with Dr. %s %s on %s from %s to %s at %s",
                        dr.getString(1), dr.getString(2),
                        dr.getDate(3), dr.getTime(4), dr.getTime(5), dr.getString(6)
                    );
                }
                // insert notification
                PreparedStatement np = con.prepareStatement(
                    "INSERT INTO notifications(user_id, appointment_id, type, message, sent_at, status) " +
                    "VALUES((SELECT user_id FROM patients WHERE national_id=?), ?, 'creation', ?, NOW(), 'pending')");
                np.setString(1, patientNid);
                np.setInt(2, apptId);
                np.setString(3, msg);
                np.executeUpdate();
                // schedule reminder
                String schedSql =
                  "INSERT INTO notification_schedule(appointment_id, remind_at) VALUES(?, ?)";
                PreparedStatement ns = con.prepareStatement(schedSql);
                LocalDate dt = dr.getDate(3).toLocalDate();
                LocalTime st = dr.getTime(4).toLocalTime();
                Timestamp remindAt = Timestamp.valueOf(LocalDateTime.of(dt, st).minusDays(1));
                ns.setInt(1, apptId);
                ns.setTimestamp(2, remindAt);
                ns.executeUpdate();
            }
            JOptionPane.showMessageDialog(parent, "Appointment saved.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error saving appointment: " + ex.getMessage());
        }
    }

    private void cancelSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(parent, "Select an appointment."); return; }
        int modelRow = appointmentTable.convertRowIndexToModel(row);
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        int apptId = (int) model.getValueAt(modelRow, 0);
        int slotId = (int) model.getValueAt(modelRow, 8);

        if (JOptionPane.showConfirmDialog(parent, "Cancel?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try (Connection con = DBConnection.getConnection()) {
            // delete reminders and old notifications
            PreparedStatement ds = con.prepareStatement(
                "DELETE FROM notification_schedule WHERE appointment_id = ?");
            ds.setInt(1, apptId);
            ds.executeUpdate();
            PreparedStatement dn = con.prepareStatement(
                "DELETE FROM notifications WHERE appointment_id = ?");
            dn.setInt(1, apptId);
            dn.executeUpdate();
            // fetch details for message
            String detailsSql =
              "SELECT CONCAT(d.first_name,' ',d.last_name), ts.slot_date, ts.start_time, ts.end_time, dept.name " +
              "FROM appointments a " +
              "JOIN doctors d ON a.doctor_national_id = d.national_id " +
              "JOIN time_slots ts ON a.slot_id = ts.slot_id " +
              "JOIN departments dept ON d.department_id = dept.department_id " +
              "WHERE a.appointment_id = ?";
            PreparedStatement dp = con.prepareStatement(detailsSql);
            dp.setInt(1, apptId);
            ResultSet dr = dp.executeQuery();
            String msg = "";
            if (dr.next()) {
                msg = String.format(
                    "Your appointment with Dr. %s on %s from %s to %s at %s has been cancelled.",
                    dr.getString(1), dr.getDate(2), dr.getTime(3), dr.getTime(4), dr.getString(5)
                );
            }
            // delete appointment and update slot
            PreparedStatement pa = con.prepareStatement(
                "DELETE FROM appointments WHERE appointment_id = ?");
            pa.setInt(1, apptId); pa.executeUpdate();
            con.prepareStatement(
                "UPDATE time_slots SET booked_cnt = GREATEST(booked_cnt - 1, 0) WHERE slot_id = " + slotId
            ).executeUpdate();
            // insert cancellation notification
            PreparedStatement cn = con.prepareStatement(
                "INSERT INTO notifications(user_id, appointment_id, type, message, sent_at, status) " +
                "VALUES((SELECT user_id FROM patients WHERE national_id=?), NULL, 'cancellation', ?, NOW(), 'pending')");
            cn.setString(1, patientNid);
            cn.setString(2, msg);
            cn.executeUpdate();

            model.removeRow(modelRow);
            JOptionPane.showMessageDialog(parent, "Appointment cancelled.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error cancelling appointment: " + ex.getMessage());
        }
    }

    private void editSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(parent, "Select an appointment."); return; }
        int modelRow = appointmentTable.convertRowIndexToModel(row);
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        int apptId = (int) model.getValueAt(modelRow, 0);
        int oldSlot = (int) model.getValueAt(modelRow, 8);
        showAppointmentDialog(new Appointment(apptId, oldSlot));
    }

    private void updateAppointment(int apptId, String newDocNid, int newSlot, int oldSlot) {
        try (Connection con = DBConnection.getConnection()) {
            // delete old reminders and notifications
            PreparedStatement ds = con.prepareStatement(
                "DELETE FROM notification_schedule WHERE appointment_id = ?"); ds.setInt(1, apptId); ds.executeUpdate();
            PreparedStatement dn = con.prepareStatement(
                "DELETE FROM notifications WHERE appointment_id = ?"); dn.setInt(1, apptId); dn.executeUpdate();
            // update appointment and slots counts
            PreparedStatement ua = con.prepareStatement(
                "UPDATE appointments SET doctor_national_id = ?, slot_id = ? WHERE appointment_id = ?");
            ua.setString(1, newDocNid); ua.setInt(2, newSlot); ua.setInt(3, apptId); ua.executeUpdate();
            con.prepareStatement(
                "UPDATE time_slots SET booked_cnt = booked_cnt - 1 WHERE slot_id = " + oldSlot
            ).executeUpdate();
            con.prepareStatement(
                "UPDATE time_slots SET booked_cnt = booked_cnt + 1 WHERE slot_id = " + newSlot
            ).executeUpdate();
            // fetch details for message
            String detailsSql =
              "SELECT CONCAT(d.first_name,' ',d.last_name), ts.slot_date, ts.start_time, ts.end_time, dept.name " +
              "FROM time_slots ts " +
              "JOIN doctors d ON ts.doctor_national_id = d.national_id " +
              "JOIN departments dept ON d.department_id = dept.department_id " +
              "WHERE ts.slot_id = ?";
            PreparedStatement dp = con.prepareStatement(detailsSql);
            dp.setInt(1, newSlot);
            ResultSet dr = dp.executeQuery();
            String msg = "";
            if (dr.next()) {
                msg = String.format(
                    "Your appointment has been updated with Dr. %s on %s from %s to %s at %s.",
                    dr.getString(1), dr.getDate(2), dr.getTime(3), dr.getTime(4), dr.getString(5)
                );
            }
            // insert modification notification
            PreparedStatement mn = con.prepareStatement(
                "INSERT INTO notifications(user_id, appointment_id, type, message, sent_at, status) " +
                "VALUES((SELECT user_id FROM patients WHERE national_id=?), ?, 'modification', ?, NOW(), 'pending')");
            mn.setString(1, patientNid);
            mn.setInt(2, apptId);
            mn.setString(3, msg);
            mn.executeUpdate();

            JOptionPane.showMessageDialog(parent, "Appointment updated.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error updating appointment: " + ex.getMessage());
        }
    }

    private static class Appointment { int id, slotId; Appointment(int id, int slotId) { this.id = id; this.slotId = slotId; }}
}
