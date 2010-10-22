package kieker.monitoring.writer.filesystem;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.configuration.ConfigurationFileConstants;
import kieker.monitoring.writer.IMonitoringLogWriter;
import kieker.monitoring.writer.util.async.AbstractWorkerThread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * 
 * ==================LICENCE========================= Copyright 2006-2009 Kieker
 * Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. ==================================================
 */
/**
 * @author Matthias Rohr, Andre van Hoorn
 */
public final class AsyncFsWriter implements IMonitoringLogWriter {

	private static final Log log = LogFactory.getLog(AsyncFsWriter.class);
	// configuration parameter
	private static final int numberOfFsWriters = 1; // one is usually sufficient
													// and more usable since
													// only one file is created
													// at once
	// internal variables
	private final Vector<AbstractWorkerThread> workers =
			new Vector<AbstractWorkerThread>();
	private volatile BlockingQueue<IMonitoringRecord> blockingQueue = null;
	private final boolean blockOnFullQueue;
	private final String storagePathBaseDir;
	private final String storagePathPostfix;
	private String storageDir = null; // full path
	private int asyncRecordQueueSize = 8000;
	private final static String defaultConstructionErrorMsg =
			"Do not select this writer using the full-qualified classname. "
					+ "Use the the constant "
					+ ConfigurationFileConstants.WRITER_ASYNCFS
					+ " and the file system specific configuration properties.";

	public AsyncFsWriter() {
		throw new UnsupportedOperationException(
				AsyncFsWriter.defaultConstructionErrorMsg);
	}

	@Override
	public boolean init(final String initString) {
		throw new UnsupportedOperationException(
				AsyncFsWriter.defaultConstructionErrorMsg);
	}

	@Override
	public Vector<AbstractWorkerThread> getWorkers() {
		return this.workers;
	}

	/**
	 * 
	 * @param storagePathBaseDir
	 * @param storagePathPostfix
	 * @param asyncRecordQueueSize
	 * @param blockOnFullQueue
	 */
	public AsyncFsWriter(final String storagePathBaseDir,
			final String storagePathPostfix, final int asyncRecordQueueSize,
			final boolean blockOnFullQueue) {
		this.storagePathBaseDir = storagePathBaseDir;
		this.storagePathPostfix = storagePathPostfix;
		this.asyncRecordQueueSize = asyncRecordQueueSize;
		this.blockOnFullQueue = blockOnFullQueue;
		this.init();
	}

	private void init() {
		File f = new File(this.storagePathBaseDir);
		if (!f.isDirectory()) {
			AsyncFsWriter.log.error(this.storagePathBaseDir
					+ " is not a directory");
			AsyncFsWriter.log.error("Will abort init().");
			return;
		}

		final DateFormat m_ISO8601UTC =
				new SimpleDateFormat("yyyyMMdd'-'HHmmssSS");
		m_ISO8601UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String dateStr = m_ISO8601UTC.format(new java.util.Date());
		this.storageDir =
				this.storagePathBaseDir + "/tpmon-" + dateStr + "-UTC-"
						+ this.storagePathPostfix + "/";

		f = new File(this.storageDir);
		if (!f.mkdir()) {
			AsyncFsWriter.log.error("Failed to create directory '"
					+ this.storagePathBaseDir + "'");
			AsyncFsWriter.log.error("Will abort init().");
			return;
		}
		AsyncFsWriter.log.info("Directory for monitoring data: "
				+ this.storageDir);

		final MappingFileWriter mappingFileWriter;
		final String mappingFileFn =
				this.storageDir + File.separatorChar + "tpmon.map";
		try {
			mappingFileWriter = new MappingFileWriter(mappingFileFn);
		} catch (final Exception exc) {
			AsyncFsWriter.log.error("Failed to create mapping file '"
					+ mappingFileFn + "'", exc);
			AsyncFsWriter.log.error("Will abort init().");
			return;
		}

		this.blockingQueue =
				new ArrayBlockingQueue<IMonitoringRecord>(
						this.asyncRecordQueueSize);
		for (int i = 0; i < AsyncFsWriter.numberOfFsWriters; i++) {
			final FsWriterThread dbw =
					new FsWriterThread(this.blockingQueue, mappingFileWriter,
							this.storageDir + "/tpmon");
			dbw.setDaemon(true); // might lead to inconsistent data due to harsh
									// shutdown
			this.workers.add(dbw);
			dbw.start();
		}
		// System.out.println("(" + numberOfFsWriters +
		// " threads) will write to the file system");
		AsyncFsWriter.log.info("(" + AsyncFsWriter.numberOfFsWriters
				+ " threads) will write to the file system");
	}

	/**
	 * This method is not synchronized.
	 */
	@Override
	public boolean newMonitoringRecord(final IMonitoringRecord monitoringRecord) {
		try {
			if (this.blockOnFullQueue) {
				this.blockingQueue.offer(monitoringRecord); // blocks when queue
															// full
			} else {
				this.blockingQueue.add(monitoringRecord); // tries to add
															// immediately!
			}
		} catch (final Exception ex) {
			AsyncFsWriter.log
					.error(" insertMonitoringData() failed: Exception: " + ex);
			return false;
		}
		return true;
	}

	public String getFilenamePrefix() {
		return this.storagePathBaseDir;
	}

	@Override
	public String getInfoString() {
		return "filenamePrefix :" + this.storagePathBaseDir
				+ ", outputDirectory :" + this.storageDir
				+ ", asyncRecordQueueSize: " + this.asyncRecordQueueSize
				+ ", blockOnFullQueue: " + this.blockOnFullQueue;
	}
}
