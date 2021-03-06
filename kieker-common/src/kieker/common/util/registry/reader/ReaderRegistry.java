/***************************************************************************
 * Copyright 2016 Kieker Project (http://kieker-monitoring.net)
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
package kieker.common.util.registry.reader;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an unsynchronized registry for read-only purposes. It is used by the readers within the analysis component.
 *
 * @author Christian Wulf
 *
 * @since 1.13
 */
// TODO move to kieker.common.registry.read
public class ReaderRegistry<E> {

	// TODO replace by a high performance map with primitive key type
	private final Map<Long, E> registryEntries = new HashMap<Long, E>(); // NOPMD (should be unsynchronized)

	/**
	 * Constructs an unsynchronized reader registry.
	 */
	public ReaderRegistry() {
		super();
	}

	public E get(final long key) {
		return this.registryEntries.get(key);
	}

	public void register(final long key, final E value) {
		this.registryEntries.put(key, value);
	}
}
