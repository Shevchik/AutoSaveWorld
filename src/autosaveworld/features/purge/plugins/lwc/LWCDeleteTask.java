package autosaveworld.features.purge.plugins.lwc;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

import autosaveworld.features.purge.taskqueue.Task;

public class LWCDeleteTask implements Task {

	private Protection protection;

	public LWCDeleteTask(Protection protection) {
		this.protection = protection;
	}

	@Override
	public boolean doNotQueue() {
		return false;
	}

	@Override
	public void performTask() {
		LWC.getInstance().getPhysicalDatabase().removeProtection(protection.getId());
	}

}
