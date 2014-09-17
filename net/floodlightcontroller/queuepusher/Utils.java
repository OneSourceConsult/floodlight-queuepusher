/*
 * @(#)Utils.java        1.00 2013
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	
	static class Queue {
		
		public int qid;
		public String uuid;
		
		public Queue(int qid, String uuid) {
			this.qid = qid;
			this.uuid = uuid;
		}
		
	}
	
	protected static Logger logger = LoggerFactory.getLogger(Utils.class);

	public static final String OVS = "ovs-vsctl";
	
	public static int qid = 0;
	
	private static List<Integer> idsInUse = Collections.synchronizedList(new ArrayList<Integer>());
	private static Map<String, Integer> uuidToQoSId = Collections.synchronizedMap(new HashMap<String, Integer>());
	private static Map<String, String> uuidToPort = Collections.synchronizedMap(new HashMap<String, String>());
	private static Map<String, String> switchPortWithQueue = Collections.synchronizedMap(new HashMap<String, String>());
	private static Map<String, List<Queue>> qosUuidToQueues = Collections.synchronizedMap(new HashMap<String, List<Queue>>());

	private Utils() { }
	
	/**
	 * Creates a new slice
	 * 
	 * @param switchid DPID of the switch
	 * @param port Port on the switch to apply the queue 
	 * @param rate Max rate of the queue
	 * @return
	 */
	
	public static QueuePusherResponse createSlice(String switchid, String port, int rate, boolean dummy) {
		if(!Utils.checkOVS()) return new QueuePusherResponse(QueuePusherResponseCode.OVS_NOT_FOUND);
		if(switchid == null) return new QueuePusherResponse(QueuePusherResponseCode.INVALID_ARGUMENTS);
		
		String qosuiid = switchPortWithQueue.get(switchid+port);
		Object[] sw = QueuePusherSwitchMapper.getMatch(switchid);
		
		if(dummy) return new QueuePusherResponse(QueuePusherResponseCode.QP_DUMMY);
		
		do {
			qid++;
		} while(idsInUse.contains(qid));
		idsInUse.add(qid);
		
		String cmd = Utils.OVS + " --db=tcp:" + (String)sw[0] + ":9091";
		if(qosuiid != null) {
			cmd += " set qos " + qosuiid + " type=linux-htb other-config:max-rate=10000000000000000000000000 queues:" + qid + "=@lequeue "
					+ "-- --id=@lequeue create queue other-config:max-rate=" + rate;
		} else {
			cmd += " set port " + port + " qos=@leqos -- --id=@leqos "
					+ "create qos type=linux-htb other-config:max-rate=10000000000000000000000000 queues:" + qid + "=@lequeue "
					+ "-- --id=@lequeue create queue other-config:max-rate=" + rate;
		}
	
		if(qid <= 0) qid = 1;
		logger.info("RUNNING: "+cmd);
		Object[] rsp = Utils.eval(cmd);
		
		if((Integer)rsp[0] == 0 && ((String)rsp[1]).length() == 74 && qosuiid == null) {
			switchPortWithQueue.put(switchid+port, ((String)rsp[1]).substring(0, 36));
			uuidToQoSId.put(((String)rsp[1]).substring(0, 36), qid);
			uuidToPort.put(((String)rsp[1]).substring(0, 36), port);
		}
		
		if((Integer)rsp[0] == 0) {
			List<Queue> installedQueues;
			if(qosuiid == null) {
				installedQueues = new ArrayList<Queue>();
				installedQueues.add(new Queue(qid, ((String)rsp[1]).substring(36, 72)));
				qosUuidToQueues.put(((String)rsp[1]).substring(0, 36), installedQueues);
			} else {
				installedQueues = qosUuidToQueues.get(qosuiid);
				installedQueues.add(new Queue(qid, ((String)rsp[1]).substring(0, 36)));
			}
		}
		
		if(qosuiid == null) {
			qosuiid = "";
		} else {
			qosuiid += "\n";
		}
		
		return new QueuePusherResponse((Integer)rsp[0] == 0 ? QueuePusherResponseCode.SUCCESS : QueuePusherResponseCode.CREATE_FAIL, qosuiid+(String)rsp[1], (String)rsp[2], qid);
	}
	
	/**
	 * Deletes a QoS slice
	 * 
	 * @param switchid Switch DPID
	 * @param uuid Queue name
	 * @return
	 */
	
	public static QueuePusherResponse deleteQoS(String switchid, String qosuuid, String queueuuid, boolean dummy) {		
		if(!Utils.checkOVS()) return new QueuePusherResponse(QueuePusherResponseCode.OVS_NOT_FOUND);
		if(qosuuid == null || queueuuid == null) return new QueuePusherResponse(QueuePusherResponseCode.INVALID_ARGUMENTS);
		
		Object[] sw = QueuePusherSwitchMapper.getMatch(switchid);
		
		if(dummy) return new QueuePusherResponse(QueuePusherResponseCode.QP_DUMMY);
		
		String port = uuidToPort.remove(qosuuid);
		String cmd = "";
		
		if(port != null) {
			cmd = Utils.OVS + " --db=tcp:" + (String)sw[0] + ":9091 -- clear Port " + port + " qos";
			logger.info("RUNNING: "+cmd);
			Utils.eval(cmd);
		} else {
			logger.warn("Port matching returned NULL");
		}
		
		String newQosUuid = null;
		
		Object[] rsp = new Object[] { (int)1, "Floodlight returned NULL ip address" };
		if(sw[0] != null) {
			cmd = Utils.OVS + " --db=tcp:" + (String)sw[0] + ":9091 -- destroy QoS " + qosuuid;
			logger.info("RUNNING: "+cmd);
			rsp = Utils.eval(cmd);
		
			cmd = Utils.OVS + " --db=tcp:" + (String)sw[0] + ":9091 -- destroy queue " + queueuuid;
			logger.info("RUNNING: "+cmd);
			Utils.eval(cmd);
		
			if((Integer)rsp[0] == 0) {
				try {
					int qid = uuidToQoSId.remove(qosuuid);
					idsInUse.remove(qid);
				} catch(Exception ex) { }
			}
			
			switchPortWithQueue.remove(switchid+port);
			newQosUuid = reinstateQueues(switchid, port, qosuuid, queueuuid);
			if(newQosUuid == null) {
				newQosUuid = "";
			} else {
				newQosUuid += "\n";
			}
		} else {
			logger.warn("Floodlight returned NULL ip address");
		}
		
		return new QueuePusherResponse((Integer)rsp[0] == 0 ? QueuePusherResponseCode.SUCCESS : QueuePusherResponseCode.DELETE_FAIL, newQosUuid+(String)rsp[1], (String)rsp[2]);
	}
	
	private static String reinstateQueues(String switchid, String port, String qosuuid, String removedQueue) {
		
		Object[] sw = QueuePusherSwitchMapper.getMatch(switchid);
		
		String cmd = Utils.OVS + " --db=tcp:" + (String)sw[0] + ":9091 set port " + port + " qos=@leqos -- --id=@leqos "
					+ "create qos type=linux-htb other-config:max-rate=10000000000000000000000000 queues=";
		
		List<Queue> installedQueues = qosUuidToQueues.remove(qosuuid);
		
		Queue toRemove = null;
		boolean start = true;
		for(Queue q : installedQueues) {
			if(!removedQueue.equalsIgnoreCase(q.uuid)) {
				cmd += (start ? "" : ",")+q.qid+"="+q.uuid;
				start = false;
			} else {
				toRemove = q;
			}
		}
		
		installedQueues.remove(toRemove);
		logger.info("RUNNING: "+cmd);
		Object[] rsp = Utils.eval(cmd);
		
		if((Integer)rsp[0] == 0) {
			switchPortWithQueue.put(switchid+port, ((String)rsp[1]).substring(0, 36));
			uuidToQoSId.put(((String)rsp[1]).substring(0, 36), qid);
			uuidToPort.put(((String)rsp[1]).substring(0, 36), port);
			qosUuidToQueues.put(((String)rsp[1]).substring(0, 36), installedQueues);
			
			return ((String)rsp[1]).substring(0, 36);
		} else {
			return null;
		}		
		
	}
	
	/**
	 * Modifies a QoS slice
	 * 
	 * @param switchid Switch DPID
	 * @param uuid UUID of the queue to modify
	 * @param rate New rate of the queue
	 * @return
	 */
	
	public static QueuePusherResponse modifySlice(String switchid, String qosuuid, String queueuuid, int rate, boolean dummy) {
		deleteQoS(switchid, qosuuid, queueuuid, dummy);
		return createSlice(switchid, QueuePusherSwitchMapper.portMatcher.get(queueuuid), rate, dummy);
	}
	
	/**
	 * Checks the existence of OVS binary on the system PATH
	 * 
	 * @return Successful execution
	 */
	
	public static boolean checkOVS() {
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		try {
			proc = rt.exec("which " + OVS);
			proc.waitFor();
			return proc.exitValue() == 0 ? true : false;
		} catch (InterruptedException e) {
			return false;
		} catch(IOException e) {
			return false;
		}
	}
	
	/**
	 * Runs the given command
	 * 
	 * @param cmd Command to execute
	 * @return 0: (int)exit code 1: (string)stdout 2: (string)stderr
	 */
	
	public static Object[] eval(String cmd) {
		
		Object[] rsp = new Object[3];
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		
		try {
			proc = rt.exec(cmd);
			proc.waitFor();
			rsp[0] = proc.exitValue();
		} catch (IOException e) {
			rsp[0] = 1;
		} catch(InterruptedException e) {
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
						sb.append(temp+"\n");
					}
					
					rsp[1] = sb.toString();
					sb = new StringBuilder();
					while((temp = stderr.readLine()) != null) {
						sb.append(temp+"\n");
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
