package test.cliedis.produser;

import org.springframework.amqp.core.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CliedisProduserApplication {

	static final String exchangeName = "cliedis_status_exchange";

	static final String queueName = "cliedis_status_queue";

	static final String routingKey = "set_cliedis_status";

	@Bean
	Queue queue() {
		return new Queue(queueName, true);
	}

	@Bean
	DirectExchange exchange() {
		return new DirectExchange(exchangeName);
	}

	@Bean
	Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}

	public static void main(String[] args) {
		SpringApplication.run(CliedisProduserApplication.class, args);
	}

}
