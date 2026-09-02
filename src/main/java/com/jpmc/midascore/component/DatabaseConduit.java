// package com.jpmc.midascore.component;

// import com.jpmc.midascore.entity.UserRecord;
// import com.jpmc.midascore.repository.UserRepository;
// import org.springframework.stereotype.Component;

// @Component
// public class DatabaseConduit {
//     private final UserRepository userRepository;

//     public DatabaseConduit(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }

//     public void save(UserRecord userRecord) {
//         userRepository.save(userRecord);
//     }

// }

package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConduit {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    // This method is called by UserPopulator to save initial user records in tests
    public UserRecord save(UserRecord userRecord) {
        return userRepository.save(userRecord);
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void receiveTransaction(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        // Validate sender, recipient, and available balance
        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount());

            userRepository.save(sender);
            userRepository.save(recipient);

            TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount());
            transactionRepository.save(transactionRecord);
        }
    }
}