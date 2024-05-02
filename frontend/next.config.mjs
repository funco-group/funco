/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  webpack: (config) => {
    config.module.rules.push({
      test: /\.svg$/,
      use: ['@svgr/webpack'],
    })

    return config
  },
  compiler: {
    styledComponents: true,
  },
  async redirects() {
    return [
      {
        source: '/history',
        destination: '/history/follow',
        permanent: false,
      },
      {
        source: '/history/follow',
        destination: '/history/follow/following',
        permanent: false,
      },
    ]
  },
}

export default nextConfig
