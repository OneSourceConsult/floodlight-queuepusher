/*
 * @(#)QueuePusherModifyResource.java        1.00 2013
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

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static cf.os.javalogger.core.Log.*;

public class QueuePusherModifyResource extends ServerResource {
	
	protected static Logger logger = LoggerFactory.getLogger(QueuePusherModifyResource.class);
	
	/**
	 * Function that handles REST request to modify QoS
	 * 
	 * @param fmJson JSON string received from POST argument
	 * @return Response
	 */
	
	@SuppressWarnings("unchecked")
	@Post
	public String store(String fmJson) {
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> args = null;
		try {
			 args = mapper.readValue(fmJson, Map.class);
		} catch (IOException e) { logger.warn("Error parsing JSON arguments", e); }
		
		measure("modifyQueue", (String)args.get("switchid"));
		
		Map<String, Object> jsonRsp = new HashMap<String, Object>();
		
		boolean dummy = false;
		try {
			dummy = ((Integer)args.get("dummy")) == 1 ? true : false;
		} catch(Exception ex) { }
		
		QueuePusherResponse rsp = Utils.modifySlice((String)args.get("switchid"), (String)args.get("qosuuid"), (String)args.get("queueuuid"), (Integer)args.get("rate"), dummy);
		if(rsp.code == QueuePusherResponseCode.SUCCESS && rsp.out.length() == 74) {
			jsonRsp.put("qosuuid", rsp.out.substring(0, 36));
			jsonRsp.put("queueuuid", rsp.out.substring(36 + 1, rsp.out.length() - 1));
			jsonRsp.put("out", "");
		} else {
			jsonRsp.put("qosuuid", "");
			jsonRsp.put("queueuuid", "");
			jsonRsp.put("out", rsp.out);
		}
		
		jsonRsp.put("exitcode", rsp.code);
		jsonRsp.put("err", rsp.err);
		
		String jsonString = "-1";
		try {
			jsonString = mapper.writeValueAsString(jsonRsp);
		} catch (JsonProcessingException e) {
			logger.warn("Problem parsing JSON response", e);
		}
		
		measure("modifyQueue_stop", (String)args.get("switchid"));
		return jsonString;
		
	}

}
