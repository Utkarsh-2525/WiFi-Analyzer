# 📶 WiFi Analyzer – Java CLI Tool  

A **cross-platform Java-based Wi-Fi Analyzer** that scans available networks, detects the currently connected SSID, measures actual internet speed for the connected network, estimates speeds for others, and displays everything in a **beautiful, color-coded, icon-rich CLI dashboard**.

---

## ✨ Features  

✅ **Network Scan** – Lists all nearby Wi-Fi networks with SSID, signal strength, security type, and graphical signal bars.  
✅ **Connected Network Detection** – Automatically detects and highlights the SSID you are currently connected to.  
✅ **Speed Test Integration** – Measures **actual download, upload, and ping** for the connected network using `speedtest-cli`.  
✅ **Estimated Speeds** – Predicts speeds for other networks based on signal strength.  
✅ **Colorized Output** – Uses ANSI colors and icons for a modern, easy-to-read terminal UI.  
✅ **Sorted by Strength** – Displays networks in descending order of signal strength, with the connected network on top.  
✅ **Cross-Platform** – Works on Windows (via `netsh`), and can be extended for Linux/macOS (`nmcli`, `airport`).  

---

## 🖼 Example Output  

```text
📶 Wi-Fi Analyzer
────────────────────────────────────────────────────────────────────────────────────────────

⏳ Measuring speed for connected network: My_Home_Network

📊 Wi-Fi Networks (Sorted by Strength)
────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
SSID                                     Strength   Signal Graph              Level       Security             Download        Upload          Ping      
────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
My_Home_Network                          92%        ██████████████████░░░░    EXCELLENT   WPA2-Personal       ⬇ 122.4 Mbps   ⬆ 110.3 Mbps   🏓 9 ms  
Neighbour_WiFi                           65%        ██████████████░░░░░░░░    GOOD        WPA3-SAE            ⬇ 50-100 Mbps ⬆ EXPECTED     ~          
Cafe_Free_WiFi                           43%        ████████░░░░░░░░░░░░░░   FAIR        Open                ⬇ 10-50 Mbps  ⬆ EXPECTED     ~          
Old_Router                               22%        ████░░░░░░░░░░░░░░░░░░   WEAK        WEP                 ⬇ <10 Mbps    ⬆ EXPECTED     ~          

✅ Scan complete.
```
🚀 Installation & Usage
1️⃣ Clone the Repository
git clone https://github.com/your-username/WiFiAnalyzer.git
cd WiFiAnalyzer

2️⃣ Compile the Project
javac WiFiAnalyzer.java

3️⃣ Run the Program
java WiFiAnalyzer

4️⃣ (Optional) Install Speedtest CLI

For accurate speed measurement of the connected SSID, install speedtest-cli
:

pip install speedtest-cli

🖥 Commands Used Internally

This project uses standard OS-level commands to collect Wi-Fi data and run speed tests:

Purpose	Command (Windows)	Example Output
Scan networks	netsh wlan show networks mode=bssid	SSIDs, signals, security types
Get connected network	netsh wlan show interfaces	Current SSID name
Run speed test	speedtest -f json	Download, upload, ping in JSON

Note: For Linux/macOS support, these commands can be replaced with:

Linux: nmcli dev wifi list

macOS: /System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport -s

⚙️ How It Works

Uses netsh wlan show networks to scan available SSIDs (on Windows).

Detects the connected SSID using netsh wlan show interfaces.

Performs real-time speed tests using speedtest-cli (if available).

Estimates speeds for unconnected networks based on signal percentage.

Displays results in a sorted, formatted, and colorized table.

🛠 Planned Features

🔄 Cross-platform support for Linux (nmcli) and macOS (airport -s).

📄 CSV/JSON export of scan results for logging and analysis.

📡 Continuous monitoring mode to refresh data periodically.
