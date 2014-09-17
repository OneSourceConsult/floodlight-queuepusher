/*
 * @(#)QueuePusherDeleteResource.java        1.00 2013
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.resource.Delete;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static cf.os.javalogger.core.Log.*;

public class QueuePusherDeleteResource extends ServerResource {
	
	protected static Logger logger = LoggerFactory.getLogger(QueuePusherDeleteResource.class);
	
	/**
	 * Function that handles REST request to delete QoS 
	 * 
	 * @param fmJson JSON string received from POST argument
	 * @return Response
	 */
	
	@SuppressWarnings("unchecked")
	@Delete
	public String del(String fmJson) {
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> args = null;
		try {
			 args = mapper.readValue(fmJson, Map.class);
		} catch (IOException e) { logger.warn("Error parsing JSON arguments", e); }
		
		measure("deleteQueue", (String)args.get("switchid"));
		
		boolean dummy = false;
		try {
			dummy = ((Integer)args.get("dummy")) == 1 ? true : false;
		} catch(Exception ex) { }
		
		QueuePusherResponse rsp = Utils.deleteQoS((String)args.get("switchid"), (String)args.get("qosuuid"), (String)args.get("queueuuid"), dummy);
		
		Map<String, Object> jsonRsp = new HashMap<String, Object>();
		jsonRsp.put("exitcode", rsp.code);
		jsonRsp.put("out", rsp.out);
		jsonRsp.put("err", rsp.err);
		
		String jsonString = "-1";
		try {
			jsonString = mapper.writeValueAsString(jsonRsp);
		} catch (JsonProcessingException e) {
			logger.warn("Problem parsing JSON response", e);
		}
		
		measure("deleteQueue_stop", (String)args.get("switchid"));
		return jsonString;
		
	}

}
