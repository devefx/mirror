package org.devefx.mirror.model;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.annotation.Table;

@Table(value="t09_sys_admin_info", key="sys_admin_id")
public class AdminInfo {
	private @Column("sys_admin_id") int id;
	private @Column String username;
	private @Column String password;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
