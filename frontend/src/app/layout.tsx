import type { Metadata } from 'next'
import './globals.css'
import StyledComponentsRegistry from '@/lib/registry'
import Navbar from '@/containers/HeaderContainer'
import RecoilRootWrapper from './RecoilWrapper'

export const metadata: Metadata = {
  title: 'FUNCO',
  description: '즐겁게 배우는 가상화폐 모의 투자 플랫폼',
  icons: {
    icon: '/favicon.png',
  },
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en">
      <body>
        <RecoilRootWrapper>
          <StyledComponentsRegistry>
            <Navbar />
            <div style={{ padding: '6.5rem 0' }}>{children}</div>
          </StyledComponentsRegistry>
        </RecoilRootWrapper>
      </body>
    </html>
  )
}
