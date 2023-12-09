package com.kyang.ftpclient.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.net.Inet4Address;

/**
 * @author kyang
 */
public class NetworkUtils {
    public static String[] getAllIps(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connMgr.getActiveNetwork();
        LinkProperties linkProperties = connMgr.getLinkProperties(network);

        if (linkProperties != null) {
            for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                Inet4Address ip4Address = (Inet4Address) linkAddress.getAddress();
                int ip4Prefix = linkAddress.getPrefixLength();
                if (!ip4Address.isLoopbackAddress() && ip4Address.isSiteLocalAddress()) {
                    String ip = ip4Address.getHostAddress() + "/" + ip4Prefix;
                    SubnetUtils utils = new SubnetUtils(ip);
                    return utils.getInfo().getAllAddresses();
                }
            }
        }
        return null;
    }

    public static void getIpConcurrent(String[] allIps, IpCheckListener listener) {
        for (String ip : allIps) {
            try {
                boolean isReachable = Inet4Address.getByName(ip).isReachable(2000);
                if (isReachable) {
                    listener.onIpReachable(ip);
                }
            } catch (IOException e) {
                listener.onIpCheckFailed(e);
            }
        }
    }

    public static void getFtpOnIp(String ip, FtpConnectionListener listener) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, 21);
            boolean isConnected = ftpClient.isConnected();
            ftpClient.disconnect();

            if (isConnected) {
                listener.onFtpConnectionSuccess(ip);
            }
        } catch (IOException e) {
            listener.onFtpConnectionFailed(ip);
        }

    }

    public interface IpCheckListener {
        /**
         * Called when an IP is found to be reachable.
         *
         * @param ip The reachable IP address.
         */
        void onIpReachable(String ip);

        /**
         * Called when an IP check fails.
         *
         * @param e The exception thrown during the IP check.
         */
        void onIpCheckFailed(Exception e);
    }

    public interface FtpConnectionListener {
        /**
         * Called when a successful FTP connection is established.
         *
         * @param ip The IP address where the FTP connection was successful.
         */
        void onFtpConnectionSuccess(String ip);

        /**
         * Called when an FTP connection attempt fails.
         *
         * @param ip The IP address where the FTP connection failed.
         */
        void onFtpConnectionFailed(String ip);
    }
}
