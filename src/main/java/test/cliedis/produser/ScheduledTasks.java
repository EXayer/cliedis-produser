package test.cliedis.produser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class ScheduledTasks {

    static class TransferObject {
        public int id; // TODO: tmp
        public int dealId;

        public TransferObject(int id) {
            this.id = id;
            this.dealId = ThreadLocalRandom.current().nextInt(1, 100000 + 1);
        }
    }

    private final RabbitTemplate rabbitTemplate;

    private int counter = 0;

    public ScheduledTasks(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleTaskWithFixedDelay() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ScheduledTasks.TransferObject transferObject = new ScheduledTasks.TransferObject(counter);

        String transferStr = "";
        try {
            transferStr = objectMapper.writeValueAsString(transferObject);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Sending #" + (counter + 1) + " message...");
        rabbitTemplate.convertAndSend(
                CliedisProduserApplication.exchangeName, CliedisProduserApplication.routingKey, transferStr
        );

        counter++;
    }
}
