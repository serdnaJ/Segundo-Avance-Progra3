import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class SistemaATM {

    // 1. ATRIBUTOS: ESTRUCTURAS DE DATOS
    // cola para procesar multas y pila para historial de pagos
    private Queue<String> colaMultas;
    private Stack<String> pilaPagos;

    // base de datos simulada usando 4 listas paralelas
    private ArrayList<String> basePlacas;
    private ArrayList<String> baseDeudas;
    private ArrayList<Integer> baseSaldos;
    private ArrayList<Integer> baseDias;

    // 2. CONSTRUCTOR
    public SistemaATM() {
        colaMultas = new LinkedList<>();
        pilaPagos = new Stack<>();
        basePlacas = new ArrayList<>();
        baseDeudas = new ArrayList<>();
        baseSaldos = new ArrayList<>();
        baseDias = new ArrayList<>();

    }

    // 3. BÚSQUEDA SECUENCIAL
    // busca una placa recorriendo las listas y devuelve el estado de su deuda
    public String buscarDeudaPorPlaca(String placaBuscada) {
        for (int i = 0; i < basePlacas.size(); i++) {
            if (basePlacas.get(i).equals(placaBuscada)) {
                int saldo = baseSaldos.get(i);

                // si el saldo es cero, ya no debe nada
                if (saldo <= 0) {
                    return "El vehículo está libre de deudas (Saldo $0).";
                }

                // arma el texto combinando los datos de las 4 listas
                return "Saldo a pagar: $" + saldo +
                        "\nPlazo máximo: " + baseDias.get(i) + " días" +
                        "\nDetalle: " + baseDeudas.get(i);
            }
        }
        return "El vehículo no registra deudas pendientes en el sistema.";
    }

    // 4. PROCESAMIENTO DE PAGOS
    // resta el monto pagado del saldo actual y guarda la transacción en la pila
    public String procesarPagoYApilar(String placa, int montoPagado, String metodo) {
        boolean encontrada = false;
        int saldoRestante = 0;

        for (int i = 0; i < basePlacas.size(); i++) {
            if (basePlacas.get(i).equals(placa)) {
                encontrada = true;
                int saldoActual = baseSaldos.get(i);
                saldoRestante = saldoActual - montoPagado;

                // validamos que la deuda no quede en números negativos
                if (saldoRestante < 0) saldoRestante = 0;

                baseSaldos.set(i, saldoRestante); // actualiza el saldo en la base de datos
                break;
            }
        }

        // genera el comprobante que se guardará en el historial
        String transaccion;
        if (encontrada) {
            transaccion = "Pago: $" + montoPagado + " | Placa: " + placa + " | Deuda Restante: $" + saldoRestante;
        } else {
            transaccion = "Pago: $" + montoPagado + " | Placa: " + placa + " (Placa libre de deudas)";
        }

        pilaPagos.push(transaccion); // apila la transacción
        return transaccion;
    }

    // 5. GESTIÓN DE INFRACCIONES (COLA)

    // añade la multa a la cola visual (bandeja de espera)
    public void agregarMulta(String multa) {
        colaMultas.offer(multa);
    }

    // guarda la multa definitivamente
    public void registrarEnBaseDeDatos(String placa, String detalleDeuda, int monto) {
        basePlacas.add(placa);
        baseDeudas.add(detalleDeuda);
        baseSaldos.add(monto);
        baseDias.add(30);
    }

    // desencola la multa más antigua simulando que se sube al sistema.
    public String procesarMulta() {
        return colaMultas.poll();
    }

    public Queue<String> getColaMultas() {
        return colaMultas;
    }

    // 6. HISTORIAL COMPLETO DE MULTAS
    // recopila todas las multas activas de una placa específica
    public String listarMultasPorPlaca(String placaBuscada) {
        StringBuilder historial = new StringBuilder();
        boolean encontrada = false;
        boolean tieneDeudasActivas = false;
        int contador = 1;

        for (int i = 0; i < basePlacas.size(); i++) {
            if (basePlacas.get(i).equals(placaBuscada)) {
                encontrada = true;

                // se lista solo si el saldo es mayor a cero
                if (baseSaldos.get(i) > 0) {
                    tieneDeudasActivas = true;
                    historial.append("Multa #").append(contador).append("\n");
                    historial.append("- Detalle: ").append(baseDeudas.get(i)).append("\n");
                    historial.append("- Saldo Pendiente: $").append(baseSaldos.get(i)).append("\n");
                    historial.append("- Plazo: ").append(baseDias.get(i)).append(" días\n");
                    historial.append("---------------------------\n");
                    contador++;
                }
            }
        }

        // devuelve el texto final dependiendo de lo que encontró
        if (!encontrada) {
            return "El vehículo con placa " + placaBuscada + " no registra historial en el sistema.";
        } else if (!tieneDeudasActivas) {
            return "El vehículo con placa " + placaBuscada + " ya pagó todas sus multas.";
        } else {
            return historial.toString();
        }
    }

    // 7. HISTORIAL DE PAGOS (PILA)

    // desapila el último pago registrado simulando una anulación
    public String anularUltimoPago() {
        if (!pilaPagos.isEmpty()) {
            return pilaPagos.pop();
        }
        return null;
    }

    public Stack<String> getPilaPagos() {
        return pilaPagos;
    }

    @Override
    public String toString() {
        return "SistemaATM\ncolaMultas=" + colaMultas + "\npilaPagos =" + pilaPagos;
    }
}