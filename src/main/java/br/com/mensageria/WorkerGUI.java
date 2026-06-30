package br.com.mensageria;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkerGUI extends JFrame {

    private static final String FILA = "pedidos";

    private final String       workerId;
    private final JLabel       status        = new JLabel("Conectando...");
    private final JLabel       contadorLabel = new JLabel("Pedidos concluídos: 0");
    private final JProgressBar progresso     = new JProgressBar();
    private final JTextArea    log           = new JTextArea();

    private int concluidos = 0;

    public WorkerGUI(String workerId) {
        super("Worker-" + workerId + " - RabbitMQ");
        this.workerId = workerId;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 480);
        montarInterface();
        conectarEConsumir();
    }

    private void montarInterface() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Worker " + workerId);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(new Color(45, 106, 45));

        status.setFont(new Font("SansSerif", Font.PLAIN, 13));
        status.setForeground(Color.GRAY);

        progresso.setStringPainted(true);
        progresso.setString("Aguardando pedido");

        contadorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel topo = new JPanel(new GridLayout(4, 1, 4, 4));
        topo.add(titulo);
        topo.add(status);
        topo.add(progresso);
        topo.add(contadorLabel);

        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createTitledBorder("Atividade"));

        root.add(topo,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void conectarEConsumir() {
        new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                factory.setPort(5672);
                factory.setUsername("guest");
                factory.setPassword("guest");

                Connection conn = factory.newConnection();
                Channel channel = conn.createChannel();
                channel.queueDeclare(FILA, true, false, false, null);
                channel.basicQos(1);

                setStatus("Aguardando pedidos na fila 'pedidos'", new Color(45, 106, 45));

                DeliverCallback callback = (tag, delivery) -> {
                    String pedido = new String(delivery.getBody(), "UTF-8");
                    String hora   = new SimpleDateFormat("HH:mm:ss").format(new Date());

                    setStatus("Processando pedido...", new Color(230, 81, 0));
                    appendLog("[" + hora + "] PROCESSANDO -> " + pedido);
                    SwingUtilities.invokeLater(() -> {
                        progresso.setIndeterminate(true);
                        progresso.setString("Processando (2s)...");
                    });

                    try {
                        Thread.sleep(2000);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                        concluidos++;
                        String horaFim = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        appendLog("[" + horaFim + "] CONCLUÍDO   -> " + pedido);
                        setStatus("Aguardando pedidos na fila 'pedidos'", new Color(45, 106, 45));
                        SwingUtilities.invokeLater(() -> {
                            progresso.setIndeterminate(false);
                            progresso.setValue(100);
                            progresso.setString("Aguardando pedido");
                            contadorLabel.setText("Pedidos concluídos: " + concluidos);
                        });
                    } catch (Exception ex) {
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                        appendLog("[ERRO] " + ex.getMessage());
                    }
                };

                channel.basicConsume(FILA, false, callback, consumerTag -> {});

            } catch (Exception ex) {
                setStatus("Falha ao conectar: " + ex.getMessage(), Color.RED);
            }
        }, "worker-consumer-thread").start();
    }

    private void setStatus(String texto, Color cor) {
        SwingUtilities.invokeLater(() -> {
            status.setText(texto);
            status.setForeground(cor);
        });
    }

    private void appendLog(String linha) {
        SwingUtilities.invokeLater(() -> {
            log.append(linha + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        String id     = System.getProperty("worker.id", "1");
        int    offset = "2".equals(id) ? 520 : 0;
        SwingUtilities.invokeLater(() -> {
            WorkerGUI gui = new WorkerGUI(id);
            gui.setLocation(100 + offset, 100);
            gui.setVisible(true);
        });
    }
}
