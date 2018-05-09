package net.aionstudios.forefront.crons;

import net.aionstudios.forefront.cron.CronDateTime;
import net.aionstudios.forefront.cron.CronJob;
import net.aionstudios.forefront.nodes.NodeManager;

public class CronUpdate extends CronJob {

	public CronUpdate() {
		super(new CronDateTime());
		this.disableMark();
	}

	@Override
	public void run() {
		NodeManager.updateNodeData();
	}

}
