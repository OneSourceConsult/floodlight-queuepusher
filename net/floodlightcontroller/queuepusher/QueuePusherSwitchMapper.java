/*
 * @(#)QueuePusherSwitchMapper.java        1.00 2013
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class QueuePusherSwitchMapper {
	
	protected static Logger logger = LoggerFactory.getLogger(QueuePusherSwitchMapper.class);
	
	private static Map<String, Object[]> dpidMatcher = new HashMap<String, Object[]>();
	
	public static Map<String, String> portMatcher = new HashMap<String, String>();
	
	/**
	 * Search local DB for IP and PORT match for this switch DPID
	 * 
	 * @param dpid Switch DPID
	 * @return 0: ip 1: port
	 */
	
	public static Object[] getMatch(String dpid) {
		Object[] obj = dpidMatcher.get(dpid);
		if(obj == null) {
			obj = getCURLMatch(dpid);
			dpidMatcher.put(dpid, obj);
		}
		return obj;
	}
	
	/**
	 * Given the switch DPID it returns its ip and port by querying floodlight
	 * 
	 * @param dpid Switch dpid
	 * @return 0: ip 1: port
	 */
	
	private static Object[] getCURLMatch(String dpid) {
		
		Object[] rsp = eval("curl -s http://127.0.0.1:8080/wm/core/controller/switches/json");
		ObjectMapper mapper = new ObjectMapper();
		
		List<Map<String, Object>> args = null;
		try {
			logger.info("JSON received from CURL: "+rsp[1]);
			 args = mapper.readValue((String)rsp[1], TypeFactory.defaultInstance().constructCollectionType(List.class, Map.class));
		} catch (IOException e) { logger.warn("Error parsing JSON arguments", e); }
		
		String ip = null;
		int port = 0;		
		for(Map<String, Object> entry : args) {
			if(((String)entry.get("dpid")).equals(dpid)) {
				String temp = ((String)entry.get("inetAddress")).substring(1);
				ip = temp.substring(0, temp.indexOf(":"));
				port = Integer.parseInt(temp.substring(temp.indexOf(":") + 1));
				break;
			}
		}
		
		return new Object[] {ip, port};
		
	}
	
	/**
	 * Runs the given command
	 * 
	 * @param cmd Command to execute
	 * @return 0: (int)exit code 1: (string)stdout 2: (string)stderr
	 */
	
	private static Object[] eval(String cmd) {
		
		Object[] rsp = new Object[3];
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		
		try {
			proc = rt.exec(cmd);
			proc.waitFor();
			rsp[0] = proc.exitValue();
		} catch (InterruptedException e) {
			rsp[0] = 1;
		} catch (IOException e) {
			rsp[0] = 1;
		} finally {
			if(proc == null) {
				rsp[0] = 1;
			} else {
				
				try {
					
					BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					
					String temp;
					StringBuilder sb = new StringBuilder();
					while((temp = stdout.readLine()) != null) {
						sb.append(temp);
					}
					
					rsp[1] = sb.toString();
					sb = new StringBuilder();
					while((temp = stderr.readLine()) != null) {
						sb.append(temp);
					}
					
					rsp[2] = sb.toString();
					
				} catch(IOException e) {
					rsp[0] = 1;
				}
				
			}
		}
		
		return rsp;
		
	}
	
}
