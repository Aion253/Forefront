package net.aionstudios.forefront.cron;

public abstract class CronJob {
	
	private CronDateTime cdt;
	private boolean enabled = true;
	private boolean markEnabled = true;
	
	public CronJob(CronDateTime cdt) {
		this.cdt = cdt;
	}
	
	public void setCronDateTime(CronDateTime cdt) {
		this.cdt = cdt;
	}
	
	public CronDateTime getCronDateTime() {
		return cdt;
	}
	
	public boolean cronMatchesNow() {
		return cdt.matchesNow();
	}
	
	public boolean cronMatches(int min, int hour, int dom, int month, int dow, int year) {
		return cdt.matches(min, hour, dom, month, dow, year);
	}
	
	public void start() {
		run();
	}
	
	public abstract void run();
	
	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public void enableMark() {
		markEnabled = true;
	}
	
	public void disableMark() {
		markEnabled = false;
	}
	
	public boolean isMarkEnabled() {
		return markEnabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

}
