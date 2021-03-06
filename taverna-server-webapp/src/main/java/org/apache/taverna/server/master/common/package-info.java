/*
 */
/**
 * This package contains the common XML elements used throughout Taverna Server's various interfaces.
 * @author Donal Fellows
 */
@XmlSchema(namespace = SERVER, elementFormDefault = QUALIFIED, attributeFormDefault = QUALIFIED, xmlns = {
		@XmlNs(prefix = "xlink", namespaceURI = XLINK),
		@XmlNs(prefix = "ts", namespaceURI = SERVER),
		@XmlNs(prefix = "ts-rest", namespaceURI = SERVER_REST),
		@XmlNs(prefix = "ts-soap", namespaceURI = SERVER_SOAP),
		@XmlNs(prefix = "feed", namespaceURI = FEED),
		@XmlNs(prefix = "admin", namespaceURI = ADMIN) })
package org.apache.taverna.server.master.common;
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

import static javax.xml.bind.annotation.XmlNsForm.QUALIFIED;
import static org.apache.taverna.server.master.common.Namespaces.ADMIN;
import static org.apache.taverna.server.master.common.Namespaces.FEED;
import static org.apache.taverna.server.master.common.Namespaces.SERVER;
import static org.apache.taverna.server.master.common.Namespaces.SERVER_REST;
import static org.apache.taverna.server.master.common.Namespaces.SERVER_SOAP;
import static org.apache.taverna.server.master.common.Namespaces.XLINK;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;

