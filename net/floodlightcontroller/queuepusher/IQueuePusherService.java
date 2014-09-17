/*
 * @(#)IQueuePusherService.java        1.00 2013
 *
 * Copyright (c) 2013 OneSource, Consultoria Informatica.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of OneSource
 * Consultoria Informatica. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with OneSource.
 */

package net.floodlightcontroller.queuepusher;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.SwitchMessagePair;

/**
 *
 * @author Joao Goncalves
 */
public interface IQueuePusherService extends IFloodlightService {
    public ConcurrentCircularBuffer<SwitchMessagePair> getBuffer();
}
