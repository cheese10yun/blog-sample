package com.cheese.embedded.service;

import com.cheese.embedded.domain.Receiver;
import com.cheese.embedded.model.Address;
import com.cheese.embedded.repository.ReceiverRepository;
import lombok.Builder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceiverService {

    private final ReceiverRepository receiverRepository;

    @Builder
    public ReceiverService(ReceiverRepository receiverRepository) {
        this.receiverRepository = receiverRepository;
    }

    @Transactional(readOnly = true)
    public Receiver findById(long id) {
        final Receiver receiver = receiverRepository.findOne(id);

        if (receiver == null) throw new IllegalArgumentException("...");

        return receiver;
    }

    @Transactional
    public Receiver create(Receiver receiver) {
        return receiverRepository.save(receiver);
    }

    @Transactional
    public Receiver updateAddress(long id, Address address) {
        final Receiver receiver = findById(id);
        receiver.updateAddress(address);
        return receiver;
    }
}
