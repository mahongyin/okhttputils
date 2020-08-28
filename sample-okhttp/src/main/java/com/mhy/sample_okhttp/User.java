package com.mhy.sample_okhttp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {

	public String username ; 
	public String password  ;
	/**
	 * data : {"admin":false,"chapterTops":[],"coinCount":0,"collectIds":[2864,12881,7892,9517,12838,12840,8791,8676,3036,12920,12849,12847,12753,12851,12854,12850,12783,12852,12816,12802,12807,12787,12797,12824,12828,12877,12897,12901,12898,12926,13666,13680,13628,13675,13634,13652,14233,14225,14219,14278,13974,13668,13571,13459,13740,14501,8479,14690,14674,13949,14745,14712,7940,3357,8035,8023,12823,12959,8884,14895,14913,14872,14851,14214,14374],"email":"","icon":"","id":50186,"nickname":"myname","password":"","publicName":"myname","token":"","type":0,"username":"myname"}
	 * errorCode : 0
	 * errorMsg :
	 */

	private DataBean data;
	private int errorCode;
	private String errorMsg;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString()
	{
		return "User{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				'}';
	}

	public DataBean getData() {
		return data;
	}

	public void setData(DataBean data) {
		this.data = data;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public static class DataBean {
		/**
		 * admin : false
		 * chapterTops : []
		 * coinCount : 0
		 * collectIds : [2864,12881,7892,9517,12838,12840,8791,8676,3036,12920,12849,12847,12753,12851,12854,12850,12783,12852,12816,12802,12807,12787,12797,12824,12828,12877,12897,12901,12898,12926,13666,13680,13628,13675,13634,13652,14233,14225,14219,14278,13974,13668,13571,13459,13740,14501,8479,14690,14674,13949,14745,14712,7940,3357,8035,8023,12823,12959,8884,14895,14913,14872,14851,14214,14374]
		 * email :
		 * icon :
		 * id : 50186
		 * nickname : myname
		 * password :
		 * publicName : myname
		 * token :
		 * type : 0
		 * username : myname
		 */

		private boolean admin;
		private int coinCount;
		private String email;
		private String icon;
		private int id;
		private String nickname;
		@SerializedName("password")
		private String passwordX;
		private String publicName;
		private String token;
		private int type;
		@SerializedName("username")
		private String usernameX;
		private List<?> chapterTops;
		private List<Integer> collectIds;

		public boolean isAdmin() {
			return admin;
		}

		public void setAdmin(boolean admin) {
			this.admin = admin;
		}

		public int getCoinCount() {
			return coinCount;
		}

		public void setCoinCount(int coinCount) {
			this.coinCount = coinCount;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getPasswordX() {
			return passwordX;
		}

		public void setPasswordX(String passwordX) {
			this.passwordX = passwordX;
		}

		public String getPublicName() {
			return publicName;
		}

		public void setPublicName(String publicName) {
			this.publicName = publicName;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getUsernameX() {
			return usernameX;
		}

		public void setUsernameX(String usernameX) {
			this.usernameX = usernameX;
		}

		public List<?> getChapterTops() {
			return chapterTops;
		}

		public void setChapterTops(List<?> chapterTops) {
			this.chapterTops = chapterTops;
		}

		public List<Integer> getCollectIds() {
			return collectIds;
		}

		public void setCollectIds(List<Integer> collectIds) {
			this.collectIds = collectIds;
		}
	}
}
