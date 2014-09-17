/*
 * @(#)QueuePusherResponseCode.java        1.00 2013
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

public enum QueuePusherResponseCode {
	OVS_NOT_FOUND,
	INVALID_ARGUMENTS,
	CREATE_FAIL,
	DELETE_FAIL,
	DELETE_IGNORED,
	NOT_IMPLEMENTED,
	UNKNOWN_CMD,
	SUCCESS,
	QP_DUMMY
}
