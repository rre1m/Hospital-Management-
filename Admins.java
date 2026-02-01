
package com.mycompany.hospital_management;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.sql.*;
import java.math.BigDecimal;
import java.awt.GridLayout;

import java.awt.Color;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.sql.*;
import javax.swing.event.ChangeListener;
 import java.util.*; 
import java.util.stream.Collectors;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
public class Admins extends javax.swing.JFrame {
private int userId;

public Admins(){

 initComponents();
}
 private List<Integer> cityIdList = new ArrayList<>();
    // For Doctors filter
    private List<Integer> doctorCityIdList = new ArrayList<>();
    private List<Integer> departmentIdList = new ArrayList<>();
    private List<Integer> specialtyIdList = new ArrayList<>();
       
   
public Admins( int userId) {
    initComponents();
    this.userId = userId;
 Connection conn = null;
    try {
        conn = DBConnection.getConnection();
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Database connection failed: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
        return; 
    }

new AdminReportController(
    this,
    jComboBox15,
    jComboBox12,
   jComboBox13,
   jComboBox14,
    jTable6,
   jButton15
);

    new AppointmentController(
    jTable3,
    jComboBox9,
    jComboBox10,
    jComboBox11,
    jComboBox8,
    jDateChooser1,
    jButton9,   
    jButton10    
);
    

new AdminNotificationsController(
    jTable4,  
    jButton13         
);

       


 
  
        loadPatientCities();
        loadGenders();
        updatePatientsTable();

        loadDoctorCities();
        loadDepartments();
        loadSpecialties();
        loadStatusFilter();
        filterDoctors();

        
         jComboBox6.addActionListener(e -> updatePatientsTable());
    jComboBox7.addActionListener(e -> updatePatientsTable());

    // 2) فلترة الأطباء
    jComboBox5.addActionListener(e -> filterDoctors());
    jComboBox2.addActionListener(e -> filterDoctors());
    jComboBox4.addActionListener(e -> filterDoctors());
    jComboBox1.addActionListener(e -> filterDoctors());

    // 3) زر الإضافة
    jButton1.addActionListener(e -> jButton1ActionPerformed(e));

    // 4) زر الحذف
    
    // 5) مستمع التعديل (لـ UPDATE فقط)
    ((DefaultTableModel)jTable1.getModel())
        .addTableModelListener(ev -> {
            if (ev.getType()!=TableModelEvent.UPDATE) return;
            int row = ev.getFirstRow();
            Object idVal = jTable1.getValueAt(row, 0);
            if (idVal!=null && !idVal.toString().isEmpty()) {
                // نفّذ التحديث في القاعدة
                // ... كود updateDoctorInDatabase(...)
            }
        });
        // Initialize listeners after populating comboboxes
       
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    }   
    
   
 private void loadPatientCities() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT city_id, name FROM cities");
             ResultSet rs = pst.executeQuery()) {
            jComboBox6.removeAllItems();
            cityIdList.clear();
            while (rs.next()) {
                cityIdList.add(rs.getInt("city_id"));
                jComboBox6.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

private void loadGenders() {
        jComboBox7.removeAllItems();
        jComboBox7.addItem("Male");
        jComboBox7.addItem("Female");
    }
private void updatePatientsTable() {
        int idx = jComboBox6.getSelectedIndex();
        Integer cityId = (idx >= 0) ? cityIdList.get(idx) : null;
        String gender = (String) jComboBox7.getSelectedItem();

        DefaultTableModel model = new DefaultTableModel();
        jTable2.setModel(model);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                 "SELECT p.id, p.national_id, p.first_name, p.middle_name, p.last_name, " +
                 "p.gender, p.date_of_birth, p.blood_type, c.name AS city, p.phone " +
                 "FROM patients p LEFT JOIN cities c ON p.city_id=c.city_id " +
                 "WHERE p.city_id=? AND p.gender=?")) {

            pst.setInt(1, cityId);
            pst.setString(2, gender);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 private void loadDoctorCities() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT city_id, name FROM cities");
             ResultSet rs = pst.executeQuery()) {
            jComboBox5.removeAllItems();
            doctorCityIdList.clear();
            jComboBox5.addItem("All");
            while (rs.next()) {
                doctorCityIdList.add(rs.getInt("city_id"));
                jComboBox5.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  private void loadDepartments() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT department_id, name FROM departments");
             ResultSet rs = pst.executeQuery()) {
            jComboBox2.removeAllItems();
            departmentIdList.clear();
            jComboBox2.addItem("All");
            while (rs.next()) {
                departmentIdList.add(rs.getInt("department_id"));
                jComboBox2.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   private void loadSpecialties() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT specialty_id, name FROM specialties");
             ResultSet rs = pst.executeQuery()) {
            jComboBox4.removeAllItems();
            specialtyIdList.clear();
            jComboBox4.addItem("All");
            while (rs.next()) {
                specialtyIdList.add(rs.getInt("specialty_id"));
                jComboBox4.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
private void loadStatusFilter() {
        jComboBox1.removeAllItems();
        jComboBox1.addItem("All");
        jComboBox1.addItem("Work Hours");
        jComboBox1.addItem("Exception");
    }
private void filterDoctors() {
   // اجلب القيم من الـ Comboboxes الصحيحة
   String city = (String) jComboBox5.getSelectedItem();
        String dept = (String) jComboBox2.getSelectedItem();
        String spec = (String) jComboBox4.getSelectedItem();
        String status = (String) jComboBox1.getSelectedItem();
        loadDoctors(city, dept, spec, status);
}
 private void loadDoctors(String city, String department, String specialty, String status) {
    // 1) Prepare table model
    DefaultTableModel model = new DefaultTableModel(
        new Object[]{"ID","Name","Specialty","Department","City",
                     "Salary","Start Time","End Time","Type"},
        0
    );
    jTable1.setModel(model);

    // 2) Build SQL depending on status
    String sql;
    if ("Exception".equals(status)) {
        // a) Only doctors with exception records
        sql = ""
          + "SELECT DISTINCT "
          + "  d.id AS doctor_id, "
          + "  CONCAT(d.first_name,' ',d.last_name) AS name, "
          + "  s.name AS specialty, "
          + "  dept.name AS department, "
          + "  c.name AS city, "
          + "  d.salary, "
          + "  ex.start_time AS start_time, "
          + "  ex.end_time   AS end_time, "
          + "  'Exception'   AS type "
          + "FROM doctors d "
          + "  JOIN exceptions ex ON d.national_id = ex.doctor_national_id "
          + "  JOIN specialties s ON d.specialty_id = s.specialty_id "
          + "  JOIN departments dept ON d.department_id = dept.department_id "
          + "  JOIN cities c ON d.city_id = c.city_id "
          + "WHERE c.name   LIKE ? "
          + "  AND dept.name LIKE ? "
          + "  AND s.name   LIKE ? ";
    }
    else {
        // b) Only doctors with work_hours (also default for any other status)
        sql = ""
          + "SELECT DISTINCT "
          + "  d.id AS doctor_id, "
          + "  CONCAT(d.first_name,' ',d.last_name) AS name, "
          + "  s.name AS specialty, "
          + "  dept.name AS department, "
          + "  c.name AS city, "
          + "  d.salary, "
          + "  wh.start_time AS start_time, "
          + "  wh.end_time   AS end_time, "
          + "  'Work Hours'  AS type "
          + "FROM doctors d "
          + "  JOIN work_hours wh ON d.national_id = wh.doctor_national_id "
          + "  JOIN specialties s ON d.specialty_id = s.specialty_id "
          + "  JOIN departments dept ON d.department_id = dept.department_id "
          + "  JOIN cities c ON d.city_id = c.city_id "
          + "WHERE c.name   LIKE ? "
          + "  AND dept.name LIKE ? "
          + "  AND s.name   LIKE ? ";
    }

    // 3) Execute and bind parameters
    try (Connection con = DBConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {

        String cityFilter  = "%" + ("All".equals(city)       ? "" : city)       + "%";
        String deptFilter  = "%" + ("All".equals(department) ? "" : department) + "%";
        String specFilter  = "%" + ("All".equals(specialty)  ? "" : specialty)  + "%";

        pst.setString(1, cityFilter);
        pst.setString(2, deptFilter);
        pst.setString(3, specFilter);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialty"),
                    rs.getString("department"),
                    rs.getString("city"),
                    rs.getBigDecimal("salary"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time"),
                    rs.getString("type")
                });
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Error loading doctors:\n" + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}


private void updateDoctorInDatabase(int docId,
            String firstName, String lastName,
            String specialtyName, String departmentName,
            String cityName, String salary,
            String startTime, String endTime, String type) {
        String sql = "UPDATE doctors " +
                     "SET first_name=?,last_name=?,specialty_id=?,department_id=?," +
                     "city_id=?,salary=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, firstName);
            pst.setString(2, lastName);
            pst.setInt(3, Integer.parseInt(getSpecialtyId(specialtyName)));
            pst.setInt(4, Integer.parseInt(getDepartmentId(departmentName)));
            pst.setInt(5, Integer.parseInt(getCityId(cityName)));
            pst.setBigDecimal(6, new BigDecimal(salary));
            pst.setInt(7, docId);
            pst.executeUpdate();
            System.out.println("Doctor updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void deleteDoctorFromDatabase(int docId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("DELETE FROM doctors WHERE id=?")) {
            pst.setInt(1, docId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Doctor deleted successfully.");
            filterDoctors();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting doctor.");
        }
    }
private String getCityId(String cityName) {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT city_id FROM cities WHERE name = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, cityName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("city_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // في حال لم يتم العثور على المدينة
    }

    // دالة لاسترجاع معرّف التخصص
    private String getSpecialtyId(String specialtyName) {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT specialty_id FROM specialties WHERE name = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, specialtyName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("specialty_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // في حال لم يتم العثور على التخصص
    }

    // دالة لاسترجاع معرّف القسم
    private String getDepartmentId(String departmentName) {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT department_id FROM departments WHERE name = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, departmentName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("department_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // في حال لم يتم العثور على القسم
    }
    
    
    
  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox<>();
        jComboBox7 = new javax.swing.JComboBox<>();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox<>();
        jPanel11 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jComboBox8 = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel24 = new javax.swing.JLabel();
        jComboBox9 = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        jComboBox10 = new javax.swing.JComboBox<>();
        jLabel26 = new javax.swing.JLabel();
        jComboBox11 = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jButton13 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jComboBox12 = new javax.swing.JComboBox<>();
        jLabel30 = new javax.swing.JLabel();
        jButton15 = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        jComboBox13 = new javax.swing.JComboBox<>();
        jLabel32 = new javax.swing.JLabel();
        jComboBox14 = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        jComboBox15 = new javax.swing.JComboBox<>();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Admins");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(3309, 862));
        jPanel1.setLayout(null);

        jPanel2.setBackground(new java.awt.Color(0, 0, 102));
        jPanel2.setPreferredSize(new java.awt.Dimension(230, 830));
        jPanel2.setLayout(null);

        jLabel1.setFont(new java.awt.Font("Sylfaen", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Care Health");
        jPanel2.add(jLabel1);
        jLabel1.setBounds(30, 70, 180, 50);

        jLabel2.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\Untitled design (4).png")); // NOI18N
        jPanel2.add(jLabel2);
        jLabel2.setBounds(70, 10, 90, 70);

        jPanel3.setBackground(new java.awt.Color(0, 0, 102));

        jLabel3.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Logout");
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel4.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\logout.png")); // NOI18N
        jLabel4.setText("jLabel4");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel3);
        jPanel3.setBounds(10, 720, 150, 40);

        jPanel4.setBackground(new java.awt.Color(0, 0, 102));
        jPanel4.setForeground(new java.awt.Color(255, 255, 255));

        jLabel5.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Patients");
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        jLabel10.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\patient.png")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel4);
        jPanel4.setBounds(0, 220, 100, 40);

        jPanel5.setBackground(new java.awt.Color(0, 0, 102));

        jLabel6.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Doctors");
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        jLabel14.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\doctor.png")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel6))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel5);
        jPanel5.setBounds(0, 270, 120, 40);

        jPanel6.setBackground(new java.awt.Color(0, 0, 102));

        jLabel7.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Appointment");
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        jLabel11.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\calendar.png")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addGap(0, 0, 0)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel11))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel6);
        jPanel6.setBounds(0, 320, 140, 40);

        jPanel7.setBackground(new java.awt.Color(0, 0, 102));

        jLabel8.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Notification Doctors");
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        jLabel12.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\notification-bell.png")); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel12)
                .addGap(0, 0, 0)
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel7);
        jPanel7.setBounds(0, 370, 190, 40);

        jPanel8.setBackground(new java.awt.Color(0, 0, 102));

        jLabel9.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Reports");
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });

        jLabel13.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\file.png")); // NOI18N
        jLabel13.setText("jLabel13");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel9)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel13))
                .addContainerGap())
        );

        jPanel2.add(jPanel8);
        jPanel8.setBounds(0, 410, 100, 50);

        jPanel17.setBackground(new java.awt.Color(0, 0, 102));

        jLabel34.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setText("Management");
        jLabel34.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel34MouseClicked(evt);
            }
        });

        jLabel35.setIcon(new javax.swing.ImageIcon("C:\\Users\\rre1r\\Downloads\\system.png")); // NOI18N

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel35)
                .addGap(0, 0, 0)
                .addComponent(jLabel34)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(jLabel35)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel17);
        jPanel17.setBounds(0, 470, 140, 40);

        jPanel1.add(jPanel2);
        jPanel2.setBounds(0, 0, 230, 890);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setPreferredSize(new java.awt.Dimension(3309, 862));
        jPanel10.setLayout(null);

        jTable2.setBackground(new java.awt.Color(239, 239, 239));
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Patient ID", "First Name", "Middle Name", "Last Name", "Gender", "Date of Birth", "Phone", "City"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jPanel10.add(jScrollPane2);
        jScrollPane2.setBounds(240, 250, 660, 402);

        jButton6.setBackground(new java.awt.Color(0, 0, 102));
        jButton6.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Delete");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton6);
        jButton6.setBounds(240, 680, 90, 30);

        jLabel23.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel23.setText("City");
        jPanel10.add(jLabel23);
        jLabel23.setBounds(250, 140, 43, 25);

        jLabel21.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel21.setText("Gender");
        jPanel10.add(jLabel21);
        jLabel21.setBounds(250, 180, 70, 25);

        jComboBox6.setBackground(new java.awt.Color(239, 239, 239));
        jPanel10.add(jComboBox6);
        jComboBox6.setBounds(340, 140, 190, 22);

        jComboBox7.setBackground(new java.awt.Color(239, 239, 239));
        jPanel10.add(jComboBox7);
        jComboBox7.setBounds(340, 180, 190, 22);

        jTabbedPane1.addTab("tab2", jPanel10);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setPreferredSize(new java.awt.Dimension(3309, 862));
        jPanel9.setLayout(null);

        jTable1.setBackground(new java.awt.Color(242, 242, 242));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Doctor ID", "Doctor Name", "Department", "Specialty", "Type", "Date", "Start Time", "End Time", "salary", "City"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel9.add(jScrollPane1);
        jScrollPane1.setBounds(200, 290, 630, 440);

        jButton1.setBackground(new java.awt.Color(0, 0, 102));
        jButton1.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Add");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel9.add(jButton1);
        jButton1.setBounds(200, 750, 70, 32);

        jButton2.setBackground(new java.awt.Color(0, 0, 102));
        jButton2.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Delete");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel9.add(jButton2);
        jButton2.setBounds(280, 750, 90, 32);

        jComboBox1.setBackground(new java.awt.Color(238, 238, 238));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Work Hours", "Exceptions" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        jPanel9.add(jComboBox1);
        jComboBox1.setBounds(310, 150, 190, 22);

        jComboBox2.setBackground(new java.awt.Color(238, 238, 238));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        jPanel9.add(jComboBox2);
        jComboBox2.setBounds(310, 190, 190, 20);

        jLabel15.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel15.setText("Status");
        jPanel9.add(jLabel15);
        jLabel15.setBounds(200, 150, 70, 25);
        jPanel9.add(jLabel16);
        jLabel16.setBounds(200, 140, 0, 0);

        jLabel17.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel17.setText("Department");
        jPanel9.add(jLabel17);
        jLabel17.setBounds(200, 190, 110, 20);

        jLabel19.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel19.setText("Specialty");
        jPanel9.add(jLabel19);
        jLabel19.setBounds(200, 235, 80, 20);

        jComboBox4.setBackground(new java.awt.Color(238, 238, 238));
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });
        jPanel9.add(jComboBox4);
        jComboBox4.setBounds(310, 230, 190, 22);

        jLabel20.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel20.setText("City");
        jPanel9.add(jLabel20);
        jLabel20.setBounds(200, 110, 40, 25);

        jComboBox5.setBackground(new java.awt.Color(238, 238, 238));
        jPanel9.add(jComboBox5);
        jComboBox5.setBounds(310, 110, 190, 22);

        jTabbedPane1.addTab("tab1", jPanel9);

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setPreferredSize(new java.awt.Dimension(3309, 862));
        jPanel11.setLayout(null);

        jLabel22.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel22.setText("Status");
        jPanel11.add(jLabel22);
        jLabel22.setBounds(210, 230, 60, 20);

        jComboBox8.setBackground(new java.awt.Color(241, 241, 241));
        jPanel11.add(jComboBox8);
        jComboBox8.setBounds(320, 230, 200, 22);

        jTable3.setBackground(new java.awt.Color(241, 241, 241));
        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Patient Name", "Doctor Name", "Department", "Specialty", "Date", "From", "To", "City", "Status"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        jPanel11.add(jScrollPane3);
        jScrollPane3.setBounds(210, 320, 930, 460);

        jLabel24.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel24.setText("City");
        jPanel11.add(jLabel24);
        jLabel24.setBounds(210, 110, 40, 20);

        jComboBox9.setBackground(new java.awt.Color(241, 241, 241));
        jPanel11.add(jComboBox9);
        jComboBox9.setBounds(320, 110, 200, 22);

        jLabel25.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel25.setText("Department");
        jPanel11.add(jLabel25);
        jLabel25.setBounds(210, 150, 110, 20);

        jComboBox10.setBackground(new java.awt.Color(241, 241, 241));
        jPanel11.add(jComboBox10);
        jComboBox10.setBounds(320, 150, 200, 22);

        jLabel26.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel26.setText("Specialty");
        jPanel11.add(jLabel26);
        jLabel26.setBounds(210, 190, 80, 20);

        jComboBox11.setBackground(new java.awt.Color(241, 241, 241));
        jPanel11.add(jComboBox11);
        jComboBox11.setBounds(320, 190, 200, 22);

        jLabel27.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel27.setText("Date");
        jPanel11.add(jLabel27);
        jLabel27.setBounds(210, 270, 40, 25);

        jDateChooser1.setBackground(new java.awt.Color(241, 241, 241));
        jPanel11.add(jDateChooser1);
        jDateChooser1.setBounds(320, 270, 200, 22);

        jButton9.setBackground(new java.awt.Color(0, 0, 102));
        jButton9.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jButton9.setForeground(new java.awt.Color(255, 255, 255));
        jButton9.setText("Add");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jPanel11.add(jButton9);
        jButton9.setBounds(210, 810, 72, 32);

        jButton10.setBackground(new java.awt.Color(0, 0, 102));
        jButton10.setFont(new java.awt.Font("Segoe UI Light", 0, 18)); // NOI18N
        jButton10.setForeground(new java.awt.Color(255, 255, 255));
        jButton10.setText("Delete");
        jPanel11.add(jButton10);
        jButton10.setBounds(290, 810, 90, 32);

        jTabbedPane1.addTab("tab3", jPanel11);

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setPreferredSize(new java.awt.Dimension(3309, 862));
        jPanel12.setLayout(null);

        jButton13.setBackground(new java.awt.Color(0, 0, 102));
        jButton13.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jButton13.setForeground(new java.awt.Color(255, 255, 255));
        jButton13.setText("Send");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        jPanel12.add(jButton13);
        jButton13.setBounds(200, 650, 110, 30);

        jTable4.setBackground(new java.awt.Color(241, 241, 241));
        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Doctor Name", "Message", "From", "To", "Date", "Status"
            }
        ));
        jScrollPane4.setViewportView(jTable4);

        jPanel12.add(jScrollPane4);
        jScrollPane4.setBounds(200, 220, 670, 402);

        jTabbedPane1.addTab("tab4", jPanel12);

        jPanel15.setPreferredSize(new java.awt.Dimension(3309, 862));

        jPanel16.setBackground(new java.awt.Color(255, 255, 255));
        jPanel16.setPreferredSize(new java.awt.Dimension(3309, 862));
        jPanel16.setLayout(null);

        jTable6.setBackground(new java.awt.Color(242, 242, 242));
        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Patient Name", "Doctor Name", "Department", "Specialty", "Date", "From", "To", "City", "Status"
            }
        ));
        jScrollPane6.setViewportView(jTable6);

        jPanel16.add(jScrollPane6);
        jScrollPane6.setBounds(230, 190, 940, 402);

        jComboBox12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox12ActionPerformed(evt);
            }
        });
        jPanel16.add(jComboBox12);
        jComboBox12.setBounds(360, 70, 210, 22);

        jLabel30.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel30.setText("Period");
        jPanel16.add(jLabel30);
        jLabel30.setBounds(230, 70, 60, 20);

        jButton15.setBackground(new java.awt.Color(0, 0, 102));
        jButton15.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jButton15.setForeground(new java.awt.Color(255, 255, 255));
        jButton15.setText("Save");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        jPanel16.add(jButton15);
        jButton15.setBounds(230, 610, 120, 30);

        jLabel31.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel31.setText("Department");
        jPanel16.add(jLabel31);
        jLabel31.setBounds(230, 110, 110, 25);

        jPanel16.add(jComboBox13);
        jComboBox13.setBounds(360, 110, 210, 22);

        jLabel32.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel32.setText("Specialty");
        jPanel16.add(jLabel32);
        jLabel32.setBounds(230, 150, 80, 20);

        jPanel16.add(jComboBox14);
        jComboBox14.setBounds(360, 150, 210, 22);

        jLabel33.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel33.setText("City");
        jPanel16.add(jLabel33);
        jLabel33.setBounds(230, 30, 40, 30);

        jPanel16.add(jComboBox15);
        jComboBox15.setBounds(360, 30, 210, 22);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, 2099, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addGap(0, 57, Short.MAX_VALUE)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, 888, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("tab6", jPanel15);

        jPanel18.setBackground(new java.awt.Color(255, 255, 255));
        jPanel18.setPreferredSize(new java.awt.Dimension(3309, 862));

        jPanel19.setBackground(new java.awt.Color(255, 255, 255));
        jPanel19.setPreferredSize(new java.awt.Dimension(3309, 862));
        jPanel19.setLayout(null);

        jLabel18.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel18.setText("City");
        jPanel19.add(jLabel18);
        jLabel18.setBounds(70, 220, 80, 25);
        jPanel19.add(jTextField1);
        jTextField1.setBounds(70, 250, 370, 30);

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });
        jPanel19.add(jTextField2);
        jTextField2.setBounds(70, 320, 370, 30);

        jLabel36.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel36.setText("Department");
        jPanel19.add(jLabel36);
        jLabel36.setBounds(70, 290, 110, 25);

        jLabel37.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jLabel37.setText("Specialty");
        jPanel19.add(jLabel37);
        jLabel37.setBounds(70, 360, 90, 25);
        jPanel19.add(jTextField3);
        jTextField3.setBounds(70, 392, 370, 30);

        jButton3.setBackground(new java.awt.Color(0, 0, 102));
        jButton3.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Save");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel19.add(jButton3);
        jButton3.setBounds(70, 440, 100, 30);

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, 4125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, 964, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("tab7", jPanel18);

        jPanel1.add(jTabbedPane1);
        jTabbedPane1.setBounds(230, -110, 1210, 980);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 3309, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 862, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
            jTabbedPane1.setSelectedIndex(0); //tab1
    updatePatientsTable();

    }//GEN-LAST:event_jLabel5MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// 1) Check that a row is actually selected
    int selectedRow = jTable1.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Please select a doctor first.", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 2) Retrieve the doctor ID from the first column of the selected row
    int doctorId = (int) jTable1.getValueAt(selectedRow, 0);

    // 3) Ask the user to confirm deletion
    int confirm = JOptionPane.showConfirmDialog(this, 
        "Are you sure you want to delete this doctor?", 
        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) {
        return; // user cancelled
    }

    // 4) Perform the deletion and refresh the table
    deleteDoctorFromDatabase(doctorId);

     // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

      int selectedRow = jTable2.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a patient to delete.");
        return;
    }

    // تأكيد الحذف
    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to delete this patient?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) return;

    // الحصول على ID المريض (نفترض أن العمود الأول هو الـ ID)
    int patientId = (int) jTable2.getValueAt(selectedRow, 0);

    try (Connection con = DBConnection.getConnection();
         PreparedStatement pst = con.prepareStatement("DELETE FROM patients WHERE id = ?")) {
        pst.setInt(1, patientId);
        pst.executeUpdate();

        // تحديث الجدول بعد الحذف
        updatePatientsTable();

        JOptionPane.showMessageDialog(this, "Patient deleted successfully.");
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error deleting patient.");
    }   
        
        
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
     jTabbedPane1.setSelectedIndex(1); 
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
     jTabbedPane1.setSelectedIndex(2); 
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel7MouseClicked

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
     jTabbedPane1.setSelectedIndex(3); 
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel8MouseClicked

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
           jTabbedPane1.setSelectedIndex(4); 
  // TODO add your handling code here:
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
 Login LoginFrame=new Login();
       LoginFrame.setVisible(true);
       LoginFrame.pack();
       LoginFrame.setLocationRelativeTo(null);   
       this.dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel34MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel34MouseClicked
                   jTabbedPane1.setSelectedIndex(5); 
// TODO add your handling code here:
    }//GEN-LAST:event_jLabel34MouseClicked

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed


    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
 
    }//GEN-LAST:event_jComboBox4ActionPerformed
private int getRoleId(String roleName) throws SQLException {
    String sql = "SELECT role_id FROM roles WHERE role_name = ?";
    try (Connection con = DBConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(sql)) {
        pst.setString(1, roleName);
        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("role_id");
            } else {
                throw new SQLException("Role not found: " + roleName);
            }
        }
    }
}


private String hashPassword(String pwd) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(pwd.getBytes(StandardCharsets.UTF_8));
        // حوّل البايتات إلى تمثيل سداسي عشري
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    } catch (NoSuchAlgorithmException e) {
        // من المفترض ألا يحدث؛ SHA-256 موجود دائماً
        throw new RuntimeException("SHA-256 algorithm not found", e);
    }
}
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
JTextField txtUsername   = new JTextField();
    JPasswordField txtPwd    = new JPasswordField();
    JTextField txtEmail      = new JTextField();
    JTextField txtNationalId = new JTextField();
    JDateChooser chooserDOB  = new JDateChooser();
    JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male","Female"});
    JTextField txtFirst      = new JTextField();
    JTextField txtLast       = new JTextField();
    JComboBox<String> cbSpec = new JComboBox<>();
    JComboBox<String> cbDept = new JComboBox<>();
    JComboBox<String> cbCity = new JComboBox<>();
    JTextField txtSalary     = new JTextField();

    // تعبئة القوائم (التخصص، القسم، المدينة)
    for (int i = 1; i < jComboBox4.getItemCount(); i++) cbSpec.addItem(jComboBox4.getItemAt(i));
    for (int i = 1; i < jComboBox2.getItemCount(); i++) cbDept.addItem(jComboBox2.getItemAt(i));
    for (int i = 1; i < jComboBox5.getItemCount(); i++) cbCity.addItem(jComboBox5.getItemAt(i));

    JPanel panel = new JPanel(new java.awt.GridLayout(0,2,5,5));
    panel.add(new JLabel("Username:"));      panel.add(txtUsername);
    panel.add(new JLabel("Password:"));      panel.add(txtPwd);
    panel.add(new JLabel("Email:"));         panel.add(txtEmail);
    panel.add(new JLabel("National ID:"));   panel.add(txtNationalId);
    panel.add(new JLabel("Gender:"));        panel.add(cbGender);
    panel.add(new JLabel("Date of Birth:")); panel.add(chooserDOB);
    panel.add(new JLabel("First Name:"));    panel.add(txtFirst);
    panel.add(new JLabel("Last Name:"));     panel.add(txtLast);
    panel.add(new JLabel("Specialty:"));     panel.add(cbSpec);
    panel.add(new JLabel("Department:"));    panel.add(cbDept);
    panel.add(new JLabel("City:"));          panel.add(cbCity);
    panel.add(new JLabel("Salary:"));        panel.add(txtSalary);

    int result = JOptionPane.showConfirmDialog(
        this, panel, "Add New Doctor",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
    );
    if (result != JOptionPane.OK_OPTION) {
        return;  // لم يضغط OK
    }

    // قراءة القيم
    String username   = txtUsername.getText().trim();
    String password   = new String(txtPwd.getPassword());
    String email      = txtEmail.getText().trim();
    String nationalId = txtNationalId.getText().trim();
    String gender     = (String) cbGender.getSelectedItem();
    java.util.Date   uDate = chooserDOB.getDate();
    java.sql.Date     dob = (uDate != null ? new java.sql.Date(uDate.getTime()) : null);
    String firstName  = txtFirst.getText().trim();
    String lastName   = txtLast.getText().trim();
    String specName   = (String) cbSpec.getSelectedItem();
    String deptName   = (String) cbDept.getSelectedItem();
    String cityName   = (String) cbCity.getSelectedItem();
    String salaryStr  = txtSalary.getText().trim();

    // نصوص الـ SQL
    String insertUser = "INSERT INTO users (username,password_hash,email,role_id) VALUES (?,?,?,?)";
    String insertDoc  = "INSERT INTO doctors "
        + "(national_id,user_id,first_name,last_name,gender,date_of_birth,"
        + "specialty_id,department_id,city_id,salary) "
        + "VALUES (?,?,?,?,?,?,?,?,?,?)";

    Connection con = null;
    try {
        con = DBConnection.getConnection();
        con.setAutoCommit(false);

        // 2) INSERT مستخدم
        int roleDoctor = getRoleId("doctor");
        try (PreparedStatement pstU = con.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
            pstU.setString(1, username);
            pstU.setString(2, hashPassword(password));
            pstU.setString(3, email);
            pstU.setInt   (4, roleDoctor);
            pstU.executeUpdate();
            ResultSet gk = pstU.getGeneratedKeys();
            if (!gk.next()) throw new SQLException("Failed to retrieve user_id");
            int newUserId = gk.getInt(1);

            // 3) INSERT طبيب
            try (PreparedStatement pstD = con.prepareStatement(insertDoc)) {
                pstD.setString(1, nationalId);
                pstD.setInt   (2, newUserId);
                pstD.setString(3, firstName);
                pstD.setString(4, lastName);
                pstD.setString(5, gender);
                pstD.setDate  (6, dob);
                pstD.setInt   (7, Integer.parseInt(getSpecialtyId(specName)));
                pstD.setInt   (8, Integer.parseInt(getDepartmentId(deptName)));
                pstD.setInt   (9, Integer.parseInt(getCityId(cityName)));
                pstD.setBigDecimal(10, new BigDecimal(salaryStr));
                pstD.executeUpdate();
            }
        }

        con.commit();
        JOptionPane.showMessageDialog(this, "Doctor and user account created successfully.");
        filterDoctors();

    } catch (Exception ex) {
        ex.printStackTrace();
        if (con != null) {
            try { con.rollback(); } catch (SQLException ignore) {}
        }
        JOptionPane.showMessageDialog(this, "Error adding doctor:\n" + ex.getMessage());

    } finally {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignore) {}
        }
    }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jComboBox12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox12ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        // 1) Read inputs from the actual text-field names
    String city       = jTextField1.getText().trim();
    String department = jTextField2.getText().trim();
    String specialty  = jTextField3.getText().trim();

    // 2) Validate non-empty
    if (city.isEmpty() || department.isEmpty() || specialty.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "All fields must be filled.",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    // 3) Confirm save
    int option = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to save?",
        "Confirm Save",
        JOptionPane.YES_NO_OPTION
    );
    if (option != JOptionPane.YES_OPTION) return;

    // 4) Save into database
    try (Connection con = DBConnection.getConnection()) {
        con.setAutoCommit(false);

        // Insert city if not exists
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT IGNORE INTO cities(name) VALUES(?)")) {
            pst.setString(1, city);
            pst.executeUpdate();
        }
        // Insert department
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT IGNORE INTO departments(name) VALUES(?)")) {
            pst.setString(1, department);
            pst.executeUpdate();
        }
        // Insert specialty
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT IGNORE INTO specialties(name) VALUES(?)")) {
            pst.setString(1, specialty);
            pst.executeUpdate();
        }

        con.commit();
        JOptionPane.showMessageDialog(
            this,
            "Saved successfully.",
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
            this,
            "Error during save:\n" + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    

        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed


    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox10;
    private javax.swing.JComboBox<String> jComboBox11;
    private javax.swing.JComboBox<String> jComboBox12;
    private javax.swing.JComboBox<String> jComboBox13;
    private javax.swing.JComboBox<String> jComboBox14;
    private javax.swing.JComboBox<String> jComboBox15;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JComboBox<String> jComboBox8;
    private javax.swing.JComboBox<String> jComboBox9;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable6;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
}
