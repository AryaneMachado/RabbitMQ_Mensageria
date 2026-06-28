package br.com.mensageria;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Produtor {

    private static final String FILA = "pedidos";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection conn = factory.newConnection();
             Channel channel = conn.createChannel()) {

            // durable=true: fila sobrevive a reinicializacoes do broker
            channel.queueDeclare(FILA, true, false, false, null);

            String[] pedidos = {
                "{ \"pedido_id\": \"001\", \"produto\": \"Notebook\",  \"valor\": 4500.00 }",
                "{ \"pedido_id\": \"002\", \"produto\": \"Mouse\",     \"valor\":   89.90 }",
                "{ \"pedido_id\": \"003\", \"produto\": \"Monitor\",   \"valor\": 1299.00 }",
                "{ \"pedido_id\": \"004\", \"produto\": \"Teclado\",   \"valor\":  250.00 }",
                "{ \"pedido_id\": \"005\", \"produto\": \"Headset\",   \"valor\":  350.00 }"
            };

            for (String pedido : pedidos) {
                // PERSISTENT garante durabilidade em caso de reinicio do broker
                channel.basicPublish("", FILA, MessageProperties.PERSISTENT_TEXT_PLAIN, pedido.getBytes("UTF-8"));
                System.out.println("[ENVIADO] " + pedido);
            }

            System.out.println("\n[INFO] Todos os pedidos foram publicados na fila '" + FILA + "'.");
            System.out.println("[INFO] Acesse http://localhost:15672 (guest/guest) para visualizar.");
        }
    }
}
