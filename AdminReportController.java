/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hospital_management;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AdminReportController {
    private final JFrame parent;
    private final JComboBox<String> cbCity;
    private final JComboBox<String> cbPeriod;
    private final JComboBox<String> cbDept;
    private final JComboBox<String> cbSpec;
    private final JTable table;
    private final JButton btnSave;
    private final DefaultTableModel model;

    public AdminReportController(JFrame parent,
                                 JComboBox<String> cbCity,
                                 JComboBox<String> cbPeriod,
                                 JComboBox<String> cbDept,
                                 JComboBox<String> cbSpec,
                                 JTable table,
                                 JButton btnSave) {
        this.parent   = parent;
        this.cbCity   = cbCity;
        this.cbPeriod = cbPeriod;
        this.cbDept   = cbDept;
        this.cbSpec   = cbSpec;
        this.table    = table;
        this.btnSave  = btnSave;

        // 1) configure table model
        model = new DefaultTableModel(new Object[]{
            "Appointment ID","Patient Name","Doctor Name",
            "Department","Specialty","Date","From","To","City","Status"
        }, 0);
        table.setModel(model);

        initFilters();
        initListeners();
        loadReport();  // initial load (All)
    }

    private void initFilters() {
        // populate each combobox with "All" + DB values
        cbCity.addItem("All");       loadLookup(cbCity, "SELECT name FROM cities");
        cbDept.addItem("All");       loadLookup(cbDept, "SELECT name FROM departments");
        cbSpec.addItem("All");       loadLookup(cbSpec, "SELECT name FROM specialties");

        cbPeriod.addItem("All");
        cbPeriod.addItem("Daily");
        cbPeriod.addItem("Weekly");
        cbPeriod.addItem("Monthly");
    }

    private void loadLookup(JComboBox<String> cb, String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cb.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initListeners() {
        ItemListener reload = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loadReport();
            }
        };
        cbCity.addItemListener(reload);
        cbPeriod.addItemListener(reload);
        cbDept.addItemListener(reload);
        cbSpec.addItemListener(reload);

        btnSave.addActionListener(e -> saveReportToFile());
    }

    private void loadReport() {
        model.setRowCount(0);
        // build dynamic SQL
        StringBuilder sb = new StringBuilder(
            "SELECT a.appointment_id, CONCAT(p.first_name,' ',p.last_name), " +
            "       CONCAT(d.first_name,' ',d.last_name), dept.name, s.name, " +
            "       ts.slot_date, ts.start_time, ts.end_time, c.name, a.status " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_national_id = p.national_id " +
            "JOIN doctors d ON a.doctor_national_id = d.national_id " +
            "JOIN departments dept ON d.department_id = dept.department_id " +
            "JOIN specialties s ON d.specialty_id = s.specialty_id " +
            "JOIN time_slots ts ON a.slot_id = ts.slot_id " +
            "JOIN cities c ON p.city_id = c.city_id " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        String city = (String) cbCity.getSelectedItem();
        if (!"All".equals(city)) {
            sb.append(" AND c.name = ?");
            params.add(city);
        }

        String dept = (String) cbDept.getSelectedItem();
        if (!"All".equals(dept)) {
            sb.append(" AND dept.name = ?");
            params.add(dept);
        }

        String spec = (String) cbSpec.getSelectedItem();
        if (!"All".equals(spec)) {
            sb.append(" AND s.name = ?");
            params.add(spec);
        }

        String period = (String) cbPeriod.getSelectedItem();
        if (!"All".equals(period)) {
            sb.append(" AND ts.slot_date >= ?");
            LocalDate now = LocalDate.now();
            LocalDate from = switch (period) {
                case "Daily"   -> now.minusDays(1);
                case "Weekly"  -> now.minusWeeks(1);
                case "Monthly" -> now.minusMonths(1);
                default        -> now;
            };
            params.add(Date.valueOf(from));
        }

        sb.append(" ORDER BY ts.slot_date DESC, ts.start_time");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i+1, params.get(i));
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getDate(6),
                        rs.getTime(7),
                        rs.getTime(8),
                        rs.getString(9),
                        rs.getString(10)
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                "Error loading report:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveReportToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("report.csv"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // header
            for (int c = 0; c < model.getColumnCount(); c++) {
                pw.print(model.getColumnName(c));
                if (c < model.getColumnCount()-1) pw.print(',');
            }
            pw.println();
            // rows
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object val = model.getValueAt(r, c);
                    pw.print(val != null ? val.toString() : "");
                    if (c < model.getColumnCount()-1) pw.print(',');
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(parent,
                "Report saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                "Error saving file:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
