/*
 * @(#)QueuePusherWebRoutable.java        1.00 2013
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

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 *
 * @author Joao Goncalves
 */
public class QueuePusherWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/qpc/json", QueuePusherCreateResource.class);
        router.attach("/qpm/json", QueuePusherModifyResource.class);
        router.attach("/qpd/json", QueuePusherDeleteResource.class);
        router.attach("/qpl/{switch}/json", QueuePusherListResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/queuepusher";
    }    
    
}
