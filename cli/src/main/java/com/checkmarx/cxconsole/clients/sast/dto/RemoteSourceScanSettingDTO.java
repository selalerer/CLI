package com.checkmarx.cxconsole.clients.sast.dto;

/**
 * Created by nirli on 08/03/2018.
 */
public class RemoteSourceScanSettingDTO {

    class Credentials {

        private String userName;

        private String password;

        public Credentials(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        public Credentials() {
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private Credentials credentials;

    private String[] paths;

    public RemoteSourceScanSettingDTO(String userName, String password, String[] paths) {
        this.credentials = new Credentials(userName, password);
        this.paths = paths;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }
}
