/*
 * @(#)QueuePusherListResource.java        1.00 2013
 *
 * Copyright (c) 2013 OneSource, Consultoria Informatica.
 * All rights reserved.
 *
 * This software is copyright of OneSource Consultoria Informatica.
 * You use it only in accordance with the terms of the
 * license agreement you entered into
 * with OneSource.
 */

package net.floodlightcontroller.queuepusher;

import java.util.HashMap;
import java.util.Map;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static cf.os.javalogger.core.Log.*;

public class QueuePusherListResource extends ServerResource {
	
	protected static Logger logger = LoggerFactory.getLogger(QueuePusherListResource.class);
	
	@Get("json")
	public String retrieve() {
		
		String sid = (String) getRequestAttributes().get("switch");
		
		measure("listResources", sid);
		
		ObjectMapper mapper = new ObjectMapper();
		QueuePusherResponse rsp = new QueuePusherResponse(QueuePusherResponseCode.NOT_IMPLEMENTED);
		
		Map<String, Object> jsonRsp = new HashMap<String, Object>();
		jsonRsp.put("switch", sid);
		jsonRsp.put("qprsp", rsp.code);
		jsonRsp.put("out", rsp.out);
		jsonRsp.put("err", rsp.err);
		
		String jsonString = "-1";
		try {
			jsonString = mapper.writeValueAsString(jsonRsp);
		} catch (JsonProcessingException e) {
			logger.warn("Problem parsing JSON response", e);
		}
		
		measure("listResources_stop", sid);
		return jsonString;
		
	}

}
