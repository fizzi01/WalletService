package it.unisalento.pasproject.walletservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ------  SECURITY  ------ //

    // Needed by authentication service
    @Value("${rabbitmq.queue.security.name}")
    private String securityResponseQueue;

    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;

    @Value("${rabbitmq.routing.security.key}")
    private String securityRequestRoutingKey;

    @Bean
    public Queue securityResponseQueue() {
        return new Queue(securityResponseQueue);
    }

    @Bean
    public TopicExchange securityExchange() {
        return new TopicExchange(securityExchange);
    }

    @Bean
    public Binding securityBinding() {
        return BindingBuilder
                .bind(securityResponseQueue())
                .to(securityExchange())
                .with(securityRequestRoutingKey);
    }

    // ------  END SECURITY  ------ //



    @Value("${rabbitmq.routing.userData.key}")
    private String userDataKey;
    

    @Value("${rabbitmq.queue.userData.name}")
    private String userDataQueue;

    @Bean
    public Queue userDataQueue() {
        return new Queue(userDataQueue);
    }

    

    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;

    @Bean
    public TopicExchange dataExchange() {
        return new TopicExchange(dataExchange);
    }

    

    @Bean
    public Binding userDatadatauserDataBinding() {
        return BindingBuilder
                .bind(userDataQueue())
                .to(dataExchange())
                .with(userDataKey);
    }

    // ------  END USER DATA  ------ //

    // ----- GENERAL DATA HANDLER  ----- //

    @Value("${rabbitmq.routing.receiveData.key}")
    private String generalDataKey;

    @Value("${rabbitmq.queue.receiveData.name}")
    private String generalDataQueue;

    @Bean
    public Queue generalDataQueue() {
        return new Queue(generalDataQueue);
    }

    @Bean
    public Binding generalDataBinding() {
        return BindingBuilder
                .bind(generalDataQueue())
                .to(dataExchange())
                .with(generalDataKey);
    }

    // ----- END GENERAL DATA HANDLER  ----- //


    // ----- TRANSACTIONS HANDLER  ----- //



    @Value("${rabbitmq.routing.execTransaction.name}")
    private String transactionExecutionRoutingKey;

    @Value("${rabbitmq.exchange.transaction.name}")
    private String transactionExchange;

    @Value("${rabbitmq.queue.receiveTransaction.name}")
    private String transactionReceiveQueue;

    @Bean
    public Queue transactionReceiveQueue() {
        return new Queue(transactionReceiveQueue);
    }

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(transactionExchange);
    }

    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionReceiveQueue())
                .to(transactionExchange())
                .with(transactionExecutionRoutingKey);
    }


    /**
     * Creates a message converter for JSON messages.
     *
     * @return a new Jackson2JsonMessageConverter instance.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates an AMQP template for sending messages.
     *
     * @param connectionFactory the connection factory to use.
     * @return a new RabbitTemplate instance.
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
