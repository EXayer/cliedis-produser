package test.cliedis.produser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import test.cliedis.produser.domain.CliedisStatus;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class ScheduledTasks {

    static class TransferObject {

        static class TransferBody {
            // Main platform deal id
            @JsonProperty("deal_id")
            public int dealId;

            // Main platform nbt deal id
            @JsonProperty("nbt_deal_id")
            public int nbtDealId;

            // CRM cliedis status (117).
            // Will be matched on main platform to ('no-updates', 'action-needed', 'not-matched')
            @JsonProperty("status")
            public String status;

            @JsonProperty("policy_id")
            public Long policyId;

            // Main platform deal type.
            // 'dsc', 'group', 'health_dental_travel', 'insurance', 'investments', 'mutual',
            // 'segregated', 'unlicensed'
            @JsonProperty("deal_type")
            public String dealType;

            public TransferBody() {
                this.dealType = "insurance";

                int dealId = ThreadLocalRandom.current().nextInt(1, 10000 + 1);
                this.dealId = dealId;
                this.nbtDealId = dealId;

                this.status = CliedisStatus.random().toString();
                this.policyId = ThreadLocalRandom.current().nextLong(1, 10000 + 1);
            }
        }

        @JsonProperty("type")
        public String eventType = "CRM_POLICY_STATUS_UPDATE";

        public TransferBody body;

        public TransferObject() {
            this.body = new TransferBody();
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

        ScheduledTasks.TransferObject transferObject = new ScheduledTasks.TransferObject();

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
