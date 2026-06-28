package br.com.mensageria;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Worker {

    private static final String FILA = "pedidos";

    public static void main(String[] args) throws Exception {
        String workerId = System.getProperty("worker.id", "1");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        channel.queueDeclare(FILA, true, false, false, null);

        // basicQos(1): broker so envia proxima mensagem apos ACK da atual (fair dispatch)
        channel.basicQos(1);

        System.out.println("[Worker-" + workerId + "] Aguardando pedidos. CTRL+C para sair.\n");

        DeliverCallback callback = (tag, delivery) -> {
            String pedido = new String(delivery.getBody(), "UTF-8");
            System.out.println("[Worker-" + workerId + "] PROCESSANDO: " + pedido);

            try {
                Thread.sleep(2000);
                System.out.println("[Worker-" + workerId + "] CONCLUIDO:   " + pedido);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                System.err.println("[Worker-" + workerId + "] FALHA: " + e.getMessage());
                // requeue=true: devolve a mensagem a fila para nova tentativa
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };

        channel.basicConsume(FILA, false, callback, consumerTag -> {});
    }
}
