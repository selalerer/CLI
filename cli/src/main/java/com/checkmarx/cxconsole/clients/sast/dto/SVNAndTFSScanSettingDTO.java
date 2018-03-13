package com.checkmarx.cxconsole.clients.sast.dto;

/**
 * Created by nirli on 08/03/2018.
 */
public class SVNAndTFSScanSettingDTO extends RemoteSourceScanSettingDTO {

    public class Uri {
        String absoluteUrl;

        int port;

        Uri(String absoluteUrl, int port) {
            this.absoluteUrl = absoluteUrl;
            this.port = port;
        }

        public String getAbsoluteUrl() {
            return absoluteUrl;
        }

        public void setAbsoluteUrl(String absoluteUrl) {
            this.absoluteUrl = absoluteUrl;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    private Uri uri;

    private byte[] privateKey;


    public SVNAndTFSScanSettingDTO(String userName, String password, String[] paths, String absoluteUrl, int port, byte[] privateKey) {
        super(userName, password, paths);
        this.uri = new Uri(absoluteUrl, port);
        this.privateKey = privateKey;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }
}
