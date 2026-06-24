package com.bank.onboarding.event;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ConsumedOnboardingEventStore {

    private final Queue<CustomerOnboardedEvent> consumedEvents = new ConcurrentLinkedQueue<>();

    public void record(CustomerOnboardedEvent event) {
        consumedEvents.add(event);
    }

    public List<CustomerOnboardedEvent> snapshot() {
        return new ArrayList<>(consumedEvents);
    }

    public void clear() {
        consumedEvents.clear();
    }
}

