package com.kyang.ftpclient.utils;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author kyang
 */
public class FtpManager {
    private static FtpManager instance;
    private final FTPClient ftpClient;

    private FtpManager(boolean useFtps) {
        if (useFtps) {
            FTPSClient ftps = new FTPSClient("TLS", false);
            ftps.setSocketFactory(SSLSocketFactory.getDefault());

            this.ftpClient = ftps;
        } else {
            this.ftpClient = new FTPClient();
        }
    }

    public static synchronized FtpManager getInstance(boolean useFtps) {
        if (instance == null) {
            instance = new FtpManager(useFtps);
        }
        return instance;
    }

    public static synchronized FtpManager getCurrentInstance() {
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
                ((FTPSClient) ftpClient).execPBSZ(0);
                ((FTPSClient) ftpClient).execPROT("P");
            }

            if (mode) {
                ftpClient.enterLocalPassiveMode();
            }

            ftpClient.setBufferSize(10240);
            ftpClient.setControlEncoding(encoding);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);

            return true;
        } catch (IOException e) {
            Log.e("connectFTP", "connection failed", e);
            return false;
        }
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
