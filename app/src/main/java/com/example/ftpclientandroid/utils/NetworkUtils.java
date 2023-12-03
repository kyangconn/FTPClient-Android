package com.example.ftpclientandroid.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;

import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.concurrent.ThreadPoolExecutor;

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

    public static void checkIpsConcurrently(String[] allIps, ThreadPoolExecutor executor, IpCheckListener listener) {
        for (String ip : allIps) {
            executor.execute(() -> {
                try {
                    boolean isReachable = Inet4Address.getByName(ip).isReachable(2000);
                    if (isReachable) {
                        listener.onIpReachable(ip);
                    }
                } catch (IOException e) {
                    listener.onIpCheckFailed(e);
                }
            });
        }
    }


    public interface IpCheckListener {
        void onIpReachable(String ip);
        void onIpCheckFailed(Exception e);
    }
}
