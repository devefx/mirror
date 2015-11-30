package org.devefx.mirror.model;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.annotation.Entity;
import org.devefx.mirror.annotation.Table;

@Table("t06_equity")
public class Equity {
	private @Column Integer id;
	private @Entity("member_id") Member member;
	private @Entity("audit_id") AdminInfo adminInfo;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}
	public AdminInfo getAdminInfo() {
		return adminInfo;
	}
	public void setAdminInfo(AdminInfo adminInfo) {
		this.adminInfo = adminInfo;
	}
}
