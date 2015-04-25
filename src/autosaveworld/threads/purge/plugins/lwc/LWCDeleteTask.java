package autosaveworld.threads.purge.plugins.lwc;

import autosaveworld.threads.purge.taskqueue.Task;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

public class LWCDeleteTask implements Task {

	private Protection protection;

	public LWCDeleteTask(Protection protection) {
		this.protection = protection;
	}

	@Override
	public boolean isHeavyTask() {
		return false;
	}

	@Override
	public void performTask() {
		LWC.getInstance().getPhysicalDatabase().removeProtection(protection.getId());
	}

}
