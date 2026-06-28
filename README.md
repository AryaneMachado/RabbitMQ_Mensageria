# RabbitMQ - Sistema de Mensageria Distribuída

Implementação prática do padrão Work Queue com RabbitMQ em Java, desenvolvida como parte do artigo
**Sistemas de Mensageria Distribuída** da disciplina de Sistemas Computacionais Distribuídos e
Computação em Nuvem do curso de Sistemas de Informação do IFSULDEMINAS - Campus Machado.

## Conceitos demonstrados

- Comunicação assíncrona entre produtor e consumidores
- Fila durável com persistência de mensagens em disco
- ACK manual: mensagem só é removida da fila após confirmação de processamento
- NACK com requeue: mensagem volta para a fila em caso de falha no processamento
- Fair dispatch com basicQos(1): balanceamento de carga entre múltiplos workers

## Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- Eclipse IDE (recomendado: Eclipse IDE for Enterprise Java Developers)
- RabbitMQ 3.13.7
- Erlang/OTP 26.2.5

## Instalação do ambiente

### 1. Erlang/OTP 26.2.5

Baixe o instalador para Windows de 64 bits:

    https://github.com/erlang/otp/releases/download/OTP-26.2.5/otp_win64_26.2.5.exe

Execute o instalador com as opções padrão. Após a instalação, configure a variável de ambiente:

    setx ERLANG_HOME "C:\Program Files\Erlang OTP" /M

### 2. RabbitMQ 3.13.7

O Erlang deve estar instalado antes do RabbitMQ. Baixe o instalador:

    https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.13.7/rabbitmq-server-3.13.7.exe

Execute o instalador com as opções padrão.

### 3. Ativar o plugin de gerenciamento

Abra o Prompt de Comando como Administrador e execute:

    "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.13.7\sbin\rabbitmq-plugins.bat" enable rabbitmq_management

## Iniciando o RabbitMQ

Abra o Prompt de Comando como Administrador e execute:

    "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.13.7\sbin\rabbitmq-server.bat"

Deixe esta janela aberta. O broker estará disponível quando aparecer a mensagem:

    completed with N plugins.

Acesse o painel de gerenciamento em:

    http://localhost:15672
    Login: guest
    Senha: guest

## Importando o projeto no Eclipse

1. Abra o Eclipse e acesse: File > Import > Maven > Existing Maven Projects
2. Em "Root Directory", selecione a pasta raiz deste projeto
3. Clique em Finish e aguarde o download das dependências Maven

## Executando

### Worker (consumidor)

Clique com o botão direito em `Worker.java` > Run As > Java Application.

O console exibirá:

    [Worker-1] Aguardando pedidos. CTRL+C para sair.

Para executar um segundo Worker simultaneamente, repita o passo acima. O Eclipse
abrirá uma segunda instância. Os dois workers receberão as mensagens alternadamente,
demonstrando o balanceamento de carga.

Para diferenciar as instâncias, configure a VM argument `-Dworker.id=2` em:
Run > Run Configurations > (selecione Worker) > Arguments > VM arguments

### Produtor

Com um ou mais workers rodando, clique com o botão direito em `Produtor.java` >
Run As > Java Application.

O produtor envia 5 pedidos para a fila e encerra imediatamente, demonstrando a
natureza assíncrona da comunicação. O console exibirá:

    [ENVIADO] { "pedido_id": "001", "produto": "Notebook",  "valor": 4500.00 }
    [ENVIADO] { "pedido_id": "002", "produto": "Mouse",     "valor":   89.90 }
    [ENVIADO] { "pedido_id": "003", "produto": "Monitor",   "valor": 1299.00 }
    [ENVIADO] { "pedido_id": "004", "produto": "Teclado",   "valor":  250.00 }
    [ENVIADO] { "pedido_id": "005", "produto": "Headset",   "valor":  350.00 }

    [INFO] Todos os pedidos foram publicados na fila 'pedidos'.

## Estrutura do projeto

    RabbitMQ-Mensageria/
    |-- pom.xml
    |-- src/
        |-- main/
            |-- java/
            |   |-- br/com/mensageria/
            |       |-- Produtor.java    (publica pedidos na fila)
            |       |-- Worker.java      (consome e processa pedidos)
            |-- resources/
                |-- logback.xml          (configuração de log)

## Dependências

| Dependência       | Versão | Finalidade                          |
|-------------------|--------|-------------------------------------|
| amqp-client       | 5.20.0 | Cliente oficial RabbitMQ para Java  |
| slf4j-api         | 2.0.12 | Interface de logging                |
| logback-classic   | 1.5.3  | Implementação de logging            |

## Monitorando pelo painel web

Durante a execução, acesse http://localhost:15672 para visualizar em tempo real:

- Queues: fila "pedidos" com quantidade de mensagens pendentes e taxa de entrega
- Connections: conexões ativas dos workers
- Consumers: número de consumidores inscritos na fila

## Referências

- RabbitMQ Documentation: https://www.rabbitmq.com/docs
- amqp-client JavaDoc: https://rabbitmq.github.io/rabbitmq-java-client/api/current/
