package com.example.ftpclientandroid;

import android.util.Log;

import org.apache.commons.net.DefaultSocketFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * @author kyang
 */
public class FTPConnection {
    private static FTPConnection instance;
    private final FTPClient ftpClient;

    private FTPConnection(boolean useFTPS) {
        if (useFTPS) {
            FTPSClient ftps = new FTPSClient("TLS", false);
            ftps.setSocketFactory(new DefaultSocketFactory());

            // Configure the FTPSClient to trust all certificates
            ftps.setTrustManager(new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // Return an empty array of certificate authorities
                    return new X509Certificate[0];
                }
            });

            this.ftpClient = ftps;
        } else {
            this.ftpClient = new FTPClient();
        }
    }

    public static synchronized FTPConnection getInstance(boolean useFTPS) {
        if (instance == null) {
            instance = new FTPConnection(useFTPS);
        }
        return instance;
    }

    public static synchronized FTPConnection getCurrentInstance() {
        return instance;
    }

    public boolean connect(String server, int port, String user, String password, boolean mode, String encoding) {
        try {
            ftpClient.connect(server, port);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.logout();
                ftpClient.disconnect();
                return false;
            }

            if (!ftpClient.login(user, password)) {
                return false;
            }

            if (ftpClient instanceof FTPSClient) {
                // 对于FTPS连接的额外配置
                ((FTPSClient) ftpClient).execPBSZ(0);
                ((FTPSClient) ftpClient).execPROT("P");
            }

            if (mode) {
                // 被动模式
                ftpClient.enterLocalPassiveMode();
            }

            ftpClient.setBufferSize(10240);
            ftpClient.setControlEncoding(encoding);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            return true;
        } catch (IOException e) {
            Log.e("connectFTP", "connection failed", e);
            return false;
        }
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    public void disconnect() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
            } catch (IOException e) {
                Log.i("logoutFTP", "error when use .logout()");
            } finally {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public FTPFile[] listFiles(String path) throws IOException {
        return ftpClient.listFiles(path);
    }

    public boolean changeWorkingDirectory(String pathname) throws IOException {
        return ftpClient.changeWorkingDirectory(pathname);
    }

    public InputStream retrieveFileStream(String remote) throws IOException {
        return ftpClient.retrieveFileStream(remote);
    }

    public boolean completePendingCommand() throws IOException {
        return ftpClient.completePendingCommand();
    }

    public boolean storeFile(String remote, InputStream local) throws IOException {
        return ftpClient.storeFile(remote, local);
    }

    public boolean deleteFile(String pathname) throws IOException {
        return ftpClient.deleteFile(pathname);
    }

    public boolean rename(String fromPath, String toPath) throws IOException {
        return ftpClient.rename(fromPath, toPath);
    }
}
