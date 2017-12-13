package com.heapbrain.core.testdeed.to;

/**
 * @author AbdulJeilani
 */

public class GatlingConfiguration {
	Double constantUsersPerSec=new Double(0);
	String duration="";
	String maxDuration="";
	int atOnceUsers=0;
	int rampUser=0;
	String rampUserOver="";
	Double rampUsersPerSec=new Double(0);
	Double rampUsersPerSecTo=new Double(0);
	String rampUsersPerSecDuring="";
	String nothingFor="";
	int status=0;
	String maxResponseTime="";
	
	public Double getConstantUsersPerSec() {
		return constantUsersPerSec;
	}
	public void setConstantUsersPerSec(Double constantUsersPerSec) {
		this.constantUsersPerSec = constantUsersPerSec;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getMaxDuration() {
		return maxDuration;
	}
	public void setMaxDuration(String maxDuration) {
		this.maxDuration = maxDuration;
	}
	public int getAtOnceUsers() {
		return atOnceUsers;
	}
	public void setAtOnceUsers(int atOnceUsers) {
		this.atOnceUsers = atOnceUsers;
	}
	public int getRampUser() {
		return rampUser;
	}
	public void setRampUser(int rampUser) {
		this.rampUser = rampUser;
	}
	public String getRampUserOver() {
		return rampUserOver;
	}
	public void setRampUserOver(String rampUserOver) {
		this.rampUserOver = rampUserOver;
	}
	public Double getRampUsersPerSec() {
		return rampUsersPerSec;
	}
	public void setRampUsersPerSec(Double rampUsersPerSec) {
		this.rampUsersPerSec = rampUsersPerSec;
	}
	public Double getRampUsersPerSecTo() {
		return rampUsersPerSecTo;
	}
	public void setRampUsersPerSecTo(Double rampUsersPerSecTo) {
		this.rampUsersPerSecTo = rampUsersPerSecTo;
	}
	public String getRampUsersPerSecDuring() {
		return rampUsersPerSecDuring;
	}
	public void setRampUsersPerSecDuring(String rampUsersPerSecDuring) {
		this.rampUsersPerSecDuring = rampUsersPerSecDuring;
	}
	public String getNothingFor() {
		return nothingFor;
	}
	public void setNothingFor(String nothingFor) {
		this.nothingFor = nothingFor;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMaxResponseTime() {
		return maxResponseTime;
	}
	public void setMaxResponseTime(String maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}
}
