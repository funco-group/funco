import BrandButtonComponent from '@/components/Common/Button/BrandButtonComponent'
import { TradeListType } from '@/interfaces/TradeType'
import tradeTypeMap from '@/lib/tradeTypeMap'
import {
  ColumnGrid,
  ListItemContainer,
  ListItemDiv,
} from '@/styles/CommonStyled'
import { useRecoilValue } from 'recoil'
import { codeNameMapState } from '@/recoils/crypto'
import parseDate from '@/utils/parseDate'
import { DateDiv } from './OpenOrderContent.styled'

interface OpenOrderContentProps {
  content: TradeListType
  handleCancelOpenOrder: (id: number) => void
}

function OpenOrderContent({
  content,
  handleCancelOpenOrder,
}: OpenOrderContentProps) {
  const tradeDate = parseDate(content.tradeDate).split(' ').join('\n')

  const nameMap = useRecoilValue(codeNameMapState)

  return (
    <ListItemContainer>
      <ColumnGrid $column="7rem 7rem 8rem 1fr 1fr 1fr 9rem">
        <ListItemDiv $align="left" color="black">
          <DateDiv>{tradeDate}</DateDiv>
        </ListItemDiv>
        <ListItemDiv
          $align="center"
          color={content.tradeType === 'BUY' ? 'red' : 'blue'}
        >
          {tradeTypeMap.get(content.tradeType)}
        </ListItemDiv>
        <ListItemDiv $align="left" color="black">
          <img
            src={`https://static.upbit.com/logos/${content.ticker.split('-')[1]}.png`}
            alt={content.ticker}
            width={13}
          />
          {nameMap.get(content.ticker)}
        </ListItemDiv>
        <ListItemDiv $align="right" color="black">
          {content.price.toLocaleString('en-US')}
          <span>WON</span>
        </ListItemDiv>
        <ListItemDiv $align="right" color="black">
          {content.orderCash.toLocaleString('en-US')}
          <span>WON</span>
        </ListItemDiv>
        <ListItemDiv $align="right" color="black">
          {content.volume}
          <span>{content.ticker.split('-')[1]}</span>
        </ListItemDiv>
        <ListItemDiv $align="center" color="black">
          <BrandButtonComponent
            content="취소"
            color={null}
            onClick={() => handleCancelOpenOrder(content.id)}
            cancel={false}
            disabled={false}
          />
        </ListItemDiv>
      </ColumnGrid>
    </ListItemContainer>
  )
}

export default OpenOrderContent
