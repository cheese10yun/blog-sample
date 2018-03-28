package com.cheese.embedded.controller;


import com.cheese.embedded.domain.Receiver;
import com.cheese.embedded.service.ReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/receivers")
public class ReceiverController {

    @Autowired
    private ReceiverService receiverService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Receiver create(@RequestBody Receiver receiver) {
        return receiverService.create(receiver);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Receiver getReceiver(@PathVariable long id) {
        return receiverService.findById(id);
    }

}
