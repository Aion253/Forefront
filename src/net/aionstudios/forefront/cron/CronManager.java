package net.aionstudios.forefront.cron;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.aionstudios.forefront.service.DateTimeServices;

public class CronManager {
	
	private static List<CronJob> jobs = new ArrayList<CronJob>();
	private static boolean cronStarted = false;
	private static Thread cronThread;
	private static ExecutorService jobExecutor;
	
	public static void startCron(long restTime) {
		if(!cronStarted) {
			cronStarted = true;
			jobExecutor = Executors.newCachedThreadPool();
			cronThread = new Thread() {
			    public void run() {
			    	int min = DateTimeServices.getCronMinute()-1;
			    	int hour = 0;
			    	int dom = 0;
			    	int month = 0;
			    	int dow = 0;
			    	int year = 0;
			    	while(cronStarted) {
			    		if(min!=DateTimeServices.getCronMinute()) {
			    			min = DateTimeServices.getCronMinute();
			    			hour = DateTimeServices.getCronHour();
			    			dom = DateTimeServices.getCronDayOfMonth();
			    			month = DateTimeServices.getCronMonth();
			    			dow = DateTimeServices.getCronDayOfWeek();
			    			year = DateTimeServices.getCronYear();
			    			for(CronJob j : jobs) {
			    				if(j.cronMatches(min, hour, dom, month, dow, year)) {
			    					jobExecutor.submit(new Runnable() {
										@Override public void run() {j.start();}
			    					});
			    				}
			    			}
			    		} else {
			    			for(CronJob j : jobs) {
			    				if(!j.isMarkEnabled()&&j.cronMatches(min, hour, dom, month, dow, year)) {
			    					jobExecutor.submit(new Runnable() {
										@Override public void run() {j.start();}
			    					});
			    				}
			    			}
			    		}
			    		try {
				            Thread.sleep(restTime);
				        } catch(InterruptedException e) {
				            System.err.println("Cron thread was interrupted!");
				            e.printStackTrace();
				        }
			    	}
			    }  
			};

			cronThread.start();
		}
	}
	
	public static void addJob(CronJob j) {
		jobs.add(j);
	}

}
