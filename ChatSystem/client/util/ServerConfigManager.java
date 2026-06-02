package ChatSystem.client.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServerConfigManager {
    private static final String FILE_NAME = "servers.dat";

    // Lớp chứa thông tin một Server
    public static class ServerInfo {
        public String name;
        public String host;
        public int port;

        public ServerInfo(String name, String host, int port) {
            this.name = name;
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return name + " (" + host + ":" + port + ")";
        }
    }

    // Nạp danh sách từ file
    public static List<ServerInfo> loadServers() {
        List<ServerInfo> servers = new ArrayList<>();
        File file = new File(FILE_NAME);
        
        if (!file.exists()) {
            // Nếu chưa có file, tạo mặc định 1 server Localhost
            servers.add(new ServerInfo("Local Server", "localhost", 12345));
            saveServers(servers);
            return servers;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    servers.add(new ServerInfo(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim())));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file cấu hình: " + e.getMessage());
        }
        return servers;
    }

    // Lưu danh sách xuống file
    public static void saveServers(List<ServerInfo> servers) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (ServerInfo s : servers) {
                bw.write(s.name + "," + s.host + "," + s.port);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi ghi file cấu hình: " + e.getMessage());
        }
    }
}