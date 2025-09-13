import java.io.*;
import java.util.*;
import java.util.regex.*;

public class WiFiAnalyzer {

    static class WiFiNetwork {
        String ssid;
        int signal;
        String auth;
        boolean isConnected;
        String download = "N/A";
        String upload = "N/A";
        String ping = "N/A";

        WiFiNetwork(String ssid, int signal, String auth, boolean isConnected) {
            this.ssid = ssid;
            this.signal = signal;
            this.auth = auth;
            this.isConnected = isConnected;
        }
    }

    public static void main(String[] args) {
        try {
            clearConsole();

            System.out.println("📶 Wi-Fi Analyzer");
            System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

            String connectedSSID = getConnectedSSID();
            List<WiFiNetwork> networks = scanNetworks(connectedSSID);

            if (networks.isEmpty()) {
                System.out.println("❌ No Wi-Fi networks found.");
                return;
            }

            // Sort: connected SSID first, then by signal descending
            networks.sort((a, b) -> {
                if (a.isConnected && !b.isConnected) return -1;
                if (!a.isConnected && b.isConnected) return 1;
                return Integer.compare(b.signal, a.signal);
            });

            boolean speedTestAvailable = isSpeedTestAvailable();

            for (WiFiNetwork net : networks) {
                if (net.isConnected && speedTestAvailable) {
                    System.out.println("⏳ Measuring speed for connected network: " + net.ssid);
                    measureActualSpeed(net);
                } else {
                    estimateSpeed(net);
                }
            }

            // Display full table
            System.out.println("\n📊 Wi-Fi Networks (Sorted by Strength)");
            System.out.println("─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("%-40s %-10s %-25s %-15s %-20s %-15s %-15s %-10s\n",
                    "SSID", "Strength", "Signal Graph", "Level", "Security", "⬇️ Download", "⬆️ Upload", "🏓 Ping");
            System.out.println("─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");

            for (WiFiNetwork net : networks) {
                String color = getSignalColor(net.signal);
                String barGraph = getSignalGraph(net.signal, 20);
                String level = getSignalLevel(net.signal);

                System.out.printf("%-40s %s%-10s\033[0m %s%-25s\033[0m %s%-15s\033[0m %-20s %-15s %-15s %-10s\n",
                        truncate(net.ssid, 40),
                        color, net.signal + "%",
                        color, barGraph,
                        color, level,
                        net.auth,
                        net.download,
                        net.upload,
                        net.ping);
            }

            if (!speedTestAvailable) {
                System.out.println("\n⚠️  Speedtest not found. Install it with: pip install speedtest-cli");
            }

            System.out.println("\n✅ Scan complete.");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    private static List<WiFiNetwork> scanNetworks(String connectedSSID) {
        List<WiFiNetwork> networks = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("netsh wlan show networks mode=bssid");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            String currentSSID = null;
            String currentAuth = null;

            Pattern ssidPattern = Pattern.compile("^SSID \\d+ : (.*)$");
            Pattern authPattern = Pattern.compile("Authentication\\s*:\\s*(.*)");
            Pattern signalPattern = Pattern.compile("Signal\\s*:\\s*(\\d+)%");

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                Matcher ssidMatcher = ssidPattern.matcher(line);
                Matcher authMatcher = authPattern.matcher(line);
                Matcher signalMatcher = signalPattern.matcher(line);

                if (ssidMatcher.find()) {
                    currentSSID = ssidMatcher.group(1);
                } else if (authMatcher.find()) {
                    currentAuth = authMatcher.group(1);
                } else if (signalMatcher.find() && currentSSID != null) {
                    int signal = Integer.parseInt(signalMatcher.group(1));
                    boolean isConnected = connectedSSID != null && connectedSSID.equals(currentSSID);
                    networks.add(new WiFiNetwork(currentSSID, signal, currentAuth, isConnected));
                    currentSSID = null;
                    currentAuth = null;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Failed to scan networks: " + e.getMessage());
        }
        return networks;
    }

    private static String getConnectedSSID() {
        try {
            Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Pattern pattern = Pattern.compile("SSID\\s*:\\s*(.*)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Failed to get connected SSID: " + e.getMessage());
        }
        return null;
    }

    private static void measureActualSpeed(WiFiNetwork net) {
        try {
            Process process = Runtime.getRuntime().exec("speedtest -f json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();

            if (output != null) {
                net.download = extractSpeed(output, "\"download\":\\s*\\{\"bandwidth\":(\\d+)");
                net.upload = extractSpeed(output, "\"upload\":\\s*\\{\"bandwidth\":(\\d+)");
                net.ping = extractPing(output) + " ms";
            }
        } catch (Exception e) {
            net.download = net.upload = net.ping = "Error";
        }
    }

    private static String extractSpeed(String json, String regex) {
        Matcher m = Pattern.compile(regex).matcher(json);
        if (m.find()) {
            double bitsPerSec = Double.parseDouble(m.group(1)) * 8;
            double mbps = bitsPerSec / 1_000_000.0;
            return String.format("%.1f Mbps", mbps);
        }
        return "0 Mbps";
    }

    private static String extractPing(String json) {
        Matcher m = Pattern.compile("\"latency\":\\s*\\{\"iqm\":([0-9.]+)").matcher(json);
        if (m.find()) return m.group(1);
        return "?";
    }

    private static void estimateSpeed(WiFiNetwork net) {
        if (net.signal >= 80) net.download = "100-300 Mbps";
        else if (net.signal >= 60) net.download = "50-100 Mbps";
        else if (net.signal >= 40) net.download = "10-50 Mbps";
        else net.download = "<10 Mbps";
        net.upload = "(E X P E C T E D)";
        net.ping = "";
    }

    private static boolean isSpeedTestAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("speedtest --version");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static String getSignalGraph(int strength, int width) {
        int filled = (strength * width) / 100;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < width; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        return bar.toString();
    }

    private static String getSignalLevel(int strength) {
        if (strength >= 80) return "EXCELLENT";
        if (strength >= 60) return "GOOD";
        if (strength >= 40) return "FAIR";
        return "WEAK";
    }

    private static String getSignalColor(int strength) {
        if (strength >= 80) return "\033[1;32m"; // Green
        if (strength >= 60) return "\033[1;33m"; // Yellow
        if (strength >= 40) return "\033[1;35m"; // Magenta
        return "\033[1;31m"; // Red
    }

    private static String truncate(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
}
