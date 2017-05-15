
import java.io.*;
import javax.swing.JFileChooser;
import com.fazecast.jSerialComm.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.JComboBox;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author alexis
 */
public class Logger extends javax.swing.JFrame {

    // Constants
    static final int LOGGER_RECORD_SIZE = 47;
    static final int FWLOAD_OK = 0x00;
    static final int FWLOAD_FAILED = 0x01;
    static final int FWLOAD_ERROR_INSUFFICIENT_DATA = 0x02;
    static final int FWLOAD_ERROR_CSUM = 0x03;
    static final int FWLOAD_ERROR_PKT_TIMEOUT = 0x04;
    static final int FWLOAD_ERROR_NO_USB = 0x05;

    static final int FWLOAD_ERROR_IN_FWCSUM = 0x10;

    static final int FWLOAD_PACKET_TYPE_FWINFO = 0x35;
    static final int FWLOAD_PACKET_TYPE_FWDATA = 0x46;
    static final int FWLOAD_PACKET_DOWNLOAD_LOG_INFO_REQ = 0x5C;
    static final int FWLOAD_PACKET_DOWNLOAD_LOG_INFO_RESP = 0x5D;
    static final int FWLOAD_PACKET_DOWNLOAD_LOG = 0x5E;
    static final int FWLOAD_PACKET_DOWNLOAD_LOG_ACK = 0x5F;
    static final int FWLOAD_PACKET_SET_RTC = 0x67;
    static final int FWLOAD_PACKET_ERASE_LOGS = 0x82;
    static final int FWLOAD_PACKET_GET_DEVID = 0x95;
    static final int FWLOAD_PACKET_READ_DEVID = 0x96;
    static final int FWLOAD_PACKET_SET_DEVID = 0x97;

    static final int FWLOAD_PACKET_TYPE_ACK = 0xE5;

    static final int FWLOAD_PKT_TYPE_IDX = 2;
    static final int FWLOAD_PKT_FW_PKT_CNT_IDX = 3;
    static final int FWLOAD_PKT_FW_PAYLOAD_SIZE_IDX = 5;
    static final int FWLOAD_PKT_FW_PAYLOAD_IDX = 6;

    static final int FWLOAD_ACK_PKT_SIZE = 7;

    static final int FWLOAD_PKT_SIZE = 64;
    static final int FWLOAD_PKT_SIGN_B0 = 0xA5;
    static final int FWLOAD_PKT_SIGN_B1 = 0x5A;
    
    // Excel program constants
    static final double VOLTAGE = 3.3;
    static final double COUNTS = 1024.0;
    
    static final double MAX_PPM_CO = 200.0;
    static final double MAX_PPM_CO2 = 200.0;
    static final double MAX_PPM_HCL = 25.0;
    static final double MAX_PPM_NO2 = 10.0;
    static final double MAX_PPM_SO2 = 25.0;
    static final double MAX_PPM_NH3 = 100.0;
    static final double MAX_PPM_HCN = 10.0;
    static final double MAX_PPM_H2S = 25.0;
    static final double MAX_PPM_CL2 = 5;
    
    static final double NA_PPM_CO = 45.0;
    static final double NA_PPM_CO2 = 45.0;
    static final double NA_PPM_HCL = 100.0;
    static final double NA_PPM_NO2 = -325.0;
    static final double NA_PPM_SO2 = 300.0;
    static final double NA_PPM_NH3 = 90.0;
    static final double NA_PPM_HCN = 40.0;
    static final double NA_PPM_H2S = 140.0;
    static final double NA_PPM_CL2 = -325.0;
    
    static final double ZERO_CO = 0.2;
    static final double ZERO_CO2 = 0.2;
    static final double ZERO_HCL = 0.2;
    static final double ZERO_NO2 = 0.67;
    static final double ZERO_SO2 = 0.2;
    static final double ZERO_NH3 = 0.2;
    static final double ZERO_HCN = 0.2;
    static final double ZERO_H2S = 0.2;
    static final double ZERO_CL2 = 0.67;
    
    static final double GAIN_CO = 120000.0;
    static final double GAIN_CO2 = 120000.0;
    static final double GAIN_HCL = 350000.0;
    static final double GAIN_NO2 = 120000.0;
    static final double GAIN_SO2 = 120000.0;
    static final double GAIN_NH3 = 120000.0;
    static final double GAIN_HCN = 350000.0;
    static final double GAIN_H2S = 350000.0;
    static final double GAIN_CL2 = 350000.0;
    
    static final double RL_CO = 10.0;
    static final double RL_HCL = 33.0;
    static final double RL_NO2 = 33.0;
    static final double RL_S02 = 33.0;
    static final double RL_NH3 = 10.0;
    static final double RL_HCN = 10.0;
    static final double RL_H2S = 10.0;
    static final double RL_CL2 = 33.0;    
    
    // Gas sensor indexes
    static final double[] sensorsZero = {ZERO_CO, ZERO_CO2, ZERO_CL2, ZERO_NO2,
                                      ZERO_SO2,ZERO_HCN, ZERO_H2S, ZERO_NH3, ZERO_HCL};
    
    static final double[] sensorsMaxPpm = {MAX_PPM_CO, MAX_PPM_CO2, MAX_PPM_CL2, MAX_PPM_NO2,
                               MAX_PPM_SO2, MAX_PPM_HCN, MAX_PPM_H2S, MAX_PPM_NH3, MAX_PPM_HCL};
    
    static final double[] sensorsNaPpm = {NA_PPM_CO, NA_PPM_CO2, NA_PPM_CL2, NA_PPM_NO2,
                               NA_PPM_SO2, NA_PPM_HCN, NA_PPM_H2S, NA_PPM_NH3, NA_PPM_HCL};
    
    static final double[] sensorsGain = {GAIN_CO, GAIN_CO2, GAIN_CL2, GAIN_NO2,
                               GAIN_SO2, GAIN_HCN, GAIN_H2S, GAIN_NH3, GAIN_HCL};
    
    static final String[] sensorsName = {"CO", "CO2", "CL2", "NO2",
                               "SO2", "HCN", "H2S", "NH3", "HCL"};

    // My definitions
    static final int RTC_STRING_LENGTH = 20;
    static final int FWLOAD_PKT_LOG_PKT_NUM_IDX = 4;
    static final int FWLOAD_PKT_LOG_PAYLOAD_IDX = 8;
    static final int FWLOAD_PKT_LOG_BATTERY_IDX = FWLOAD_PKT_LOG_PAYLOAD_IDX + 20;
    static final int FWLOAD_PKT_LOG_ERROR_IDX = FWLOAD_PKT_LOG_BATTERY_IDX + 1;
    static final int FWLOAD_PKT_LOG_SENSORS_IDX = FWLOAD_PKT_LOG_ERROR_IDX + 4;
    static final int FWLOAD_PKT_LOG_PRESSURE_IDX = FWLOAD_PKT_LOG_SENSORS_IDX + 18;
    static final int FWLOAD_PKT_LOG_TEMPERATURE_IDX = FWLOAD_PKT_LOG_PRESSURE_IDX + 2;
    static final byte NUMBER_OF_GAS_SENSORS = 9;

    MyOpenFilter openFileFilter = new MyOpenFilter();
    MySaveFilter saveFileFilter = new MySaveFilter();
    SerialPort[] ports = null;
    File recordFile = null;
    File programFile = null;
    PrintWriter outputStream = null;
    Path tempPath = null;
    PrintWriter outputStream2 = null;
    Path tempPath2 = null;
    String selectedPortName = null;
    String recordFormat = "%s,%d,0x%x,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%f\n";
    String sampleHeaderFormat = "%s,%d,%3.1f\n";
    String gasOutputFormat = "%s,%d,%8.6f,%8.6f,%8.6f,%d,%8.6f,%d,%8.6f,%8.6f\n";
    int openedPort = -1;
    int selectedPortIndex = -1;
    int logPacketErrorCounter = 0;
    PacketListener listener = new PacketListener();
    byte[] procBuffer = new byte[FWLOAD_PKT_SIZE];
    byte[] outBuffer = new byte[FWLOAD_PKT_SIZE];
    boolean newPacket = false;
    boolean logAvailable = false;
    long totalLogs = 0;
    long logReq = 0;
    long currentDeviceId = 0;
    SimpleDateFormat myDateFormat = new SimpleDateFormat ("ss mm HH u dd MM YY");

    /**
     * Creates new form Logger
     */
    public Logger() {
        initComponents();
        
        // Initiate my custom components
        ActionListener updateClockAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Assumes clock is a JLabel
                //clockLabel.setText(new Date().toString());
                updateDateLabel();
            }
        };
        Timer t = new Timer(1000, updateClockAction);
        t.start();                
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        detectButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        closePortButton = new javax.swing.JButton();
        portsComboBox = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        readButton = new javax.swing.JButton();
        saveToButton = new javax.swing.JButton();
        eraseButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        getButton = new javax.swing.JButton();
        setButton = new javax.swing.JButton();
        deviceIdTextField = new javax.swing.JFormattedTextField();
        jPanel5 = new javax.swing.JPanel();
        syncButton = new javax.swing.JButton();
        clockLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("COM Port Settings"));

        detectButton.setText("Detect");
        detectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectButtonActionPerformed(evt);
            }
        });

        openButton.setText("Open");
        openButton.setEnabled(false);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        closePortButton.setText("Close Port");
        closePortButton.setEnabled(false);
        closePortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closePortButtonActionPerformed(evt);
            }
        });

        portsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portsComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(openButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(detectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closePortButton)
                    .addComponent(portsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(detectButton)
                    .addComponent(portsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openButton)
                    .addComponent(closePortButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Logger Data"));

        readButton.setText("Read");
        readButton.setEnabled(false);
        readButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readButtonActionPerformed(evt);
            }
        });

        saveToButton.setText("Save to");
        saveToButton.setEnabled(false);
        saveToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveToButtonActionPerformed(evt);
            }
        });

        eraseButton.setText("Erase");
        eraseButton.setEnabled(false);
        eraseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(readButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveToButton)
                .addGap(18, 18, 18)
                .addComponent(eraseButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(readButton)
                    .addComponent(saveToButton)
                    .addComponent(eraseButton))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Console Output"));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Device ID"));

        getButton.setText("Get");
        getButton.setEnabled(false);
        getButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getButtonActionPerformed(evt);
            }
        });

        setButton.setText("Set");
        setButton.setEnabled(false);
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setButtonActionPerformed(evt);
            }
        });

        deviceIdTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#"))));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(getButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(setButton, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(deviceIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 158, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getButton)
                    .addComponent(setButton)
                    .addComponent(deviceIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Real Time Clock"));

        syncButton.setText("Sync");
        syncButton.setEnabled(false);
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        clockLabel.setText(".");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(syncButton)
                .addGap(29, 29, 29)
                .addComponent(clockLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(syncButton)
                    .addComponent(clockLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/jmi_logo_icon2.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/jdiCompanyName3.png"))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("MGDS LOGGER v. 1.2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(jLabel3))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void detectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detectButtonActionPerformed
        // TODO add your handling code here:
        ports = SerialPort.getCommPorts();
        jTextArea1.append(String.format("Number of ports detedted: %d COM ports.\n", ports.length));
        portsComboBox.removeAllItems();
        if (ports.length > 0) {
            for (int k = 0; k < ports.length; k++) {
                portsComboBox.addItem(ports[k].getSystemPortName());
                jTextArea1.append(String.format("%s\n", ports[k].getDescriptivePortName()));
            }
            openButton.setEnabled(true);
            selectedPortIndex = 0;// Initially select one port
        }
    }//GEN-LAST:event_detectButtonActionPerformed

    private void portsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portsComboBoxActionPerformed
        // TODO add your handling code here:
        JComboBox cb = (JComboBox) evt.getSource();
        selectedPortName = (String) cb.getSelectedItem();
        //jTextArea1.append(String.format("Selected Port is: %s\n", selectedPortName));
        for (int k = 0; k < ports.length; k++) {
            // Search port index corresponding to port name selected
            if (ports[k].getSystemPortName().equals(selectedPortName)) {
                selectedPortIndex = k;
                //jTextArea1.append(String.format("Index found %d\n", k));
            }
        }
    }//GEN-LAST:event_portsComboBoxActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        // TODO add your handling code here:
        if (selectedPortIndex >= 0) {
            if (ports[selectedPortIndex].openPort()) {
                openedPort = selectedPortIndex;
                jTextArea1.append(String.format("Port %d opened successfully\n", openedPort));
                ports[openedPort].addDataListener(listener);

                // Enable some buttons
                closePortButton.setEnabled(true);

                //fileButton.setEnabled(true);
                //updateButton.setEnabled(true);
                readButton.setEnabled(true);
                eraseButton.setEnabled(true);
                getButton.setEnabled(true);
                setButton.setEnabled(true);
                syncButton.setEnabled(true);

                // Disable
                detectButton.setEnabled(false);
                openButton.setEnabled(false);

            } else {
                jTextArea1.append("Can not open the port\n");
            }
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void closePortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closePortButtonActionPerformed
        // TODO add your handling code here:
        if (openedPort > 0) {
            jTextArea1.append(String.format("openedPort = %d\n", openedPort));
            jTextArea1.append(String.format("ports.length = %d\n", ports.length));
            ports[openedPort].removeDataListener();
            if (ports[openedPort].closePort()) {
                //System.out.println("Port closed.");
                jTextArea1.append(String.format("Port %d closed successfully\n", openedPort));
                openedPort = -1;

                // Disable relevant buttons
                readButton.setEnabled(false);
                saveToButton.setEnabled(false);
                eraseButton.setEnabled(false);
                //fileButton.setEnabled(false);
                //updateButton.setEnabled(false);
                closePortButton.setEnabled(false);
                getButton.setEnabled(false);
                setButton.setEnabled(false);
                syncButton.setEnabled(false);

                // Enable Open button
                //openButton.setEnabled(true);
                detectButton.setEnabled(true);

            } else {
                System.out.println("Can not close the port.");
            }
        } else {
            System.out.println("No COM port was opened.");
        }
    }//GEN-LAST:event_closePortButtonActionPerformed

    private void readButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readButtonActionPerformed
        // TODO add your handling code here:
        startLogging(outBuffer); // Send command to read log data        
    }//GEN-LAST:event_readButtonActionPerformed

    private void saveToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveToButtonActionPerformed
        // TODO add your handling code here:
        jFileChooser1.setFileFilter(saveFileFilter);
        int returnVal = jFileChooser1.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            recordFile = jFileChooser1.getSelectedFile();
            if (logAvailable) {
                // Copy temp log file to location selected by user
                try {
                    //Files.copy(tempPath.toAbsolutePath(), Paths.get(recordFile.getAbsolutePath()), REPLACE_EXISTING);
                    Files.copy(tempPath2.toAbsolutePath(), Paths.get(recordFile.getAbsolutePath()), REPLACE_EXISTING);
                    System.out.println("Saving to: " + recordFile.getAbsolutePath());
                } catch (IOException error) {
                    jTextArea1.append(String.format("%s\n", error));
                }
            }

        } else {
            System.out.println("File access cancelled by user.");
        }
    }//GEN-LAST:event_saveToButtonActionPerformed

    private void eraseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraseButtonActionPerformed
        // TODO add your handling code here:
      sendEraseLogRequestPacket(outBuffer);
    }//GEN-LAST:event_eraseButtonActionPerformed

    private void getButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getButtonActionPerformed
        // TODO add your handling code here:
        sendGetIdResquestPacket(outBuffer);
        
    }//GEN-LAST:event_getButtonActionPerformed

    private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
        // TODO add your handling code here:
        try{
            deviceIdTextField.commitEdit();
            currentDeviceId = ((Number) deviceIdTextField.getValue()).longValue();
        } catch(ParseException error){
            jTextArea1.append(String.format("%s\n", error));
        }
        if(currentDeviceId > 0){
            //Send packet through serial port
            sendSetIdPacket(outBuffer, currentDeviceId);
        }
        else{
            jTextArea1.append("Not valid ID, only positives numbers allowed\n");
        }
    }//GEN-LAST:event_setButtonActionPerformed

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
        // TODO add your handling code here:
        sendSetRtcPacket(outBuffer);
    }//GEN-LAST:event_syncButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        // Close all that is oppened
        if(openedPort >= 0){
            ports[openedPort].closePort();
        }
        if(outputStream != null){
            outputStream.close();
        }
        if(outputStream2 != null){
            outputStream2.close();
        }
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Logger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        //</editor-fold>
        */
    try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Logger().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel clockLabel;
    private javax.swing.JButton closePortButton;
    private javax.swing.JButton detectButton;
    private javax.swing.JFormattedTextField deviceIdTextField;
    private javax.swing.JButton eraseButton;
    private javax.swing.JButton getButton;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton openButton;
    private javax.swing.JComboBox portsComboBox;
    private javax.swing.JButton readButton;
    private javax.swing.JButton saveToButton;
    private javax.swing.JButton setButton;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables

    class MySaveFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File file) {
            // Allow only directories, or files with ".txt" extension
            return file.isDirectory() || file.getAbsolutePath().endsWith(".csv");
        }

        @Override
        public String getDescription() {
            // This description will be displayed in the dialog,
            // hard-coded = ugly, should be done via I18N
            return "CSV files (*.csv)";
        }
    }

    class MyOpenFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File file) {
            // Allow only directories, or files with ".txt" extension
            return file.getAbsolutePath().endsWith(".bin");
        }

        @Override
        public String getDescription() {
            // This description will be displayed in the dialog,
            // hard-coded = ugly, should be done via I18N
            return "Binary file (*.bin)";
        }
    }

    private final class PacketListener implements SerialPortPacketListener {

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public int getPacketSize() {
            return FWLOAD_PKT_SIZE;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] tempBuffer = event.getReceivedData();
            processRawPacket(tempBuffer);

            //   System.out.print((char)newData[i]);
            //System.out.println("\n");
        }
    }

    private void processRawPacket(byte[] data) {
        int packetType = -1;
        long packetReceived = -1;

        System.arraycopy(data, 0, procBuffer, 0, FWLOAD_PKT_SIZE);
        newPacket = false;
        packetType = procBuffer[2] & 0xFF;
        if (!(chkCsumFWLOAD(procBuffer) == FWLOAD_OK)) {
            // Invalid checksum, no process this packet
            jTextArea1.append(String.format("Invalid checksum on packet type: %x\n", packetType));
            return;
        }
        switch (packetType) {
            case FWLOAD_PACKET_DOWNLOAD_LOG_INFO_RESP:

                totalLogs = procBuffer[FWLOAD_PKT_TYPE_IDX + 5] & 0xFF;
                totalLogs |= (procBuffer[FWLOAD_PKT_TYPE_IDX + 4] << 8) & 0x0000FFFF;
                totalLogs |= (procBuffer[FWLOAD_PKT_TYPE_IDX + 3] << 16) & 0x00FFFFFF;
                totalLogs |= (procBuffer[FWLOAD_PKT_TYPE_IDX + 2] << 24) & 0xFFFFFFFF;
                jTextArea1.append(String.format("Total logs in device: %d\n", totalLogs));
                logReq = 0;
                // Send ack from current RESP and request FIRST log packet
                sendLogReq(outBuffer, logReq);
                break;

            case FWLOAD_PACKET_DOWNLOAD_LOG:
                // Convert and process log data packet
                packetReceived = processLogPacket(procBuffer);
                if (packetReceived == logReq) {
                    logReq++;
                } else {
                    logPacketErrorCounter++;
                }
                if (logReq <= totalLogs) {
                    //sendLogReq(outBuffer, logReq);
                    sendLogReq(outBuffer, logReq);
                } else {
                    // All logs have been read
                    totalLogs = -1;

                }
                break;
                
            case FWLOAD_PACKET_TYPE_ACK:
                jTextArea1.append(String.format("AKC received\n"));
                break;
                
            case FWLOAD_PACKET_READ_DEVID:
                readDeviceIdPacket(procBuffer);
        }
    }

    //--------------------------------------------------------------------------
    private void sendLogReq(byte[] buffer, long logNum) {

        clrPktFWLOAD(buffer);
        buffer[FWLOAD_PKT_TYPE_IDX] = (byte) FWLOAD_PACKET_DOWNLOAD_LOG_ACK;
        // Add requested Log packet number
        buffer[FWLOAD_PKT_TYPE_IDX + 1] = (byte) ((logNum >> 24) & 0xFF);
        buffer[FWLOAD_PKT_TYPE_IDX + 2] = (byte) ((logNum >> 16) & 0xFF);
        buffer[FWLOAD_PKT_TYPE_IDX + 3] = (byte) ((logNum >> 8) & 0xFF);
        buffer[FWLOAD_PKT_TYPE_IDX + 4] = (byte) (logNum & 0xFF);
        addCsumFWLOAD(buffer);
        if (openedPort >= 0) {
            if (ports[openedPort].isOpen()) {
                ports[openedPort].writeBytes(buffer, FWLOAD_PKT_SIZE);
            } else {
                jTextArea1.append(String.format("Not opened Port"));
            }
        } else {
            jTextArea1.append(String.format("Not valid Port"));
        }
    }

    //--------------------------------------------------------------------------
    private void startLogging(byte[] buffer) {
        clrPktFWLOAD(buffer);
        buffer[FWLOAD_PKT_TYPE_IDX] = (byte) FWLOAD_PACKET_DOWNLOAD_LOG_INFO_REQ;
        addCsumFWLOAD(buffer);
        if (openedPort >= 0) {
            if (ports[openedPort].isOpen()) {
                ports[openedPort].writeBytes(buffer, FWLOAD_PKT_SIZE);
            } else {
                jTextArea1.append(String.format("Not opened Port"));
            }
        } else {
            jTextArea1.append(String.format("Not valid Port"));
        }
    }

    //--------------------------------------------------------------------------
    private long processLogPacket(byte[] buffer) {

        // TODO: Add wrong packet arrived error handling
        byte[] rtcBytes = new byte[RTC_STRING_LENGTH];
        int batteryLevel = 0;
        int[] sensors = new int[9];
        int pressure = 0;
        int temperature = 0;
        double temperatureDouble = 0;
        int j, k;
        long packetLogNumber = -1;
        long errorCode = 0;

        System.arraycopy(buffer, FWLOAD_PKT_LOG_PAYLOAD_IDX, rtcBytes, 0, RTC_STRING_LENGTH);
        Charset charset = Charset.forName("US-ASCII");
        //try{
        String rtcString = new String(rtcBytes, charset);
        String processedSensors = null;
        //}catch(UnsupportedEncodingException error){
        //   jTextArea1.append(String.format("%s\n", error));
        //}

        packetLogNumber = buffer[FWLOAD_PKT_LOG_PKT_NUM_IDX + 3] & 0xFF;
        packetLogNumber |= (buffer[FWLOAD_PKT_LOG_PKT_NUM_IDX + 2] << 8) & 0xFFFF;
        packetLogNumber |= (buffer[FWLOAD_PKT_LOG_PKT_NUM_IDX + 1] << 16) & 0xFFFFFF;
        packetLogNumber |= (buffer[FWLOAD_PKT_LOG_PKT_NUM_IDX] << 24) & 0xFFFFFFFF;

        batteryLevel = buffer[FWLOAD_PKT_LOG_BATTERY_IDX] & 0xFF;

        errorCode = buffer[FWLOAD_PKT_LOG_ERROR_IDX + 3] & 0xFF;
        errorCode |= (buffer[FWLOAD_PKT_LOG_ERROR_IDX + 2] << 8) & 0xFFFF;
        errorCode |= (buffer[FWLOAD_PKT_LOG_ERROR_IDX + 1] << 16) & 0xFFFFFF;
        errorCode |= (buffer[FWLOAD_PKT_LOG_ERROR_IDX] << 24) & 0xFFFFFFFF;

        for (k = FWLOAD_PKT_LOG_SENSORS_IDX, j = 0; k < FWLOAD_PKT_LOG_PRESSURE_IDX; k = k + 2, j++) {
            sensors[j] = (buffer[k + 1]) & 0xFF;
            sensors[j] |= (buffer[k] << 8) & 0xFFFF;
        }

        pressure = buffer[FWLOAD_PKT_LOG_PRESSURE_IDX + 1] & 0xFF;
        pressure |= (buffer[FWLOAD_PKT_LOG_PRESSURE_IDX] << 8) & 0xFFFF;

        temperature = buffer[FWLOAD_PKT_LOG_TEMPERATURE_IDX + 1] & 0xFF;
        temperature |= (buffer[FWLOAD_PKT_LOG_TEMPERATURE_IDX] << 8) & 0xFFFF;

        temperatureDouble = temperature * 0.1;
        // Write to CSV file
        if (packetLogNumber == 0) {
            // Create a temporary file

            try {
                tempPath = Paths.get("tempLogRecord.csv");
                outputStream = new PrintWriter(Files.newBufferedWriter(tempPath, charset));

                tempPath2 = Paths.get("tempLogRecord2.csv");
                outputStream2 = new PrintWriter(Files.newBufferedWriter(tempPath2, charset));
                //BufferedWriter writer = Files.newBufferedWriter(tempPath, charset);
                //outputStream = new PrintWriter(new FileWriter("tempLogRecord.txt"));

                // Write headers
                outputStream.format("Date,Battery Level,Error Code,"
                        + "CO,CO2,CL2,NO2,SO2,HCN,H2S,NH3,HCL,Pressure,Temperature\n");
                /*
                outputStream2.format("Date,Battery Level,Error Code,"
                        + "CO,CO2,CL2,NO2,SO2,HCN,H2S,NH3,HCL,Pressure,Temperature\n");
                */

            } catch (IOException error) {
                jTextArea1.append(String.format("%s\n", error));
            }
        }

        outputStream.format(recordFormat, rtcString, batteryLevel, errorCode, sensors[0],
                sensors[1], sensors[2], sensors[3], sensors[4], sensors[5], sensors[6],
                sensors[7], sensors[8], pressure, temperatureDouble);
        
    // Process sensor readings
    processSensorsReadings(rtcString, batteryLevel, errorCode, sensors,
            pressure, temperatureDouble);


        if (packetLogNumber == totalLogs) {
            // Close File and mark that a new log is available for save to disk
            logAvailable = true;
            outputStream.close();
            outputStream2.close();
            saveToButton.setEnabled(true);
        }
        return packetLogNumber;
    }
    //--------------------------------------------------------------------------
    private void processSensorsReadings(String rtc, int batteryLevel,
            long errorCode, int[] rawSensors, int pressure, double temperatureDouble ){
        
        String processedSensors = null;
        double[] measuredNa = new double[9];
        double[] measuredPpm = new double[9];
        double[] voltsZeroGas = new double[9];
        double[] countsZeroGas = new double[9];
        double[] voltsMaxExposure = new double[9];
        double[] countsMaxExposure = new double[9];
        double[] deltaCounts = new double[9];
        double[] countsPerPpm = new double[9];
        
        // Sample data header
        outputStream2.format("Date, Pressure (mBar), Temperature\n");
        outputStream2.format(sampleHeaderFormat,rtc, pressure, temperatureDouble);
        outputStream2.format("Gas, Measured Counts, Measured nA, Measured ppm, Volts at Zero Gas,"
                + "Count at Zero Gas, Volts at Max. Exposure, Counts at Max. Exposure,"
                + "Delta Counts, Counts/ppm\n");
        
        // Process sensors
        for(int k = 0; k < NUMBER_OF_GAS_SENSORS; k++){
            voltsZeroGas[k] = VOLTAGE*sensorsZero[k];
            countsZeroGas[k] = COUNTS*sensorsZero[k];
            
            voltsMaxExposure[k] = sensorsZero[k]*VOLTAGE + 
                    sensorsMaxPpm[k]*sensorsNaPpm[k]*0.000000001*sensorsGain[k];
            countsMaxExposure[k] = voltsMaxExposure[k]/(COUNTS*VOLTAGE);
            
            deltaCounts[k] = countsMaxExposure[k] - countsZeroGas[k];
            countsPerPpm[k] = deltaCounts[k]/sensorsMaxPpm[k];
            
            measuredNa[k] = Math.abs(rawSensors[k] - countsZeroGas[k])*VOLTAGE/COUNTS/sensorsGain[k]*1000000000;
            measuredPpm[k] = measuredNa[k]/sensorsNaPpm[k];
            
            // Write to file
            outputStream2.format(gasOutputFormat,
                    sensorsName[k], rawSensors[k], measuredNa[k], measuredPpm[k],
                    voltsZeroGas[k], countsZeroGas[k], voltsMaxExposure[k], countsMaxExposure[k],
                    deltaCounts[k], countsPerPpm[k]);
        }
    }
    
    //--------------------------------------------------------------------------
    private void readDeviceIdPacket(byte[] buffer){
        
        currentDeviceId = buffer[(FWLOAD_PKT_TYPE_IDX) + 3]             & 0xFF;
        currentDeviceId |= (buffer[(FWLOAD_PKT_TYPE_IDX) + 2] << 8)     & 0xFFFF;
        currentDeviceId |= (buffer[(FWLOAD_PKT_TYPE_IDX) + 5] << 16)    & 0xFFFFFF;
        currentDeviceId |= (buffer[(FWLOAD_PKT_TYPE_IDX) + 4] << 24)    & 0xFFFFFF;
        
        // Display in text container
        deviceIdTextField.setValue(currentDeviceId);
    }
    private void sendSetIdPacket(byte[] buffer, long Id){
        clrPktFWLOAD(buffer);
        buffer[FWLOAD_PKT_TYPE_IDX] = (byte) FWLOAD_PACKET_SET_DEVID;    
        buffer[FWLOAD_PKT_TYPE_IDX + 1] = 4;
        
        buffer[FWLOAD_PKT_TYPE_IDX + 3] = (byte)(Id & 0xFF);
        buffer[(FWLOAD_PKT_TYPE_IDX) + 2] = (byte) ((Id >> 8) & 0xFF);
        buffer[(FWLOAD_PKT_TYPE_IDX) + 5] = (byte) ((Id >> 16) & 0xFF);
        buffer[(FWLOAD_PKT_TYPE_IDX) + 4] = (byte) ((Id >> 24) & 0xFF);
        addCsumFWLOAD(buffer);
        // Check port and send packet
        if (openedPort >= 0) {
            if (ports[openedPort].isOpen()) {
                ports[openedPort].writeBytes(buffer, FWLOAD_PKT_SIZE);
            } else {
                jTextArea1.append(String.format("Not opened Port\n"));
            }
        } else {
            jTextArea1.append(String.format("Not valid Port\n"));
        }        
    }
    //--------------------------------------------------------------------------
    private void sendGetIdResquestPacket(byte[] buffer){
        clrPktFWLOAD(buffer);
        buffer[FWLOAD_PKT_TYPE_IDX] = (byte) FWLOAD_PACKET_GET_DEVID;    
        buffer[FWLOAD_PKT_TYPE_IDX + 1] = 0;
        addCsumFWLOAD(buffer);
        // Check port and send packet
        if (openedPort >= 0) {
            if (ports[openedPort].isOpen()) {
                ports[openedPort].writeBytes(buffer, FWLOAD_PKT_SIZE);
            } else {
                jTextArea1.append(String.format("Not opened Port\n"));
            }
        } else {
            jTextArea1.append(String.format("Not valid Port\n"));
        }         
    }
    //--------------------------------------------------------------------------
    private void sendEraseLogRequestPacket(byte[] buffer){
        clrPktFWLOAD(buffer);
        buffer[FWLOAD_PKT_TYPE_IDX] = (byte) FWLOAD_PACKET_ERASE_LOGS;
        buffer[FWLOAD_PKT_TYPE_IDX + 1] = 0;
        addCsumFWLOAD(buffer);
        // Check port and send packet
        if (openedPort >= 0) {
            if (ports[openedPort].isOpen()) {
                ports[openedPort].writeBytes(buffer, FWLOAD_PKT_SIZE);
            } else {
                jTextArea1.append(String.format("Not opened Port\n"));
            }
        } else {
            jTextArea1.append(String.format("Not valid Port\n"));
        }         
    }
    private void sendSetRtcPacket(byte[] buffer){
        
        String dateString = null;
        clrPktFWLOAD(buffer);
        buffer[FWLOAD_PKT_TYPE_IDX] = (byte) FWLOAD_PACKET_SET_RTC;
        buffer[FWLOAD_PKT_TYPE_IDX + 1] = 4;
        
        // Construct RTC message
        Date currentDate = new Date();
        dateString = myDateFormat.format(currentDate);
        Scanner s = new Scanner(dateString);
        buffer[FWLOAD_PKT_TYPE_IDX + 2] = (byte) s.nextInt();
        buffer[FWLOAD_PKT_TYPE_IDX + 3] = (byte) s.nextInt();
        buffer[FWLOAD_PKT_TYPE_IDX + 4] = (byte) s.nextInt();
        buffer[FWLOAD_PKT_TYPE_IDX + 5] = (byte) s.nextInt();
        buffer[FWLOAD_PKT_TYPE_IDX + 6] = (byte) s.nextInt();
        buffer[FWLOAD_PKT_TYPE_IDX + 7] = (byte) s.nextInt();
        buffer[FWLOAD_PKT_TYPE_IDX + 8] = (byte) s.nextInt();
        
        addCsumFWLOAD(buffer);
        
        // Check port and send packet
        if (openedPort >= 0) {
            if (ports[openedPort].isOpen()) {
                ports[openedPort].writeBytes(buffer, FWLOAD_PKT_SIZE);
            } else {
                jTextArea1.append(String.format("Not opened Port\n"));
            }
        } else {
            jTextArea1.append(String.format("Not valid Port\n"));
        }         
    }
    //--------------------------------------------------------------------------
    private void updateDateLabel(){

        clockLabel.setText(new Date().toString());
        /*
        String dateString = null;
        Date currentDate = new Date();
        dateString = myDateFormat.format(currentDate);
        Scanner s = new Scanner(dateString);
        
        byte ss = (byte) s.nextInt();
        byte mm = (byte) s.nextInt();
        byte HH = (byte) s.nextInt();
        byte u = (byte) s.nextInt();
        byte d = (byte) s.nextInt();
        byte MM = (byte) s.nextInt();
        byte YY = (byte) s.nextInt();
        
        clockLabel.setText(String.format("%d %d %d %d %d %d %d",ss,mm,HH,u,d,MM,YY));
        */
    }
    //--------------------------------------------------------------------------
    void clrPktFWLOAD(byte[] pkt_data) {
        int k;
        for (k = 0; k < (FWLOAD_PKT_SIZE); k++) {
            pkt_data[k] = 0x00;
        }
        // add packet sign - preambule
        pkt_data[0] = (byte) FWLOAD_PKT_SIGN_B0;
        pkt_data[1] = (byte) FWLOAD_PKT_SIGN_B1;
    }

    //--------------------------------------------------------------------------
    private int chkCsumFWLOAD(byte[] buff) {/// validate packet checksum 
        int k;
        byte[] csum = {0x00, 0x00};
        byte flag = 0x00;

        for (k = 0; k < ((FWLOAD_PKT_SIZE) - 2); k++) {
            csum[flag] ^= buff[k];
            flag ^= 0x01;
        }

        if ((csum[0] == buff[(FWLOAD_PKT_SIZE) - 2]) && (csum[1] == buff[(FWLOAD_PKT_SIZE) - 1])) {
            return (FWLOAD_OK);
        }

        return (FWLOAD_ERROR_CSUM);
    }

    //--------------------------------------------------------------------------    
    private void addCsumFWLOAD(byte[] pkt_data) {
        int k;
        pkt_data[(FWLOAD_PKT_SIZE) - 2] = 0x00;
        pkt_data[(FWLOAD_PKT_SIZE) - 1] = 0x00;

        for (k = 0; k < ((FWLOAD_PKT_SIZE) - 2); k += 2) {
            pkt_data[(FWLOAD_PKT_SIZE) - 2] ^= pkt_data[k];
            pkt_data[(FWLOAD_PKT_SIZE) - 1] ^= pkt_data[k + 1];
        }
    }
}
