/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // Allow importing the workspace shared package as raw TS source.
  transpilePackages: ["@enso/shared"],
  output: "standalone",
};

export default nextConfig;
