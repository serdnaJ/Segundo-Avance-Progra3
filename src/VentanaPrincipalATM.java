import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaPrincipalATM {

    // 1. VARIABLES DE LA INTERFAZ GRÁFICA
    private JPanel panelPrincipal;
    private JTabbedPane tabbedPane1;

    // módulo de infracciones (pestaña 1)
    private JPanel infracciones;
    private JTextField txtPlaca;
    private JTextArea txtDescripcion;
    private JButton btnEmitirMulta;
    private JTextArea txtAreaCola;
    private JSpinner spinner1;
    private JButton listarMultasButton;
    private JButton subirAlSistemaButton;

    // módulo de pagos (pestaña 2)
    private JPanel pagos;
    private JComboBox cbxMetodo;
    private JSpinner spinnerMonto;
    private JButton btnPagar;
    private JButton btnDeshacer;
    private JTextField textField1;
    private JButton buscarPlacaButton;
    private JTextArea txtAreaPila;

    // 2. INSTANCIA DEL SISTEMA
    private SistemaATM sistema = new SistemaATM();

    public VentanaPrincipalATM() {
        // botón para registrar una nueva multa
        btnEmitirMulta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String placa = txtPlaca.getText().trim().toUpperCase();
                String desc = txtDescripcion.getText().trim();
                int montoMulta = (Integer) spinner1.getValue();

                if (!placa.isEmpty() && !desc.isEmpty() && montoMulta > 0) {
                    // guarda visualmente en la bandeja
                    String multa = "PLACA: " + placa + " | $" + montoMulta + " | " + desc;
                    sistema.agregarMulta(multa);

                    // guarda internamente en la base de datos
                    sistema.registrarEnBaseDeDatos(placa, desc, montoMulta);

                    actualizarVistaCola();

                    // limpia los campos de texto
                    txtPlaca.setText("");
                    txtDescripcion.setText("");
                    spinner1.setValue(0);
                } else {
                    JOptionPane.showMessageDialog(panelPrincipal, "Llene todos los datos y asegúrese de que el monto sea mayor a 0.", "Error de validación", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // botón para ver todas las multas de un vehículo específico
        listarMultasButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String placa = txtPlaca.getText().trim().toUpperCase();

                if (!placa.isEmpty()) {
                    String historial = sistema.listarMultasPorPlaca(placa);
                    JOptionPane.showMessageDialog(panelPrincipal,
                            "Historial de Infracciones:\n\n" + historial,
                            "Multas de " + placa,
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panelPrincipal,
                            "Por favor, ingrese una placa en el campo superior para listar sus multas.",
                            "Falta Placa",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // botón para despachar la multa más antigua
        subirAlSistemaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String procesada = sistema.procesarMulta(); // saca el primer registro ingresado

                if (procesada != null) {
                    actualizarVistaCola();
                    JOptionPane.showMessageDialog(panelPrincipal, "Multa subida al sistema nacional:\n\n" + procesada, "Proceso Exitoso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panelPrincipal, "No hay multas pendientes para subir.", "Bandeja Vacía", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // botón para consultar cuánto debe una placa
        buscarPlacaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String placa = textField1.getText().trim().toUpperCase();

                if (!placa.isEmpty()) {
                    String resultado = sistema.buscarDeudaPorPlaca(placa);
                    JOptionPane.showMessageDialog(panelPrincipal, "Resultado de la búsqueda:\n\n" + resultado, "Consulta de Deudas", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panelPrincipal, "Ingrese una placa para buscar.", "Campo vacío", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // botón para registrar un pago y descontar la deuda
        btnPagar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String placa = textField1.getText().trim().toUpperCase();
                String metodo = cbxMetodo.getSelectedItem().toString();
                int monto = (Integer) spinnerMonto.getValue();

                if (monto <= 0) {
                    JOptionPane.showMessageDialog(panelPrincipal, "El monto debe ser mayor a $0.", "Monto Inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (placa.isEmpty()) {
                    JOptionPane.showMessageDialog(panelPrincipal, "Ingrese la placa a la que desea abonar el pago.", "Falta Placa", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // procesa el pago y lo guarda en el historial
                String resumenPago = sistema.procesarPagoYApilar(placa, monto, metodo);

                actualizarVistaPila();
                JOptionPane.showMessageDialog(panelPrincipal, "Transacción Exitosa:\n\n" + resumenPago, "Pago Registrado", JOptionPane.INFORMATION_MESSAGE);

                // limpia los campos
                textField1.setText("");
                spinnerMonto.setValue(0);
            }
        });

        // botón para deshacer la última transacción registrada
        btnDeshacer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String anulado = sistema.anularUltimoPago(); // revierte la última acción ingresada

                if (anulado != null) {
                    actualizarVistaPila();
                    JOptionPane.showMessageDialog(panelPrincipal, "Transacción anulada con éxito:\n\n" + anulado, "Rollback Exitoso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panelPrincipal, "No hay pagos recientes para anular.", "Historial Vacío", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }


    // actualiza el cuadro de texto de multas pendientes
    private void actualizarVistaCola() {
        StringBuilder textoPantalla = new StringBuilder();
        for (String item : sistema.getColaMultas()) {
            textoPantalla.append(item).append("\n");
        }
        txtAreaCola.setText(textoPantalla.toString());
    }

    // actualiza el historial de pagos de forma invertida para ver el más reciente arriba
    private void actualizarVistaPila() {
        StringBuilder textoPantalla = new StringBuilder();
        for (int i = sistema.getPilaPagos().size() - 1; i >= 0; i--) {
            textoPantalla.append(sistema.getPilaPagos().get(i)).append("\n");
        }
        txtAreaPila.setText(textoPantalla.toString());
    }

    // main para arrancar
    public static void main(String[] args) {
        JFrame frame = new JFrame("Sistema ATM - Gestión de Tráfico");
        frame.setContentPane(new VentanaPrincipalATM().panelPrincipal);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}