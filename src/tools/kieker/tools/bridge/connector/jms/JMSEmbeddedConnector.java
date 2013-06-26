/***************************************************************************
 * Copyright 2013 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.bridge.connector.jms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.activemq.broker.BrokerService;

import kieker.common.record.IMonitoringRecord;
import kieker.tools.bridge.connector.ConnectorDataTransmissionException;

/**
 * 
 * 
 * @author Reiner Jung
 * @since 1.8
 */
public class JMSEmbeddedConnector extends JMSClientConnector {
	private BrokerService broker;
	private final int port;

	/**
	 * Construct a new JMS service consumer and an embedded JMS service.
	 * 
	 * @param configuration
	 *            Kieker configuration object
	 * @param lookupEntityMap
	 *            IMonitoringRecord id map
	 * @param port
	 *            Port the JMS service is listening to
	 * @throws URISyntaxException
	 *             if the URI is malformed. Most likely will not happen.
	 */
	public JMSEmbeddedConnector(final Map<Integer, Class<IMonitoringRecord>> recordMap, final int port) throws URISyntaxException {
		super(recordMap, null, null, new URI("tcp://localhost:" + port));
		this.port = port;
	}

	@Override
	public void initialize() throws ConnectorDataTransmissionException {
		this.broker = new BrokerService();
		this.broker.setUseJmx(true);
		try {
			this.broker.addConnector("tcp://localhost:" + this.port);
			this.broker.start();
			super.initialize();
			// CHECKSTYLE:OFF IllegalCatch - Exception cannot be avoided as the broker actually throws this.
		} catch (final Exception e) {
			// CHECKSTYLE:ON
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}
	}

	@Override
	public void close() throws ConnectorDataTransmissionException {
		try {
			super.close();
			this.broker.stop();
			// CHECKSTYLE:OFF IllegalCatch - Exception cannot be avoided as the broker actually throws this.
		} catch (final Exception e) {
			// CHECKSTYLE:ON
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}
	}
}