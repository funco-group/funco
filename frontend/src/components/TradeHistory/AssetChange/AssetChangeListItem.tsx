import { ListItemDiv, ColumnGrid } from '@/styles/CommonStyled'
import { ListItemContainer } from '@/styles/ListItemContainer'
import { AssetHistoryType } from '@/interfaces/AssetType'
import { useRecoilValue } from 'recoil'
import { codeNameMapState } from '@/recoils/crypto'
import parseDate from '@/utils/parseDate'
import { AssetChangeListItemContainer } from './AssetChangeListItem.styled'

interface AssetChangeListItemProps {
  history: AssetHistoryType
}

function AssetChangeListItem({ history }: AssetChangeListItemProps) {
  const tradeTypeMap = new Map([
    ['BUY', '매수'],
    ['SELL', '매도'],
    ['FOLLOWING', '팔로잉'],
    ['FOLLOWER', '팔로워'],
  ])
  const nameMap = useRecoilValue(codeNameMapState)

  const getColorForTradeType = (tradeType: string) => {
    switch (tradeType) {
      case 'BUY':
        return 'red'
      case 'SELL':
        return 'blue'
      default:
        return 'black'
    }
  }
  return (
    <ListItemContainer>
      <AssetChangeListItemContainer>
        <ColumnGrid $column="7rem 6rem 5rem 1.3fr 1fr 1fr 1fr 1fr">
          <ListItemDiv $align="left" color="black">
            {parseDate(history.date)}
          </ListItemDiv>
          <ListItemDiv $align="left" color="black">
            {history.assetType === 'COIN' ? (
              <>
                <img
                  src={`https://static.upbit.com/logos/${history.name.split('-')[1]}.png`}
                  alt={history.name}
                  width={13}
                />
                {nameMap.get(history.name)}
              </>
            ) : (
              <>
                <img src="/icon/follow-icon.png" alt="팔로우" width={13} />
                팔로우
              </>
            )}
          </ListItemDiv>
          <ListItemDiv
            $align=""
            color={getColorForTradeType(history.tradeType)}
          >
            {tradeTypeMap.get(history.tradeType)}
          </ListItemDiv>
          <ListItemDiv
            $align={history.assetType === 'COIN' ? 'right' : 'center'}
            color="black"
          >
            {history.assetType === 'COIN' ? history.volume : '-'}
            {history.assetType === 'COIN' && <span>WON</span>}
          </ListItemDiv>
          <ListItemDiv
            $align={history.price ? 'right' : 'center'}
            color="black"
          >
            {history.price ? history.price.toLocaleString('ko-KR') : '-'}
            {history.price && <span>WON</span>}
          </ListItemDiv>
          <ListItemDiv $align="right" color="black">
            {history.orderCash.toLocaleString('ko-KR')}
            <span>WON</span>
          </ListItemDiv>
          <ListItemDiv
            $align={history.commission !== null ? 'right' : 'center'}
            color="black"
          >
            {history.commission !== null
              ? history.commission.toLocaleString('ko-KR')
              : '-'}
            {history.commission !== null && <span>WON</span>}
          </ListItemDiv>
          <ListItemDiv
            $align={history.settlement !== null ? 'right' : 'center'}
            color="black"
          >
            {history.settlement !== null
              ? history.settlement.toLocaleString()
              : '-'}
            {history.settlement !== null && <span>WON</span>}
          </ListItemDiv>
        </ColumnGrid>
      </AssetChangeListItemContainer>
    </ListItemContainer>
  )
}

export default AssetChangeListItem
