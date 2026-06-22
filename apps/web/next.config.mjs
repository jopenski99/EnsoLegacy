/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'export',
  basePath: '/enso-legacy',        // ← your repo name, exact case
  assetPrefix: '/enso-legacy/',    // ← same, with trailing slash
  images: {
    unoptimized: true,             // ← required, Pages can't run Next image optimizer
  },
};

export default nextConfig;
