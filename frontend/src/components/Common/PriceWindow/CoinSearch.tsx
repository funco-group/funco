import { memo } from 'react'
import { CoinSearchContainer } from './CoinSearch.styled'
// import SearchIcon from '/image/icon/search-icon.svg'

interface CoinSearchProps {
  setSearch: React.Dispatch<React.SetStateAction<string>>
}

const CoinSearch = memo(({ setSearch }: CoinSearchProps) => (
  <CoinSearchContainer>
    <img src="/icon/search-icon.svg" alt="search-icon" />
    <input
      type="text"
      placeholder="코인명/심볼검색"
      onChange={(e) => {
        setSearch(e.target.value)
      }}
    />
  </CoinSearchContainer>
))

export default CoinSearch
