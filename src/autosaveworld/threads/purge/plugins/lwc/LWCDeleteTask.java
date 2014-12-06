package autosaveworld.threads.purge.plugins.lwc;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

public class LWCDeleteTask implements LWCPurgeTask {

	private Protection protection;

	public LWCDeleteTask(Protection protection) {
		this.protection = protection;
	}

	@Override
	public void performTask() {
		LWC.getInstance().getPhysicalDatabase().removeProtection(protection.getId());
	}

}
