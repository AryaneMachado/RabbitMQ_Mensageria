package br.com.mensageria;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProdutorGUI extends JFrame {

    private static final String FILA = "pedidos";
    private static int contador = 0;

    private final JTextField campoProduto = new JTextField();
    private final JTextField campoValor   = new JTextField();
    private final JTextArea  log          = new JTextArea();
    private final JLabel     status       = new JLabel("Conectando ao RabbitMQ...");

    private Channel    channel;
    private Connection connection;

    public ProdutorGUI() {
        super("Produtor - RabbitMQ");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 480);
        setLocationRelativeTo(null);
        montarInterface();
        conectar();
    }

    private void montarInterface() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Produtor de Pedidos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(new Color(45, 106, 45));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Produto:"));
        form.add(campoProduto);
        form.add(new JLabel("Valor (R$):"));
        form.add(campoValor);

        JButton enviar = new JButton("Enviar Pedido");
        enviar.setBackground(new Color(45, 106, 45));
        enviar.setForeground(Color.WHITE);
        enviar.setFocusPainted(false);
        enviar.addActionListener(e -> enviarPedido());

        JPanel topo = new JPanel(new BorderLayout(0, 10));
        topo.add(titulo, BorderLayout.NORTH);
        topo.add(form,   BorderLayout.CENTER);
        topo.add(enviar, BorderLayout.SOUTH);

        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createTitledBorder("Mensagens publicadas"));

        status.setForeground(Color.GRAY);

        root.add(topo,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void conectar() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost("localhost");
                    factory.setPort(5672);
                    factory.setUsername("guest");
                    factory.setPassword("guest");

                    connection = factory.newConnection();
                    channel    = connection.createChannel();
                    channel.queueDeclare(FILA, true, false, false, null);
                } catch (Exception ex) {
                    setStatus("Falha ao conectar: " + ex.getMessage(), Color.RED);
                    return null;
                }
                setStatus("Conectado à fila 'pedidos' (localhost:5672)", new Color(45, 106, 45));
                return null;
            }
        }.execute();
    }

    private void setStatus(String texto, Color cor) {
        SwingUtilities.invokeLater(() -> {
            status.setText(texto);
            status.setForeground(cor);
        });
    }

    private void enviarPedido() {
        if (channel == null) {
            JOptionPane.showMessageDialog(this, "Ainda conectando ao RabbitMQ, aguarde...");
            return;
        }
        String produto    = campoProduto.getText().trim();
        String valorTexto = campoValor.getText().trim();

        if (produto.isEmpty() || valorTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha produto e valor.");
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorTexto.replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido.");
            return;
        }

        contador++;
        String id         = String.format("%03d", contador);
        String pedidoJson = String.format(
                "{ \"pedido_id\": \"%s\", \"produto\": \"%s\", \"valor\": %.2f }",
                id, produto, valor);

        try {
            channel.basicPublish("", FILA, MessageProperties.PERSISTENT_TEXT_PLAIN,
                    pedidoJson.getBytes("UTF-8"));

            String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
            log.append("[" + hora + "] ENVIADO -> " + pedidoJson + "\n");
            log.setCaretPosition(log.getDocument().getLength());

            campoProduto.setText("");
            campoValor.setText("");
            campoProduto.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao publicar: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProdutorGUI().setVisible(true));
    }
}
