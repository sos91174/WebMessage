package test;

import java.util.Date;

public class Message {
	 private String content;
	    private String ip;
	    private String username;
	    private Date date;

	    public String getContent() {
	        return content;
	    }

	    public void setContent(String content) {
	        this.content = content;
	    }

	    public String getIp() {
	        return ip;
	    }

	    public void setIp(String ip) {
	        this.ip = ip;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public Date getDate() {
	        return date;
	    }

	    public void setDate(Date date) {
	        this.date = date;
	    }

	    @Override
	    public String toString() {

	        return "Message: [" + username + ", " + ip + ", " + content + "]";
	    }
	

}
