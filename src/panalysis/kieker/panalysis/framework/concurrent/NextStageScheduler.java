/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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

package kieker.panalysis.framework.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import kieker.panalysis.framework.core.IPipeline;
import kieker.panalysis.framework.core.IStage;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class NextStageScheduler {

	protected final Map<IStage, Boolean> statesOfStages = new HashMap<IStage, Boolean>();
	private final Collection<IStage> highestPrioritizedEnabledStages = new ArrayList<IStage>();
	private final StageWorkList workList;

	public NextStageScheduler(final IPipeline pipeline, final int accessesDeviceId) throws Exception {
		this.workList = new StageWorkList(accessesDeviceId, pipeline.getStages().size());

		this.highestPrioritizedEnabledStages.addAll(pipeline.getStartStages());

		this.workList.pushAll(this.highestPrioritizedEnabledStages);
		System.out.println("Initial work list: " + this.workList);
		// this.workList.addAll(pipeline.getStages());

		for (final IStage stage : pipeline.getStages()) {
			this.enable(stage);
		}
	}

	public IStage get() {
		return this.workList.read();
	}

	public boolean isAnyStageActive() {
		// System.out.println("workList: " + this.workList);
		return !this.workList.isEmpty();
	}

	protected void enable(final IStage stage) {
		// // / TODO consider to move state (enabled/disabled) of stage to stage for performance reasons
		this.statesOfStages.put(stage, Boolean.TRUE);
	}

	public void disable(final IStage stage) {
		this.statesOfStages.put(stage, Boolean.FALSE);
		// if (!Thread.currentThread().getName().equals("startThread")) {
		// }
		// System.out.println("Disabled stage " + stage + ", statesOfStages: " + this.statesOfStages);

		if (this.highestPrioritizedEnabledStages.contains(stage)) {
			this.highestPrioritizedEnabledStages.remove(stage);
			for (final IStage outputStage : stage.getAllOutputStages()) {
				if (this.statesOfStages.get(outputStage) == Boolean.TRUE) {
					this.highestPrioritizedEnabledStages.add(outputStage);
				}
			}
		}

		stage.fireSignalClosingToAllOutputPorts();
	}

	public void determineNextStage(final IStage stage, final boolean executedSuccessfully) {
		final Collection<? extends IStage> outputStages = stage.getContext().getOutputStages();
		// System.out.println("outputStages: " + outputStages);
		if (outputStages.size() > 0) {
			if (stage.getContext().inputPortsAreEmpty()) {
				this.workList.pop();
			}

			// TODO consider to not add the stage again if it has a cyclic pipe
			// while (outputStages.remove(stage)) {
			// }

			this.workList.pushAll(outputStages);

			stage.getContext().getOutputStages().clear();
		} else {
			this.workList.pop();
		}

		if (this.workList.isEmpty()) {
			this.workList.pushAll(this.highestPrioritizedEnabledStages);
		}
	}
}