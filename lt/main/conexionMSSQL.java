import java.util.Vector;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class conexionMSSQL {
    String servidor, usuario, contraseña, nombreBD;
    Connection conexion;

    public conexionMSSQL(String usuario, String contraseña, String nombreBD) {
        this.servidor = "jdbc:sqlserver://localhost:1433;";
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.nombreBD = nombreBD;
    }

    public conexionMSSQL(String servidor, String usuario, String contraseña, String nombreBD) {
        this.servidor = servidor;
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.nombreBD = nombreBD;
    }

    public String getServidor() {
        return this.servidor;
    }

    public String getUsuario() {
        return this.usuario;
    }

    public String getContraseña() {
        return this.contraseña;
    }

    public String getNombreBD() {
        return this.nombreBD;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public void setNombreBD(String nombreBD) {
        this.nombreBD = nombreBD;
    }

    public Connection conectar() {
        String connectionUrl = this.servidor + "databaseName=" + this.nombreBD + ";user=" + this.usuario + ";password="
                + this.contraseña + ";loginTimeout=30; encrypt=false;";
        try {
            Connection con = DriverManager.getConnection(connectionUrl);
            System.out.println("Conexión exitosa");
            this.conexion = con;
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void desconectar() {
        try {
            conexion.close();
            System.out.println("Conexión cerrada");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Vector<String> mostrarTablas() {
        Vector<String> tablas = new Vector<>();
        try {
            DatabaseMetaData meta = conexion.getMetaData();
            ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });
            while (res.next()) {
                String tabla = res.getString("TABLE_NAME");
                if (!tabla.equals("trace_xe_action_map") && !tabla.equals("trace_xe_event_map")) {
                    tablas.add(tabla);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tablas;
    }

    public void insertarDatos() {
        JFrame frame = new JFrame("Insertar datos");

        JLabel insertarDatos = new JLabel("Insertar datos");
        JLabel selectTable = new JLabel("Seleccionar tabla: ");
        JLabel formato = new JLabel("Formato: ");
        JLabel datos = new JLabel("Datos: ");

        JComboBox<String> tablas = new JComboBox<String>(mostrarTablas());

        JTextField formatoTxt = new JTextField();

        JTextArea datosTxt = new JTextArea();

        JButton copiarFormato = new JButton(new ImageIcon(getClass().getResource("/resource/copia.png")));
        JButton insertar = new JButton("Insertar");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Label
        insertarDatos.setFont(new Font("Serif", Font.PLAIN, 24));
        insertarDatos.setBorder(new MatteBorder(0, 0, 2, 0, new Color(76, 76, 76, 100)));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 40, 0);
        panel.add(insertarDatos, gbc);

        gbc.insets = new Insets(0, 10, 20, 10);
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(selectTable, gbc);

        gbc.gridy = 2;
        panel.add(formato, gbc);

        gbc.gridy = 3;
        panel.add(datos, gbc);

        // TextArea
        datosTxt.setFont(new Font("Consolas", Font.PLAIN, 12));
        datosTxt.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(datosTxt);
        scroll.setPreferredSize(new Dimension(300, 100));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        panel.add(scroll, gbc);

        // ComboBox
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(tablas, gbc);

        // TextField
        formatoTxt.setPreferredSize(new Dimension(200, 30));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(formatoTxt, gbc);

        // Button
        gbc.insets = new Insets(0, 0, 20, 10);
        gbc.gridx = 2;
        panel.add(copiarFormato, gbc);

        gbc.gridy = 5;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        panel.add(insertar, gbc);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        tablas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formatoTxt.setText(nombreColumnas(tablas.getSelectedItem().toString()));
            }
        });
        copiarFormato.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String texto = formatoTxt.getText();
                Clipboard portapapeles = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selecion = new StringSelection(texto);
                portapapeles.setContents(selecion, null);
            }
        });
        insertar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement stmt = conexion.createStatement();
                    String sql = "INSERT INTO " + tablas.getSelectedItem().toString() + " "
                            + nombreColumnas(tablas.getSelectedItem().toString()) + " VALUES " + datosTxt.getText();
                    stmt.executeUpdate(sql);
                    JOptionPane.showMessageDialog(null, "Datos insertados");
                    System.out.println("Datos insertados");
                } catch (SQLException ex) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String trazaPila = sw.toString();
                    JOptionPane.showMessageDialog(null, trazaPila);
                }
                datosTxt.setText("");
            }
        });
    }

    public String nombreColumnas(String tabla) {
        String nombres = "(";
        try {
            Statement stmt = conexion.createStatement();
            String sql = "SELECT * FROM " + tabla;
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                nombres += (i != columnCount) ? (rsmd.getColumnName(i) + ", ") : rsmd.getColumnName(i) + ")";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombres;
    }

    public Vector<String> nombreColumnasV(String tablas) {
        Vector<String> nombres = new Vector<String>();
        try {
            Statement stmt = conexion.createStatement();
            String sql = "SELECT * FROM " + tablas;
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                nombres.add(rsmd.getColumnName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombres;
    }

    public void mostrarTabla() {

        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        Vector<Object> fila = new Vector<Object>();

        JFrame frame = new JFrame("Mostrar tabla");

        JLabel mostrarTabla = new JLabel("Mostrar tabla");
        JLabel selectTable = new JLabel("Seleccionar tabla: ");

        JComboBox<String> tablas = new JComboBox<String>(mostrarTablas());

        JButton mostrar = new JButton("Mostrar");

        JTable tabla = new JTable();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Label
        mostrarTabla.setFont(new Font("Serif", Font.PLAIN, 24));
        mostrarTabla.setBorder(new MatteBorder(0, 0, 2, 0, new Color(76, 76, 76, 100)));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 40, 0);
        panel.add(mostrarTabla, gbc);

        gbc.insets = new Insets(0, 10, 20, 10);
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(selectTable, gbc);

        // ComboBox
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        panel.add(tablas, gbc);

        // Button
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(mostrar, gbc);

        // Table
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(tabla), gbc);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        mostrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    fila.clear();
                    data.clear();
                    Statement stmt = conexion.createStatement();
                    String sql = "SELECT * FROM " + tablas.getSelectedItem().toString();
                    ResultSet rs = stmt.executeQuery(sql);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            fila.add(rs.getString(i));
                        }
                        data.add(fila);
                    }
                    DefaultTableModel model = new DefaultTableModel(data,
                            nombreColumnasV(tablas.getSelectedItem().toString()));
                    tabla.setModel(model);
                } catch (SQLException ex) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String trazaPila = sw.toString();
                    JOptionPane.showMessageDialog(null, trazaPila);
                }
            }
        });
    }
    public static void main(String[] args) {
        conexionMSSQL con = new conexionMSSQL("sa", "miespositaT02", "CINECLUB");
        con.conectar();
        con.insertarDatos();
    }
}
