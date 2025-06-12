import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: false,
  output: "standalone",
  images: {
    domains: ["ddragon.leagueoflegends.com"],
  },
};

export default nextConfig;
