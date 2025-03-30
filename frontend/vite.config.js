import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";

export default defineConfig({
  plugins: [react()],
  define: {
    global: "window", // global 변수를 window로 매핑
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"), // @를 src 폴더로 매핑
    },
  },
  optimizeDeps: {
    include: ["stompjs"],
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
        ws: true,
        headers: {
          Connection: "keep-alive",
        },
        configure: (proxy, _options) => {
          proxy.on("error", (err, _req, _res) => {
            console.log("proxy error", err);
          });
          proxy.on("proxyReq", (proxyReq, req, _res) => {
            if (req.headers.authorization) {
              proxyReq.setHeader("Authorization", req.headers.authorization);
            }
            console.log("Sending Request:", {
              method: req.method,
              url: req.url,
              headers: proxyReq.getHeaders(),
            });
          });
          proxy.on("proxyRes", (proxyRes, req, _res) => {
            console.log("Received Response:", {
              statusCode: proxyRes.statusCode,
              url: req.url,
              headers: proxyRes.headers,
            });
          });
        },
      },
    },
  },
});
