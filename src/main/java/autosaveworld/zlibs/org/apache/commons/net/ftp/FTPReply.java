/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package autosaveworld.zlibs.org.apache.commons.net.ftp;

public final class FTPReply {

	public static final int RESTART_MARKER = 110;
	public static final int SERVICE_NOT_READY = 120;
	public static final int DATA_CONNECTION_ALREADY_OPEN = 125;
	public static final int FILE_STATUS_OK = 150;
	public static final int COMMAND_OK = 200;
	public static final int COMMAND_IS_SUPERFLUOUS = 202;
	public static final int SYSTEM_STATUS = 211;
	public static final int DIRECTORY_STATUS = 212;
	public static final int FILE_STATUS = 213;
	public static final int HELP_MESSAGE = 214;
	public static final int NAME_SYSTEM_TYPE = 215;
	public static final int SERVICE_READY = 220;
	public static final int SERVICE_CLOSING_CONTROL_CONNECTION = 221;
	public static final int DATA_CONNECTION_OPEN = 225;
	public static final int CLOSING_DATA_CONNECTION = 226;
	public static final int ENTERING_PASSIVE_MODE = 227;
	public static final int ENTERING_EPSV_MODE = 229;
	public static final int USER_LOGGED_IN = 230;
	public static final int FILE_ACTION_OK = 250;
	public static final int PATHNAME_CREATED = 257;
	public static final int NEED_PASSWORD = 331;
	public static final int NEED_ACCOUNT = 332;
	public static final int FILE_ACTION_PENDING = 350;
	public static final int SERVICE_NOT_AVAILABLE = 421;
	public static final int CANNOT_OPEN_DATA_CONNECTION = 425;
	public static final int TRANSFER_ABORTED = 426;
	public static final int FILE_ACTION_NOT_TAKEN = 450;
	public static final int ACTION_ABORTED = 451;
	public static final int INSUFFICIENT_STORAGE = 452;
	public static final int UNRECOGNIZED_COMMAND = 500;
	public static final int SYNTAX_ERROR_IN_ARGUMENTS = 501;
	public static final int COMMAND_NOT_IMPLEMENTED = 502;
	public static final int BAD_COMMAND_SEQUENCE = 503;
	public static final int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER = 504;
	public static final int NOT_LOGGED_IN = 530;
	public static final int NEED_ACCOUNT_FOR_STORING_FILES = 532;
	public static final int FILE_UNAVAILABLE = 550;
	public static final int PAGE_TYPE_UNKNOWN = 551;
	public static final int STORAGE_ALLOCATION_EXCEEDED = 552;
	public static final int FILE_NAME_NOT_ALLOWED = 553;
	public static final int SECURITY_DATA_EXCHANGE_COMPLETE = 234;
	public static final int SECURITY_DATA_EXCHANGE_SUCCESSFULLY = 235;
	public static final int SECURITY_MECHANISM_IS_OK = 334;
	public static final int SECURITY_DATA_IS_ACCEPTABLE = 335;
	public static final int UNAVAILABLE_RESOURCE = 431;
	public static final int BAD_TLS_NEGOTIATION_OR_DATA_ENCRYPTION_REQUIRED = 522;
	public static final int DENIED_FOR_POLICY_REASONS = 533;
	public static final int REQUEST_DENIED = 534;
	public static final int FAILED_SECURITY_CHECK = 535;
	public static final int REQUESTED_PROT_LEVEL_NOT_SUPPORTED = 536;
	public static final int EXTENDED_PORT_FAILURE = 522;

	private FTPReply() {
	}

	public static boolean isPositivePreliminary(int reply) {
		return ((reply >= 100) && (reply < 200));
	}

	public static boolean isPositiveCompletion(int reply) {
		return ((reply >= 200) && (reply < 300));
	}

	public static boolean isPositiveIntermediate(int reply) {
		return ((reply >= 300) && (reply < 400));
	}

	public static boolean isNegativeTransient(int reply) {
		return ((reply >= 400) && (reply < 500));
	}

	public static boolean isNegativePermanent(int reply) {
		return ((reply >= 500) && (reply < 600));
	}

	public static boolean isProtectedReplyCode(int reply) {
		return ((reply >= 600) && (reply < 700));
	}

}
