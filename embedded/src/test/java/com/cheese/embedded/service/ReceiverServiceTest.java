package com.cheese.embedded.service;

import com.cheese.embedded.domain.Receiver;
import com.cheese.embedded.model.Address;
import com.cheese.embedded.model.Name;
import com.cheese.embedded.model.PhoneNumber;
import com.cheese.embedded.repository.ReceiverRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;

//@SpringBootTest(classes = EmbeddedApplication.class)
@RunWith(MockitoJUnitRunner.class)

public class ReceiverServiceTest {

    @Mock
    private ReceiverRepository receiverRepository;

    private ReceiverService receiverService;


    @Before
    public void setUp() throws Exception {
        receiverService = new ReceiverService(receiverRepository);
    }

    @Test
    public void findById() {
    }

    @Test
    public void create() {
    }

    @Test
    public void updateAddress() {
        //given
        final Receiver receiver = buildReceiver("서울");
        final Address newAddress = buildAddress("부산");
        given(receiverRepository.findOne(anyLong())).willReturn(receiver);

        //when
        final Receiver updateAddress = receiverService.updateAddress(anyLong(), newAddress);

        //then
        System.out.println(updateAddress);


    }

    private Receiver buildReceiver(String city) {
        return Receiver.builder()
                .address(buildAddress(city))
                .name(buildName())
                .phoneNumber(buildPhoneNumber())
                .build();
    }

    private PhoneNumber buildPhoneNumber() {
        return PhoneNumber.builder()
                .value("010-8233-8288")
                .build();
    }

    private Name buildName() {
        return Name.builder()
                .first("남윤")
                .last("김")
                .build();
    }

    private Address buildAddress(String city) {
        return Address.builder()
                .city(city)
                .street("신도림")
                .zipCode("071-224")
                .build();
    }

}