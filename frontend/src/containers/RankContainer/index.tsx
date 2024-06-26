'use client'

import { useState } from 'react'
import { RankType } from '@/interfaces/rank/RankType'
import TopRank from '@/components/Rank/TopRank'
import RankTab from '@/components/Rank/RankTab'
import RankTable from '@/components/Rank/RankTable'
import RankPagination from '@/components/Rank/RankPagination'
import { RankPageContainer } from './styled'

function Rank() {
  const [topRankList, setTopRankList] = useState<RankType[]>()
  const [nowTabName, setNowTabName] = useState('asset')
  const [totalPage, setTotalPage] = useState(1)
  const [nowPage, setNowPage] = useState(0)
  return (
    <RankPageContainer>
      {topRankList && (
        <TopRank topRankList={topRankList} nowTabName={nowTabName} />
      )}
      <RankTab
        nowTabName={nowTabName}
        setNowTabName={setNowTabName}
        setNowPage={setNowPage}
      />
      <RankTable
        setTopRankList={setTopRankList}
        nowTabName={nowTabName}
        nowPage={nowPage}
        totalPage={totalPage}
        setTotalPage={setTotalPage}
      />
      <RankPagination
        nowPage={nowPage}
        setNowPage={setNowPage}
        totalPage={totalPage}
      />
    </RankPageContainer>
  )
}

export default Rank
