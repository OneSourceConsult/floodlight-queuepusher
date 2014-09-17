/*
 * @(#)QueuePusher.java        1.00 2013
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.SwitchMessagePair;
import net.floodlightcontroller.restserver.IRestApiService;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Joao Goncalves
 */
public class QueuePusher implements IOFMessageListener, IFloodlightModule, IQueuePusherService {
    
    protected IRestApiService restApi;
    protected IFloodlightProviderService floodlightProvider;
    protected ConcurrentCircularBuffer<SwitchMessagePair> buffer;
    
    protected static Logger logger;
    
    @Override
    public String getName() {
        return QueuePusher.class.getSimpleName();
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch(msg.getType()) {
            case PACKET_IN:
                buffer.add(new SwitchMessagePair(sw, msg));
                break;
            default:
                break;
        }
        return Command.CONTINUE;
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false; // We do not care of packet order
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false; // We do not care of packet order
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IQueuePusherService.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IQueuePusherService.class, this);
        return m;
    }

    /*
     * Register module in the system
     */
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IRestApiService.class);
        return l;
    }

    /*
     * Start everything and return the pointer to this module
     */
    
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApi = context.getServiceImpl(IRestApiService.class);
        buffer = new ConcurrentCircularBuffer<SwitchMessagePair>(SwitchMessagePair.class, 100);
        logger = LoggerFactory.getLogger(QueuePusher.class);
    }

    /*
     * Register listener type
     */
    
    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        restApi.addRestletRoutable(new QueuePusherWebRoutable());
    }

    @Override
    public ConcurrentCircularBuffer<SwitchMessagePair> getBuffer() {
        return buffer;
    }
    
}
