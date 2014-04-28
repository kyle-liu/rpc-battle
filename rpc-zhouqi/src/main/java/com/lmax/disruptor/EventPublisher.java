/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;


/**
 * Utility class for simplifying publication to the ring buffer.
 */
public class EventPublisher<E>
{
    private final RingBuffer<E> ringBuffer;

    /**
     * Construct from the ring buffer to be published to.
     * @param ringBuffer into which events will be published.
     */
    public EventPublisher(final RingBuffer<E> ringBuffer)
    {
        this.ringBuffer = ringBuffer;
    }
    
    /**
     * Publishes an event to the ring buffer.  It handles
     * claiming the next sequence, getting the current (uninitialized) 
     * event from the ring buffer and publishing the claimed sequence
     * after translation.
     * 
     * @param translator The user specified translation for the event
     */
    public void publishEvent(final EventTranslator<E> translator)
    {
        final long sequence = ringBuffer.next();
        translateAndPublish(translator, sequence);
    }
    
    /**
     * Attempts to publish an event to the ring buffer.  It handles
     * claiming the next sequence, getting the current (uninitialized) 
     * event from the ring buffer and publishing the claimed sequence
     * after translation.  Will return false if specified capacity
     * was not available.
     * 
     * @param translator The user specified translation for the event
     * @param capacity The capacity that should be available before publishing
     * @return true if the value was published, false if there was insufficient
     * capacity.
     */
    public boolean tryPublishEvent(EventTranslator<E> translator, int capacity)
    {
        try
        {
            final long sequence = ringBuffer.tryNext(capacity);
            translateAndPublish(translator, sequence);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    private void translateAndPublish(final EventTranslator<E> translator, final long sequence)
    {
        try
        {
            translator.translateTo(ringBuffer.get(sequence), sequence);
        }
        finally
        {
            ringBuffer.publish(sequence);
        }
    }
}
