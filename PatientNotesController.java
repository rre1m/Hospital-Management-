/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hospital_management;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;


public class PatientNotesController {
    private final JFrame parent;
    private final String doctorNid;      // رقم الطبيب القومي
    private final JTable notesTable;     // jTable3
    private final JButton btnAdd;        // jButton6

    public PatientNotesController(
        JFrame parent,
        String doctorNid,
        JTable notesTable,
        JButton btnAdd
    ) {
        this.parent     = parent;
        this.doctorNid  = doctorNid;
        this.notesTable = notesTable;
        this.btnAdd     = btnAdd;
        init();
        loadNotesTable();
    }

    private void init() {
        btnAdd.addActionListener(e -> showAddNoteDialog());
    }

    /** 1) تحميل كل المواعيد مع ملاحظاتها (إن وجدت) للطبيب */
    private void loadNotesTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{ "Appt ID", "Patient NID", "Date", "From", "To", "Notes" }, 0
        );
        notesTable.setModel(model);

        String sql =
            "SELECT a.appointment_id, a.patient_national_id, ts.slot_date, " +
            "       ts.start_time, ts.end_time, COALESCE(a.notes,'') AS notes " +
            "FROM appointments a " +
            "JOIN time_slots ts ON a.slot_id = ts.slot_id " +
            "WHERE a.doctor_national_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, doctorNid);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("appointment_id"),
                        rs.getString("patient_national_id"),
                        rs.getDate("slot_date"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time"),
                        rs.getString("notes")
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                "Error loading notes:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 2) حوار إضافة/تعديل الملاحظة */
    private void showAddNoteDialog() {
        // جدول المواعيد نفسه مع فلترة عن طريق JTextField
        DefaultTableModel apptsModel = (DefaultTableModel) notesTable.getModel();
        JTable apptsTable = new JTable(apptsModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(apptsModel);
        apptsTable.setRowSorter(sorter);

        //  حقل البحث
        JTextField searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        // حقل الملاحظة
        JTextArea txtNotes = new JTextArea(5, 30);
        JScrollPane notesScroll = new JScrollPane(txtNotes);

        // أزرار
        JButton btnSave   = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        // بناء الـDialog
        JPanel north = new JPanel(new BorderLayout(5,5));
        north.add(new JLabel("Search Patient:"), BorderLayout.WEST);
        north.add(searchField, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(5,5));
        center.add(new JScrollPane(apptsTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(5,5));
        south.add(new JLabel("Notes:"), BorderLayout.NORTH);
        south.add(notesScroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.add(btnSave);
        buttons.add(btnCancel);

        JDialog dialog = new JDialog(parent, "Add/Edit Note", true);
        dialog.getContentPane().setLayout(new BorderLayout(10,10));
        dialog.add(north,   BorderLayout.NORTH);
        dialog.add(center,  BorderLayout.CENTER);
        dialog.add(south,   BorderLayout.EAST);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        // فعلياً عرض الملاحظة الحالية عند اختيار صف
        apptsTable.getSelectionModel().addListSelectionListener(ev -> {
            int sel = apptsTable.getSelectedRow();
            if (sel >= 0) {
                int modelRow = apptsTable.convertRowIndexToModel(sel);
                String existingNote = (String) apptsModel.getValueAt(modelRow, 5);
                txtNotes.setText(existingNote);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            int sel = apptsTable.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an appointment.");
                return;
            }
            int modelRow  = apptsTable.convertRowIndexToModel(sel);
            int apptId    = (int) apptsModel.getValueAt(modelRow, 0);
            String note   = txtNotes.getText().trim();

            // حفظ الملاحظة
            saveNote(apptId, note);
            dialog.dispose();
            loadNotesTable();  // إعادة التحميل لعرض الملاحظة الجديدة
        });

        dialog.setVisible(true);
    }

    // 3) حفظ الملاحظة في قاعدة البيانات
    private void saveNote(int apptId, String note) {
        String sql = "UPDATE appointments SET notes = ? WHERE appointment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, note);
            pst.setInt(2, apptId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                "Error saving note:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
